package fixeh.instrument.woventools.policy;

import java.io.*;

import fixeh.instrument.woventools.Log;
import fixeh.instrument.woventools.LogProxy;

/**
 * Created by Shunjie Ding on 29/01/2018.
 */
public final class newControlPolicyFactory {
    private static final Log LOG = LogProxy.getInstance();

    private static final String DEFAULT_POLICY_XML_CONFIG = "/data/local/tmp/fixeh-policy.xml";

    public static newGeneralControlPolicy buildGeneralControlPolicy(PolicyConfig policyConfig) {
        newGeneralControlPolicy generalControlPolicy = new newGeneralControlPolicy();

        if (policyConfig.getPolicyEntries() != null) {
            // fix NPE
            for (PolicyConfig.PolicyEntry policyEntry : policyConfig.getPolicyEntries()) {
                //LOG.d(LogProxy.LOG_TAG,"find " + policyEntry.getKind());
                switch (policyEntry.getKind()) {
                    case PolicyConfig.PolicyEntryKinds.PACKAGE:
                       if(policyEntry.getOther("filtermode") != null){
                           boolean flag =  Integer.parseInt(policyEntry.getOther("filtermode")) == 0 ? false : true;
                           generalControlPolicy.setFilterPackage(flag);
                       }
                       if(policyEntry.getOther("stack") !=  null){
                           generalControlPolicy.setActivePackages(policyEntry.getValue(), policyEntry.getOther("stack"));
                       }else{
                           generalControlPolicy.setActivePackages(policyEntry.getValue(), null);
                       }
                        break;
                    case PolicyConfig.PolicyEntryKinds.CLASS:
                        if(policyEntry.getOther("filtermode") != null){
                            boolean flag =  Integer.parseInt(policyEntry.getOther("filtermode")) == 0 ? false : true;
                            generalControlPolicy.setFilterClasses(flag);
                        }
                        if(policyEntry.getOther("stack") !=  null){
                            generalControlPolicy.setActiveClasses(policyEntry.getValue(), policyEntry.getOther("stack"));
                        }else{
                            generalControlPolicy.setActiveClasses(policyEntry.getValue(), null);
                        }
                        break;
                    case PolicyConfig.PolicyEntryKinds.METHOD:
                        if(policyEntry.getOther("filtermode") != null){
                            boolean flag =  Integer.parseInt(policyEntry.getOther("filtermode")) == 0 ? false : true;
                            generalControlPolicy.setFilterInvocations(flag);
                        }
                        if(policyEntry.getOther("stack") !=  null){
                            generalControlPolicy.setActiveInvocations(policyEntry.getValue(), policyEntry.getOther("stack"));
                        }else{
                            generalControlPolicy.setActiveInvocations(policyEntry.getValue(), null);
                        }
                        if(policyEntry.getOther("pattern") != null){
                            generalControlPolicy.setInvocationPatterns(policyEntry.getValue(), policyEntry.getOther("pattern"));
                        }else{
                            generalControlPolicy.setInvocationPatterns(policyEntry.getValue(), null);
                        }
                        break;
                    case PolicyConfig.PolicyEntryKinds.EXCEPTION:
                        if(policyEntry.getOther("filtermode") != null){
                            boolean flag =  Integer.parseInt(policyEntry.getOther("filtermode")) == 0 ? false : true;
                            generalControlPolicy.setFilterExceptions(flag);
                        }
                        if(policyEntry.getOther("stack") !=  null){
                            generalControlPolicy.setActiveExceptions(policyEntry.getValue(), policyEntry.getOther("stack"));
                        }else{
                            generalControlPolicy.setActiveExceptions(policyEntry.getValue(), null);
                        }
                        break;
                    case PolicyConfig.PolicyEntryKinds.STACKTRACE:
                        if(policyEntry.getOther("filtermode") != null){
                            boolean flag =  Integer.parseInt(policyEntry.getOther("filtermode")) == 0 ? false : true;
                            generalControlPolicy.setFilterExceptions(flag);
                        }
                        if(policyEntry.getOther("keyword") != null){
                            generalControlPolicy.setActiveStackKeyWords(policyEntry.getOther("keyword"));
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
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                // ignore and fallback to default
                LOG.d(LogProxy.LOG_TAG,"something wrong!! :: " + e.toString() + " :: " + sw.toString());
            }
        }

        LOG.i(LogProxy.LOG_TAG, "Using default force control policy!");
        return new ForceControlPolicy();
    }
}
