package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.AbstractIT;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.test.common.PKIUtil;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.SMPServiceMetadata;
import eu.europa.ec.dynamicdiscovery.model.identifiers.SMPProcessIdentifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.w3c.dom.Document;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_KEYSTORE_NAME;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

/**
 * @author François Gautier
 * @version 5.1
 * @since 27-12-22
 */
@DirtiesContext
public class DynamicDiscoveryServiceOASISIT extends AbstractIT {


    private static SMPServiceMetadata serviceMetadata;
    private static SMPServiceMetadata expired_serviceMetadata;
    private static SMPServiceMetadata emptyCertificate_serviceMetadata;

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public DynamicDiscoveryServiceOASIS dynamicDiscoveryServiceOASIS() {
            return new DynamicDiscoveryServiceOASIS() {
                @Override
                protected SMPServiceMetadata getServiceMetadata(
                        String participantId,
                        String participantIdScheme,
                        String documentId,
                        String processId,
                        String processIdScheme) throws TechnicalException {
                    if (StringUtils.contains(participantId, "expired")) {
                        return expired_serviceMetadata;
                    }
                    if (StringUtils.contains(participantId, "empty_cert")) {
                        return emptyCertificate_serviceMetadata;
                    }
                    return serviceMetadata;
                }

            };
        }
    }

    @Autowired
    private DynamicDiscoveryServiceOASIS dynamicDiscoveryServiceOASIS;

    @BeforeClass
    public static void beforeClass() throws Exception {

        PKIUtil pkiUtil = new PKIUtil();
        final X509Certificate expiredCertificate = pkiUtil.createCertificateWithSubject(BigInteger.valueOf(300L), "CN=" + "party2" + ",OU=Domibus,O=eDelivery,C=EU", DateUtils.addDays(Calendar.getInstance().getTime(), -30), DateUtils.addDays(Calendar.getInstance().getTime(), -20));

        expired_serviceMetadata = DynamicDiscoveryFactoryTest.buildServiceMetadata(
                new SMPProcessIdentifier("urn:epsosPatientService::List", "ehealth-procid-qns"),
                "bdxr-transport-ebms3-as4-v1p0",
                "http://localhost",
                expiredCertificate,
                "participantId_expired");

        emptyCertificate_serviceMetadata = DynamicDiscoveryFactoryTest.buildServiceMetadata(
                new SMPProcessIdentifier("urn:epsosPatientService::List", "ehealth-procid-qns"),
                "bdxr-transport-ebms3-as4-v1p0",
                "http://localhost",
                null,
                "participantId_empty_cert");
    }

    @Before
    public void setUp() throws Exception {
        uploadPmode(SERVICE_PORT);
        createStore(DOMIBUS_TRUSTSTORE_NAME, "keystores/gateway_truststore.jks");
        createStore(DOMIBUS_KEYSTORE_NAME, "keystores/gateway_keystore.jks");
    }

    @Test(expected = AuthenticationException.class)
    public void lookupInformation_expired() throws EbMS3Exception {
        dynamicDiscoveryServiceOASIS.lookupInformation("domain",
                "participantId_expired",
                "participantIdScheme",
                "scheme::value",
                "urn:epsosPatientService::List",
                "ehealth-procid-qns");
    }

    @Test(expected = ConfigurationException.class)
    public void lookupInformation_emptyCert() throws EbMS3Exception {
        dynamicDiscoveryServiceOASIS.lookupInformation("domain",
                "participantId_empty_cert",
                "participantIdScheme",
                "scheme::value",
                "urn:epsosPatientService::List",
                "ehealth-procid-qns");
    }
}
