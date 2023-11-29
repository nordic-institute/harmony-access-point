package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.message.UserMessageServiceHelper;
import eu.domibus.logging.DomibusLogger;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static eu.domibus.test.common.PKIUtil.*;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class AbstractDynamicDiscoveryServiceTest {

    @Tested
    AbstractDynamicDiscoveryService testInstance;

    @Injectable
    DomibusLogger logger;

    @Injectable
    UserMessageServiceHelper userMessageServiceHelper;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Test
    public void testGetAllowedSMPCertificatePolicyOIDsPropertyNotDefined() {

        new Expectations(testInstance) {{
            testInstance.getDynamicDiscoveryUtil().getTrimmedDomibusProperty(anyString);
            result = null;

            testInstance.getLogger();
            result = logger;
        }};

        List<String> result = testInstance.getAllowedSMPCertificatePolicyOIDs();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        new FullVerifications(testInstance) {{
            String logTemplate;
            String parameter;
            logger.debug(logTemplate = withCapture(), parameter = withCapture());

            Assert.assertEquals("The value for property [{}] is empty.", logTemplate);
            Assert.assertEquals(DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION, parameter);
            // test call correct property
            String property;
            testInstance.getDynamicDiscoveryUtil().getTrimmedDomibusProperty(property = withCapture());
            Assert.assertEquals(DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION, property);
        }};
    }

    @Test
    public void testGetAllowedSMPCertificatePolicyOIDsPropertyWithOne() {
        new Expectations(testInstance) {{
            testInstance.getDynamicDiscoveryUtil().getTrimmedDomibusProperty(anyString);
            result = CERTIFICATE_POLICY_QCP_NATURAL;
        }};

        List<String> result = testInstance.getAllowedSMPCertificatePolicyOIDs();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CERTIFICATE_POLICY_QCP_NATURAL, result.get(0));
    }

    @Test
    public void testGetAllowedSMPCertificatePolicyOIDsPropertyWithMultipleAndSpaces() {
        new Expectations(testInstance) {{
            testInstance.getDynamicDiscoveryUtil().getTrimmedDomibusProperty(anyString);
            result = CERTIFICATE_POLICY_QCP_NATURAL
                    + "," + CERTIFICATE_POLICY_QCP_LEGAL
                    + ", " + CERTIFICATE_POLICY_QCP_NATURAL_QSCD
                    + " ,     " + CERTIFICATE_POLICY_QCP_LEGAL_QSCD;
        }};

        List<String> result = testInstance.getAllowedSMPCertificatePolicyOIDs();

        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals(CERTIFICATE_POLICY_QCP_NATURAL, result.get(0));
        assertEquals(CERTIFICATE_POLICY_QCP_LEGAL, result.get(1));
        assertEquals(CERTIFICATE_POLICY_QCP_NATURAL_QSCD, result.get(2));
        assertEquals(CERTIFICATE_POLICY_QCP_LEGAL_QSCD, result.get(3));
    }


    @Test
    public void testGetPartyIdTypeTestGetDefaultForNullProperty() {
        new Expectations(testInstance) {{
            testInstance.getPartyIdTypePropertyName();
            result = DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_PARTYID_RESPONDER_ROLE;

            testInstance.getDynamicDiscoveryUtil().getTrimmedDomibusProperty(DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_PARTYID_RESPONDER_ROLE);
            result = " ";
        }};

        String partyIdType = testInstance.getPartyIdType();
        assertNull(partyIdType);
    }

    @Test
    public void testGetPartyIdTypeFromProperty() {
        final String URN_TYPE_VALUE = "TEST_PARTY_TYPE_FROM_PROPERTY";
        new Expectations(testInstance) {{
            testInstance.getPartyIdTypePropertyName();
            result = DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_PARTYID_TYPE;

            testInstance.getDynamicDiscoveryUtil().getTrimmedDomibusProperty(DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_PARTYID_TYPE);
            result = URN_TYPE_VALUE;
        }};

        String partyIdType = testInstance.getPartyIdType();
        Assert.assertEquals(partyIdType, URN_TYPE_VALUE);
    }

    @Test
    public void testGetResponderRoleFromProperty() {
        final String RESPONDER_ROLE = "RESPONDER_ROLE_FROM_PROPERTY";
        new Expectations(testInstance) {{
            testInstance.getPartyIdResponderRolePropertyName();
            result = DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_PARTYID_RESPONDER_ROLE;

            testInstance.getDynamicDiscoveryUtil().getTrimmedDomibusProperty(DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_PARTYID_RESPONDER_ROLE);
            result = RESPONDER_ROLE;
        }};

        String partyIdType = testInstance.getResponderRole();

        Assert.assertEquals(partyIdType, RESPONDER_ROLE);
    }

    @Test
    public void testIsValidEndpointForNullPeriod() {

        boolean result = testInstance.isValidEndpoint(null, null);

        assertTrue("If period is not given the endpoint must be considered as valid!", result);
    }

    @Test
    public void testIsValidEndpointForValidPeriod() {
        Date currentDate = Calendar.getInstance().getTime();

        boolean result = testInstance.isValidEndpoint(DateUtils.addDays(currentDate,-1), DateUtils.addDays(currentDate,+1));

        assertTrue(result);
    }

    @Test
    public void testIsValidEndpointForNotYetValid() {
        Date currentDate = Calendar.getInstance().getTime();
        Date fromDate =DateUtils.addDays(currentDate,1);
        new Expectations(testInstance) {{
            testInstance.getLogger();
            result = logger;
        }};

        boolean result = testInstance.isValidEndpoint(fromDate, DateUtils.addDays(currentDate,2));
        assertFalse(result);
        // test log the cause for not valid period
        new FullVerifications(testInstance) {{
            String logTemplate;
            String parameter;
            logger.warn(logTemplate = withCapture(), parameter = withCapture());

            Assert.assertEquals("Found endpoint which is not yet activated! Endpoint's activation date: [{}]!", logTemplate);
            Assert.assertEquals(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(fromDate), parameter);
        }};
    }

    @Test
    public void testIsValidEndpointForExpired() {
        Date currentDate = Calendar.getInstance().getTime();
        Date toDate =DateUtils.addDays(currentDate,-1);
        new Expectations(testInstance) {{
            testInstance.getLogger();
            result = logger;
        }};

        boolean result = testInstance.isValidEndpoint(DateUtils.addDays(currentDate,-2), toDate);
        assertFalse(result);
        // test log the cause for not valid periode
        new FullVerifications(testInstance) {{
            String logTemplate;
            String parameter;
            logger.warn(logTemplate = withCapture(), parameter = withCapture());

            Assert.assertEquals("Found endpoint, which is expired! Endpoint's expiration date: [{}]!", logTemplate);
            Assert.assertEquals(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(toDate), parameter);
        }};
    }
}
