package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.IssueLevel;
import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.xml.DomibusXMLException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class XPathPModeValidator extends AbstractPModeValidator {

    private String targetExpression; // = "//businessProcesses/legConfigurations/legConfiguration/@service";
    private String acceptedValuesExpression; // = "//businessProcesses/services/service/@name";
    private IssueLevel level = IssueLevel.WARNING;

    private Document xmlDocument;

    public XPathPModeValidator(String targetExpression, String acceptedValuesExpression) {
        this.targetExpression = targetExpression;
        this.acceptedValuesExpression = acceptedValuesExpression;
    }

    public List<PModeIssue> validateAsXml(byte[] xmlBytes) {

        parseXml(xmlBytes);

        List<String> valuesToValidate = extractValues(targetExpression);
        List<String> acceptedValues = extractValues(acceptedValuesExpression);

        List<PModeIssue> issues = new ArrayList<>();
        for (String value : valuesToValidate) {
            if (!acceptedValues.contains(value)) {
                issues.add(new PModeIssue("Service [" + value + "] not found", level));
            }
        }

        return issues;
    }

    private List<String> extractValues(String expression) {
        XPathExpression xPathExpr;
        try {
            xPathExpr = XPathFactory.newInstance().newXPath().compile(expression);
        } catch (XPathExpressionException xe) {
            throw new DomibusXMLException("Invalid xpath expression: " + expression, xe);
        }

        try {
            List<String> values = new ArrayList<>();
            NodeList nodeList = (NodeList) xPathExpr.evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                values.add(node.getNodeValue());
            }
            return values;
        } catch (XPathExpressionException xe) {
            throw new DomibusXMLException("Could not evaluate xpath: " + expression, xe);
        }
    }

    private void parseXml(byte[] xmlBytes) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            InputStream inputStream = new ByteArrayInputStream(xmlBytes);
            this.xmlDocument = builder.parse(inputStream);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new DomibusXMLException("Invalid xml document", e);
        }
    }
}
