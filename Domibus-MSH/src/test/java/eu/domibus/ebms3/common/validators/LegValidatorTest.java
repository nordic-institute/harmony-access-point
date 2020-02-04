//package eu.domibus.ebms3.common.validators;
//
//import eu.domibus.api.pmode.PModeIssue;
//import eu.domibus.common.model.configuration.BusinessProcesses;
//import eu.domibus.common.model.configuration.Configuration;
//import eu.domibus.common.model.configuration.LegConfiguration;
//import eu.domibus.common.model.configuration.Service;
//import eu.domibus.core.pmode.validation.validators.LegValidator;
//import mockit.Expectations;
//import mockit.Mocked;
//import mockit.Tested;
//import mockit.integration.junit4.JMockit;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.util.Collections;
//import java.util.List;
//
///**
// * @author Catalin Enache
// * @since 4.1.2
// */
//@RunWith(JMockit.class)
//public class LegValidatorTest {
//
//    @Tested
//    LegValidator legValidator;
//
//    @Test
//    public void test_validate(final @Mocked Configuration configuration, final @Mocked BusinessProcesses businessProcesses) {
//
//        final String legConfigurationName = "testConfiguration";
//        final LegConfiguration legConfiguration = new LegConfiguration();
//        final Service service = new Service();
//        service.setName("testService");
//        service.setValue("testServiceValue");
//        legConfiguration.setName(legConfigurationName);
//        legConfiguration.setService(service);
//
//        new Expectations() {{
//            configuration.getBusinessProcesses();
//            result = businessProcesses;
//
//            businessProcesses.getLegConfigurations();
//            result = Collections.singleton(legConfiguration);
//        }};
//
//        //tested method
//        final List<PModeIssue> results = legValidator.validate(configuration);
//
//        Assert.assertNotNull(results);
//        Assert.assertTrue(results.size() == 6);
//        Assert.assertTrue(results.stream().anyMatch(el -> el.getMessage().equals("Invalid action specified for leg configuration [" + legConfigurationName + "]")));
//        Assert.assertTrue(results.stream().anyMatch(el -> el.getMessage().equals("Invalid security specified for leg configuration [" + legConfigurationName + "]")));
//        Assert.assertTrue(results.stream().anyMatch(el -> el.getMessage().equals("Invalid defaultMpc specified for leg configuration [" + legConfigurationName + "]")));
//        Assert.assertTrue(results.stream().anyMatch(el -> el.getMessage().equals("Invalid receptionAwareness specified for leg configuration [" + legConfigurationName + "]")));
//        Assert.assertTrue(results.stream().anyMatch(el -> el.getMessage().equals("Invalid reliability specified for leg configuration [" + legConfigurationName + "]")));
//        Assert.assertTrue(results.stream().anyMatch(el -> el.getMessage().equals("Invalid errorHandling specified for leg configuration [" + legConfigurationName + "]")));
//    }
//
//}