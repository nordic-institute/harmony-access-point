package eu.domibus.core.ebms3.ws.policy;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Security;
import eu.domibus.core.cxf.DomibusBus;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyBuilderImpl;
import org.apache.neethi.Policy;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author Arun Raj
 * @since 3.3
 */
@RunWith(JMockit.class)
public class PolicyServiceImplTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PolicyServiceImplTest.class);
    private static final String TEST_RESOURCES_DIR = "./src/test/resources/policy";

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    DomibusBus busCore;

    @Tested
    PolicyServiceImpl policyService;

    @Test
    public void testIsNoSecurityPolicy_NullPolicy() {
        //when null policy is specified
        boolean result1 = policyService.isNoSecurityPolicy(null);
        assertTrue("Expected NoSecurityPolicy as true when null input provided", result1);
    }

    @Test
    public void testIsNoSecurityPolicy_DoNothingPolicy(@Injectable final Policy doNothingPolicy) {
        //when doNothingPolicy.xml is specified
        new Expectations() {{
            doNothingPolicy.isEmpty();
            result = true;
        }};

        boolean result2 = policyService.isNoSecurityPolicy(doNothingPolicy);
        assertTrue(result2);
    }

    @Test
    public void testIsNoSecurityPolicy_SignOnPolicy(@Injectable final Policy signOnlyPolicy) {
        new Expectations() {{
            signOnlyPolicy.isEmpty();
            result = false;
        }};

        boolean result3 = policyService.isNoSecurityPolicy(signOnlyPolicy);
        assertFalse(result3);
    }

    @Test
    public void testParsePolicyException() {

        try {
            policyService.parsePolicy("NonExistentFileLocation");
            fail();
        } catch (Exception e) {
            assertTrue("Expecting ConfigurationException", e instanceof ConfigurationException);
        }
    }

    @Test
    public void testSignOnlyPolicy() {
        PolicyBuilder pb = new PolicyBuilderImpl();
        new Expectations() {{
            busCore.getExtension(PolicyBuilder.class);
            result = pb;
            domibusConfigurationService.getConfigLocation();
            result = ".";
        }};

        Policy policy = policyService.parsePolicy(TEST_RESOURCES_DIR + "/eDeliveryAS4Policy_test_signOnly.xml");
        assertTrue(policyService.isNoEncryptionPolicy(policy));
        policy = policyService.parsePolicy(TEST_RESOURCES_DIR + "/eDeliveryAS4Policy_test.xml");
        assertFalse(policyService.isNoEncryptionPolicy(policy));
        policy = policyService.parsePolicy(TEST_RESOURCES_DIR + "/eDeliveryAS4Policy_test_donothing.xml");
        assertTrue(policyService.isNoEncryptionPolicy(policy));
    }

    @Test
    public void testGetPolicy() {
        LegConfiguration legConfiguration = new LegConfiguration();
        Security securityPolicy = new Security();
        securityPolicy.setPolicy("NonExistentPolicy");
        legConfiguration.setSecurity(securityPolicy);

        try {
            policyService.getPolicy(legConfiguration);
            fail();
        } catch (Exception e) {
            assertTrue("Expecting ConfigurationException", e instanceof ConfigurationException);
        }
    }

}
