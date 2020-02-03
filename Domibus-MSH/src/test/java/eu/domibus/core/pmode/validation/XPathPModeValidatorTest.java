//package eu.domibus.core.pmode.validation;
//
//import eu.domibus.api.pmode.PModeIssue;
//import mockit.Tested;
//import mockit.integration.junit4.JMockit;
//import org.apache.commons.io.IOUtils;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.List;
//
//@RunWith(JMockit.class)
//public class XPathPModeValidatorTest {
//
//    @Tested
//    XPathPModeValidator xPathPModeValidator = new XPathPModeValidator(
//            "//configuration/@party",
//            "//businessProcesses/parties/party/@name",
//            "Party [%s] not found in business process parties.");
//
//    @Test
//    public void validateAsXml_NoIssues() throws IOException {
//        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream("samplePModes/domibus-pmode-validation-tests-issues.xml");
//        byte[] pModeBytes = IOUtils.toByteArray(xmlStream);
//
//        List<PModeIssue> issues = xPathPModeValidator.validateAsXml(pModeBytes);
//
//        Assert.assertTrue(issues.size() == 1);
//    }
//}