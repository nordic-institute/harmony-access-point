//package eu.domibus.core.pmode.validation;
//
//import eu.domibus.api.property.DomibusPropertyProvider;
//import mockit.Expectations;
//import mockit.Injectable;
//import mockit.Tested;
//import mockit.integration.junit4.JMockit;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;
//
//import static eu.domibus.core.pmode.validation.ConfigurablePModeValidator.STRING_PREDICATE;
//
//@RunWith(JMockit.class)
//public class ConfigurablePModeValidatorTest {
//
//    private static final String DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR_SELF_PARTY = "domibus.pMode.validation.xPathValidator.SelfParty";
//    private static final String DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR_LEG_SPLITTING = "domibus.pMode.validation.xPathValidator.LegSplitting";
//
//    @Tested
//    ConfigurablePModeValidator configurablePModeValidator;
//
//    @Injectable
//    DomibusPropertyProvider domibusPropertyProvider;
//
//    @Test
//    public void readConfigurationAndCreateValidators() {
//
//        Set<String> propNames = new HashSet<>(Arrays.asList(DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR_SELF_PARTY, DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR_LEG_SPLITTING));
//
//        new Expectations() {{
//            domibusPropertyProvider.getPropertyNames(STRING_PREDICATE);
//            result = propNames;
//
//            domibusPropertyProvider.getProperty(DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR_SELF_PARTY);
//            result = "//configuration/@party;//businessProcesses/parties/party/@name;ERROR;Party [%s] not found in business process parties.";
//
//            domibusPropertyProvider.getProperty(DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR_LEG_SPLITTING);
//            result = "//legConfigurations/legConfiguration/@splitting[string-length()>0];//businessProcesses/splittingConfigurations/splitting/@name;ERROR;Leg splitting [%s] not found in splitting configurations.";
//
//        }};
//
//        configurablePModeValidator.readConfigurationAndCreateValidators();
//
//        Assert.assertTrue(configurablePModeValidator.getValidators().size() == 2);
//    }
//
//    @Test
//    public void readConfigurationAndCreateValidators_IgnoreWrongConfig() {
//
//        Set<String> propNames = new HashSet<>(Arrays.asList(DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR_SELF_PARTY, DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR_LEG_SPLITTING));
//
//        new Expectations() {{
//            domibusPropertyProvider.getPropertyNames(STRING_PREDICATE);
//            result = propNames;
//
//            domibusPropertyProvider.getProperty(DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR_SELF_PARTY);
//            result = "//configuration/@party;//businessProcesses/parties/party/@name;ERROR;Party [%s] not found in business process parties.";
//
//            domibusPropertyProvider.getProperty(DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR_LEG_SPLITTING);
//            result = "//legConfigurations/legConfiguration/@splitting[string-length()>0]";
//
//        }};
//
//        configurablePModeValidator.readConfigurationAndCreateValidators();
//
//        Assert.assertTrue(configurablePModeValidator.getValidators().size() == 1);
//    }
//}