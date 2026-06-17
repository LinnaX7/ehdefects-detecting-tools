package fixeh.instrument.woventools.policy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public final class PolicyConfigUtils {
    private static String getAttributeStringValue(NamedNodeMap attributes, String name) {
        Node node = attributes.getNamedItem(name);
        return node == null ? null : node.getNodeValue();
    }

    private static boolean containsAttribute(NamedNodeMap attributes, String name) {
        return attributes.getNamedItem(name) != null;
    }

    private static PolicyConfig.RemoteController getRemoteControllerFromDocNode(Node node)
        throws InvalidPolicyConfigException {
        if (node == null) {
            return null;
        }

        NamedNodeMap attributes = node.getAttributes();
        PolicyConfig.RemoteController remoteController = new PolicyConfig.RemoteController();

        if (!containsAttribute(attributes, "enable")) {
            throw new InvalidPolicyConfigException(
                "Node remote-controller must have attribute 'enable'!");
        }

        remoteController.setEnabled(Boolean.valueOf(getAttributeStringValue(attributes, "enable")));

        if (remoteController.isEnabled()) {
            if (!containsAttribute(attributes, "address")
                || !containsAttribute(attributes, "port")) {
                throw new InvalidPolicyConfigException(
                    "Node remote-controller must have attribute 'address' and 'port' if enabled!");
            }

            try {
                remoteController.setAddress(getAttributeStringValue(attributes, "address"));
                remoteController.setPort(Integer.valueOf(
                    Objects.requireNonNull(getAttributeStringValue(attributes, "port"))));
            } catch (Exception e) {
                throw new InvalidPolicyConfigException(e.getMessage(), e);
            }

            if (remoteController.getAddress() == null || remoteController.getAddress().isEmpty()) {
                throw new InvalidPolicyConfigException(
                    "Node remote-controller must have a valid address when enabled!");
            }
        }

        return remoteController;
    }

    private static boolean checkPolicyValue(String kind, String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        switch (kind) {
            case PolicyConfig.PolicyEntryKinds.FILE:
                try {
                    new URL(value);
                    return true;
                } catch (MalformedURLException e) {
                    return false;
                }

            case PolicyConfig.PolicyEntryKinds.PACKAGE:
            case PolicyConfig.PolicyEntryKinds.CLASS:
            case PolicyConfig.PolicyEntryKinds.METHOD:
            case PolicyConfig.PolicyEntryKinds.EXCEPTION:
            case PolicyConfig.PolicyEntryKinds.STACKTRACE:
                // Do not check class or packages or methods
                return true;
        }

        return false;
    }

    private static PolicyConfig.PolicyEntry getPolicyEntryFromDocNode(Node node)
        throws InvalidPolicyConfigException {
        if (node == null) {
            return null;
        }

        NamedNodeMap attributes = node.getAttributes();
        PolicyConfig.PolicyEntry policyEntry = new PolicyConfig.PolicyEntry();

        if (!(containsAttribute(attributes, "kind") && containsAttribute(attributes, "value"))) {
            throw new InvalidPolicyConfigException(
                "Node policyentry must have attributes 'kind' and 'value'!");
        }

        policyEntry.setKind(getAttributeStringValue(attributes, "kind"));
        if (!PolicyConfig.KNOWN_POLICY_ENTRY_KINDS.contains(policyEntry.getKind())) {
            throw new InvalidPolicyConfigException(
                "Node policyentry has unrecognized kind " + policyEntry.getKind());
        }

        policyEntry.setValue(getAttributeStringValue(attributes, "value"));
        if (!checkPolicyValue(policyEntry.getKind(), policyEntry.getValue())) {
            throw new InvalidPolicyConfigException("Node policyentry has invalid value "
                + policyEntry.getValue() + " for kind " + policyEntry.getKind());
        }

        for (int i = 0; i < attributes.getLength(); ++i) {
            Node attrNode = attributes.item(i);
            if (attrNode.getNodeName().equals("kind") || attrNode.getNodeName().equals("value")) {
                continue;
            }

            policyEntry.addOther(attrNode.getNodeName(), attrNode.getNodeValue());
        }

        return policyEntry;
    }

    private static List<PolicyConfig.PolicyEntry> getPolicyEntriesFromDocNode(Node node)
        throws InvalidPolicyConfigException {
        if (node == null) {
            return null;
        }

        NodeList policyEntryNodes = ((Element) node).getElementsByTagName("policyentry");
        List<PolicyConfig.PolicyEntry> policyEntries =
            new ArrayList<>(policyEntryNodes.getLength());

        for (int i = 0; i < policyEntryNodes.getLength(); ++i) {
            policyEntries.add(getPolicyEntryFromDocNode(policyEntryNodes.item(i)));
        }

        return policyEntries.isEmpty() ? null : policyEntries;
    }

    private static void getAttributesFromPolicyNode(Node node, PolicyConfig policyConfig) {
        NamedNodeMap attributes = node.getAttributes();
        if (containsAttribute(attributes, "exclude")) {
            String excludeStr = getAttributeStringValue(attributes, "exclude");
            policyConfig.setExclude(Boolean.valueOf(excludeStr));
        }

        if (containsAttribute(attributes, "limit")) {
            String limitStr = getAttributeStringValue(attributes, "limit");
            assert limitStr != null;
            try {
                policyConfig.setLimit(Integer.valueOf(limitStr));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (containsAttribute(attributes, "pattern")) {
            String patternStr = getAttributeStringValue(attributes, "pattern");
            policyConfig.setGeneralPattern(patternStr);
        }
    }

    public static PolicyConfig buildPolicyConfig(final Document document)
        throws InvalidPolicyConfigException {
        document.getDocumentElement().normalize();

        NodeList remoteControllerNodeList = document.getElementsByTagName("remote-controller");
        if (remoteControllerNodeList.getLength() > 1) {
            throw new InvalidPolicyConfigException(
                "There must be only one remote controller node in document!");
        }

        PolicyConfig.RemoteController remoteController = null;
        if (remoteControllerNodeList.getLength() == 1) {
            remoteController = getRemoteControllerFromDocNode(remoteControllerNodeList.item(0));
        }

        NodeList policyNodeList = document.getElementsByTagName("policy");

        if (policyNodeList.getLength() > 1) {
            throw new InvalidPolicyConfigException(
                "There must be only one policy node in document!");
        } else if (policyNodeList.getLength() == 1) {
            Node policyNode = policyNodeList.item(0);
            PolicyConfig policyConfig =
                new PolicyConfig(remoteController, getPolicyEntriesFromDocNode(policyNode));
            getAttributesFromPolicyNode(policyNode, policyConfig);
            return policyConfig;
        } else {
            return new PolicyConfig(remoteController, null);
        }
    }

    public static PolicyConfig  buildPolicyConfig(final URL source)
        throws IOException, InvalidPolicyConfigException {
        try (InputStream is = source.openStream()) {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            return buildPolicyConfig(document);
        } catch (ParserConfigurationException | SAXException e) {
            throw new InvalidPolicyConfigException(
                "Can not parse xml config file " + source.toString() + e.toString());
        }
    }

    private static void setRemoteController(
        Document document, Element parent, PolicyConfig policyConfig) {
        if (policyConfig.getRemoteController() == null)
            return;

        PolicyConfig.RemoteController remoteController = policyConfig.getRemoteController();
        Element rc = document.createElement("remote-controller");
        rc.setAttribute("enabled", String.valueOf(remoteController.isEnabled()));
        rc.setAttribute("address", remoteController.getAddress());
        rc.setAttribute("port", String.valueOf(remoteController.getPort()));
        parent.appendChild(rc);
    }

    private static void setPolicyEntries(
        Document document, Element parent, PolicyConfig policyConfig) {
        Element policyNode = document.createElement("policy");
        policyNode.setAttribute("exclude", String.valueOf(policyConfig.isExclude()));
        policyNode.setAttribute("limit", String.valueOf(policyConfig.getLimit()));
        if (policyConfig.getGeneralPattern() != null) {
            policyNode.setAttribute("pattern", policyConfig.getGeneralPattern());
        }
        parent.appendChild(policyNode);

        if (policyConfig.getPolicyEntries() != null) {
            for (PolicyConfig.PolicyEntry policyEntry : policyConfig.getPolicyEntries()) {
                Element policyEntryNode = document.createElement("policyentry");
                policyEntryNode.setAttribute("kind", policyEntry.getKind());
                policyEntryNode.setAttribute("value", policyEntry.getValue());
                if (policyEntry.getOthers() != null) {
                    for (Map.Entry<String, String> entry : policyEntry.getOthers().entrySet()) {
                        policyEntryNode.setAttribute(entry.getKey(), entry.getValue());
                    }
                }
                policyNode.appendChild(policyEntryNode);
            }
        }
    }

    public static Document buildDocument(final PolicyConfig policyConfig)
        throws ParserConfigurationException {
        if (policyConfig == null) {
            return null;
        }

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element root = document.createElement("fixeh");
        document.appendChild(root);

        // Set document according to PolicyConfig
        setRemoteController(document, root, policyConfig);
        setPolicyEntries(document, root, policyConfig);

        return document;
    }

    public static void saveDocToFile(Document doc, File file) throws TransformerException {
        DOMSource src = new DOMSource(doc);
        StreamResult res = new StreamResult(file);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(src, res);
    }
}
