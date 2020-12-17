//package eu.domibus.core.pmode.validation;
//
//import eu.domibus.api.pmode.PModeIssue;
//import eu.domibus.common.model.configuration.Configuration;
//import mockit.*;
//import mockit.integration.junit4.JMockit;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//@RunWith(JMockit.class)
//public class CompositePModeValidatorTest {
//
//    @Tested
//    CompositePModeValidator compositePModeValidator;
//
//    @Injectable
//    CompositePModeValidator innerValidator1;
//
//    @Injectable
//    CompositePModeValidator innerValidator2;
//
////    @Test
////    public void validateAsXml(@Mocked byte[] rawConfiguration, @Mocked Configuration configuration) {
////
////        List<PModeValidator> list = new ArrayList<>();
////        list.add(innerValidator1);
////        list.add(innerValidator2);
////        compositePModeValidator.setValidators(list);
////
////        PModeIssue issue1 = new PModeIssue();
////        issue1.setLevel(PModeIssue.IssueLevel.ERROR);
////        issue1.setMessage("Leg configuration is wrong");
////
////        PModeIssue issue2 = new PModeIssue();
////        issue2.setLevel(PModeIssue.IssueLevel.WARNING);
////        issue2.setMessage("Process configuration is wrong");
////
////        List<PModeIssue> issues1 = Arrays.asList(issue1);
////        List<PModeIssue> issues2 = Arrays.asList(issue2);
////
////        new Expectations() {{
////            innerValidator1.validateAsXml(rawConfiguration);
////            result = issues1;
////            innerValidator2.validateAsXml(rawConfiguration);
////            result = issues2;
////        }};
////
////        List<PModeIssue> res = compositePModeValidator.validateAsXml(rawConfiguration);
////
////        new Verifications() {{
////            innerValidator1.validateAsXml(rawConfiguration);
////            times = 1;
////            innerValidator2.validateAsXml(rawConfiguration);
////            times = 1;
////            innerValidator1.validateAsConfiguration(configuration);
////            times = 0;
////        }};
////
////        Assert.assertTrue(res.size() == 2);
////        Assert.assertTrue(res.get(0) == issue1);
////    }
//
//    @Test
//    public void validateAsConfiguration(@Mocked byte[] rawConfiguration, @Mocked Configuration configuration) {
//        List<PModeValidator> list = new ArrayList<>();
//        list.add(innerValidator1);
//        list.add(innerValidator2);
//        compositePModeValidator.setValidators(list);
//
//        PModeIssue issue1 = new PModeIssue();
//        issue1.setLevel(PModeIssue.Level.ERROR);
//        issue1.setMessage("Leg configuration is wrong");
//
//        PModeIssue issue2 = new PModeIssue();
//        issue2.setLevel(PModeIssue.Level.WARNING);
//        issue2.setMessage("Process configuration is wrong");
//
//        List<PModeIssue> issues1 = Arrays.asList(issue1);
//        List<PModeIssue> issues2 = Arrays.asList(issue2);
//
//        new Expectations() {{
//            innerValidator1.validate(configuration);
//            result = issues1;
//            innerValidator2.validate(configuration);
//            result = issues2;
//        }};
//
//        List<PModeIssue> res = compositePModeValidator.validate(configuration);
//
//        new Verifications() {{
//            innerValidator1.validate(configuration);
//            times = 1;
//            innerValidator2.validate(configuration);
//            times = 1;
////            innerValidator1.validateAsXml(rawConfiguration);
////            times = 0;
//        }};
//
//        Assert.assertTrue(res.size() == 2);
//        Assert.assertTrue(res.get(0) == issue1);
//    }
//}