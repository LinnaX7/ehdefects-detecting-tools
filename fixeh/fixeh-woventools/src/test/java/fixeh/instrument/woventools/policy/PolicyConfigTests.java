package fixeh.instrument.woventools.policy;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class PolicyConfigTests {
    private static final String TEST_XML_DOCUMENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<fixeh>\n"
        + "    <!-- address should be replaced before pushing to remote device -->\n"
        + "    <remote-controller enable=\"false\" address=\"127.0.0.1\" port=\"7675\"/>\n"
        + "    <!-- if exclude is false, then all package/class/method will be supposed to be included -->\n"
        + "    <policy exclude=\"true\" search=\"false\" limit=\"5\">\n"
        + "        <!-- policy file may contain any entries this file contains, and value may be remote(http/https) -->\n"
        + "        <!-- ONLY THE FIRST FILE POLICY WILL BE USED! -->\n"
        + "\n"
        + "        <!--<policyentry kind=\"file\" value=\"file:///data/local/tmp/fixeh-policy.xml\"/>-->\n"
        + "\n"
        + "        <!-- if policy file is specified and is valid, any other entries will be ignored -->\n"
        + "\n"
        + "        <!-- ALL EXCLUDED -->\n"
        + "\n"
        + "        <!-- you can specify as many package/class/method entries as you can (even if duplicated), -->\n"
        + "        <!-- class name must be full qualified, method name must be set like the example below -->\n"
        + "        <policyentry kind=\"package\" value=\"android.database\"/>\n"
        + "        <policyentry kind=\"class\" value=\"android.database.sqlite.SQLiteDatabase\"/>\n"
        + "        <policyentry kind=\"method\"\n"
        + "                     value=\"android.database.sqlite.SQLiteDatabase: void execSQL(java.lang.String)\"/>\n"
        + "    </policy>\n"
        + "</fixeh>";

    private static final String Exp_TEST_XML_DOCUMENT =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<fixeh>\n"
        + "    <!-- address should be replaced before pushing to remote device -->\n"
        + "    <remote-controller enable=\"false\" address=\"127.0.0.1\" port=\"7675\"/>\n"
        + "    <!-- if exclude is false, then all package/class/method will be supposed to be included -->\n"
        + "    <policy exclude=\"false\" search=\"false\" limit=\"5\">\n"
        + "        <!-- policy file may contain any entries this file contains, and value may be remote(http/https) -->\n"
        + "        <!-- ONLY THE FIRST FILE POLICY WILL BE USED! -->\n"
        + "\n"
        + "        <!--<policyentry kind=\"file\" value=\"file:///data/local/tmp/fixeh-policy.xml\"/>-->\n"
        + "\n"
        + "        <!-- if policy file is specified and is valid, any other entries will be ignored -->\n"
        + "\n"
        + "        <!-- ALL EXCLUDED -->\n"
        + "\n"
        + "        <!-- you can specify as many package/class/method entries as you can (even if duplicated), -->\n"
        + "        <!-- class name must be full qualified, method name must be set like the example below -->\n"
        + "        <policyentry kind=\"package\" value=\"android.database\"/>\n"
        + "        <policyentry kind=\"class\" value=\"android.database.sqlite.SQLiteDatabase\"/>\n"
        + "        <policyentry kind=\"exception\"\n"
        + "                     value=\"java.io.IOException\" pattern=\"01\"/>\n"
        + "    </policy>\n"
        + "</fixeh>";

    @Test
    public void testParsingXmlConfig() throws ParserConfigurationException, IOException,
                                              SAXException, InvalidPolicyConfigException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new ByteArrayInputStream(TEST_XML_DOCUMENT.getBytes()));
        PolicyConfig policyConfig = PolicyConfigUtils.buildPolicyConfig(document);

        Assert.assertNotNull(policyConfig.getRemoteController());
        Assert.assertTrue(!policyConfig.isRemoteControllerEnabled());
        Assert.assertNull(policyConfig.getFirstFilePolicyValue());
        Assert.assertTrue(policyConfig.isExclude());
        Assert.assertEquals(5, policyConfig.getLimit());
        Assert.assertEquals(3, policyConfig.getPolicyEntries().size());
    }

    @Test
    public void testGeneralControlPolicyFromXml()
        throws ParserConfigurationException, InvalidPolicyConfigException, IOException,
               SAXException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new ByteArrayInputStream(TEST_XML_DOCUMENT.getBytes()));
        PolicyConfig policyConfig = PolicyConfigUtils.buildPolicyConfig(document);

        ControlPolicy controlPolicy = ControlPolicyFactory.buildGeneralControlPolicy(policyConfig);
        Assert.assertTrue(controlPolicy.getClass().equals(GeneralControlPolicy.class));

        Assert.assertTrue(((GeneralControlPolicy) controlPolicy).isExclude());
        Assert.assertFalse(controlPolicy.takeOver(
            "android.database.sqlite.SQLiteDatabase: void execSQL(java.lang.String)", 1, null));
        Assert.assertTrue(
            controlPolicy.takeOver("java.io.File: java.io.File getCanonicalFile()", 1, null));
        Assert.assertFalse(
            controlPolicy.takeOver("java.io.File: java.io.File getCanonicalFile()", 6, null));
    }
    @Test
    public void testGeneralControlPolicyFromXmlException()
        throws ParserConfigurationException, InvalidPolicyConfigException, IOException,
               SAXException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new ByteArrayInputStream(Exp_TEST_XML_DOCUMENT.getBytes()));
        PolicyConfig policyConfig = PolicyConfigUtils.buildPolicyConfig(document);
        ControlPolicy controlPolicy = ControlPolicyFactory.buildGeneralControlPolicy(policyConfig);
        Assert.assertFalse(((GeneralControlPolicy) controlPolicy).isExclude());
        Assert.assertTrue(
            ((GeneralControlPolicy) controlPolicy)
                .takeOver("android.database.sqlite.SQLiteDatabase: void execSQL(java.lang.String)",
                    6, new IOException(), 1));
        Assert.assertFalse(((GeneralControlPolicy) controlPolicy)
                               .takeOver("java.io.File: void close()", 1, new IOException(), 1));
        Assert.assertFalse(
            ((GeneralControlPolicy) controlPolicy)
                .takeOver("android.database.sqlite.SQLiteDatabase: void execSQL(java.lang.String)",
                    1, new RuntimeException(), 1));
        Assert.assertFalse(
            ((GeneralControlPolicy) controlPolicy)
                .takeOver("android.database.sqlite.SQLiteDatabase: void execSQL(java.lang.String)",
                    1, new IOException(), 6));
    }
}
