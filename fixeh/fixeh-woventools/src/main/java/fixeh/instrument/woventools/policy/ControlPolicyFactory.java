package fixeh.instrument.woventools.policy;

import java.io.File;
import java.io.IOException;
import fixeh.instrument.woventools.Log;
import fixeh.instrument.woventools.LogProxy;

/**
 * Created by Shunjie Ding on 29/01/2018.
 */
public final class ControlPolicyFactory {
    private static final Log LOG = LogProxy.getInstance();

    private static final String DEFAULT_POLICY_XML_CONFIG = "/data/local/tmp/fixeh-policy.xml";

    public static GeneralControlPolicy buildGeneralControlPolicy(PolicyConfig policyConfig) {
        GeneralControlPolicy generalControlPolicy = new GeneralControlPolicy();

        if (policyConfig.getPolicyEntries() != null) {
            // fix NPE
            for (PolicyConfig.PolicyEntry policyEntry : policyConfig.getPolicyEntries()) {
                //LOG.d(LogProxy.LOG_TAG,"find " + policyEntry.getKind());
                switch (policyEntry.getKind()) {
                    case PolicyConfig.PolicyEntryKinds.PACKAGE:
                        generalControlPolicy.addPackage(policyEntry.getValue());
                        break;
                    case PolicyConfig.PolicyEntryKinds.CLASS:
                        generalControlPolicy.addClass(policyEntry.getValue());
                        break;
                    case PolicyConfig.PolicyEntryKinds.METHOD:
                        generalControlPolicy.addMethod(policyEntry.getValue());
                        // Set pattern if specified
                        if (policyEntry.getOther("pattern") != null) {
                            generalControlPolicy.setMethodPattern(
                                    policyEntry.getValue(), policyEntry.getOther("pattern"));
                        }
                        break;
                    case PolicyConfig.PolicyEntryKinds.EXCEPTION:
                        generalControlPolicy.addException(policyEntry.getValue());
                        if (policyEntry.getOther("pattern") != null) {
                            generalControlPolicy.setExceptionPattern(policyEntry.getValue(),
                                    policyEntry.getOther("pattern"));
                        /*generalControlPolicy.setExceptionMethodPatterns(
                                policyEntry.getValue(), policyEntry.getOther("method"),
                                policyEntry.getOther("pattern"));*/
                        }
                        if (policyEntry.getOther("maxcount") != null){
                            generalControlPolicy.setStackMaxrepeat(policyEntry.getValue(),policyEntry.getOther("maxcount"));
                        }else{
                            generalControlPolicy.setStackMaxrepeat(policyEntry.getValue(),String.valueOf(-1));
                        }
                        break;
                    case PolicyConfig.PolicyEntryKinds.FILTER:
                        switch(policyEntry.getOther("type")){
                            case "method":
                                generalControlPolicy.addFilterMethods(policyEntry.getValue(),
                                        policyEntry.getOther("stackkeyword"));

                                break;
                            case "package":
                                generalControlPolicy.addFilterPackage(policyEntry.getValue(),
                                        policyEntry.getOther("stackkeyword"));
                                break;
                            case "class":
                                generalControlPolicy.addFilterClasses(policyEntry.getValue(),
                                        policyEntry.getOther("stackkeyword"));
                                break;
                            case "stackkeyword":
                                generalControlPolicy.addFilterStackKeyWords(policyEntry.getValue());
                        }

                        break;
                }
            }
        }

            generalControlPolicy.setLimit(policyConfig.getLimit());
            generalControlPolicy.setExclude(policyConfig.isExclude());
            generalControlPolicy.setGeneralPattern(policyConfig.getGeneralPattern());
            return generalControlPolicy;
        }


    public static ControlPolicy autoDetect() {
        // Construct policy using the following order
        // 1. remote controlled policy
        // 2. remote xml file
        // 3. local xml file
        // 4. default force control policy

        File configFile = new File(DEFAULT_POLICY_XML_CONFIG);

        if (configFile.exists()) {

            try {

                PolicyConfig policyConfig =
                    PolicyConfigUtils.buildPolicyConfig(configFile.toURI().toURL());
                if (policyConfig.isRemoteControllerEnabled()) {
                    PolicyConfig.RemoteController remoteController =
                        policyConfig.getRemoteController();
                    LOG.i(LogProxy.LOG_TAG,
                        String.format("Generating RemoteDynamicControlPolicy(%s:%d)",
                            remoteController.getAddress(), remoteController.getPort()));
                    return new RemoteDynamicControlPolicy(remoteController.getAddress(),
                        remoteController.getPort(), buildGeneralControlPolicy(policyConfig));
                } else {
                    String url = policyConfig.getFirstFilePolicyValue();
                    if (url != null && !DEFAULT_POLICY_XML_CONFIG.equals(url)) {
                        LOG.i(LogProxy.LOG_TAG,
                            "Generating GeneralControlPolicy from remote config: " + url);
                        return buildGeneralControlPolicy(
                            PolicyConfigUtils.buildPolicyConfig(configFile.toURI().toURL()));
                    } else {
                        LOG.i(
                            LogProxy.LOG_TAG, "Generating GeneralControlPolicy from local config");
                        return buildGeneralControlPolicy(policyConfig);
                    }
                }
            } catch (IOException | InvalidPolicyConfigException e) {
                // ignore and fallback to default
                LOG.d(LogProxy.LOG_TAG,"something wrong!!");
            }
        }

        LOG.i(LogProxy.LOG_TAG, "Using default force control policy!");
        return new ForceControlPolicy();
    }
}
