package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.test.common.PKIUtil;
import eu.europa.ec.dynamicdiscovery.model.SMPServiceMetadata;
import eu.europa.ec.dynamicdiscovery.model.identifiers.SMPProcessIdentifier;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
@Configuration
public class DynamicDiscoveryServicePEPPOLConfigurationMockup {

    public static final Domain DOMAIN = new Domain("default", "default");

    public static final String FINAL_RECIPIENT1 = "0208:1111";
    public static final String FINAL_RECIPIENT2 = "0208:2222";
    public static final String FINAL_RECIPIENT3 = "0208:3333";
    public static final String FINAL_RECIPIENT4 = "0208:4444";
    public static final String FINAL_RECIPIENT5 = "0208:5555";
    public static final String FINAL_RECIPIENT_MULTIPLE_THREADS_FORMAT = "9925:%s";

    public static final String PARTY_NAME1 = "party1";
    public static final Long PARTY_NAME1_CERTIFICATE_SERIAL_NUMBER = 100L;
    public static final String PARTY_NAME_MULTIPLE_THREADS_CERTIFICATE_SERIAL_NUMBER_FORMAT = "1000%s";
    public static final String PARTY_NAME2 = "party2";
    public static final String PARTY_NAME3 = "party3";

    public static final String PARTY_NAME4 = "party4";

    public static final String PARTY_NAME5 = "partyConfiguredInPmode";

    public static final String PARTY_NAME_MULTIPLE_THREADS_FORMAT = "threadParty%s";
    public static final Long PARTY_NAME2_CERTIFICATE_SERIAL_NUMBER = 200L;
    public static final String TRANSPORT_PROFILE = "peppol-transport-as4-v2_0";
    public static final SMPProcessIdentifier PROCESS_IDENTIFIER = new SMPProcessIdentifier("bdx:noprocess", "tc1");

    public static Map<String, FinalRecipientConfiguration> participantConfigurations = new HashMap<>();

    @Bean
    public Domain myDomain() {
        return DOMAIN;
    }


    @Primary
    @Bean
    public DynamicDiscoveryServicePEPPOL dynamicDiscoveryServicePEPPOL() {
        PKIUtil pkiUtil = new PKIUtil();
        //we create the certificate for party 1 and party 2 Access Points
        final X509Certificate party1Certificate = pkiUtil.createCertificateWithSubject(BigInteger.valueOf(PARTY_NAME1_CERTIFICATE_SERIAL_NUMBER), "CN=" + PARTY_NAME1 + ",OU=Domibus,O=eDelivery,C=EU");
        final X509Certificate party2Certificate = pkiUtil.createCertificateWithSubject(BigInteger.valueOf(PARTY_NAME2_CERTIFICATE_SERIAL_NUMBER), "CN=" + PARTY_NAME2 + ",OU=Domibus,O=eDelivery,C=EU");
        final X509Certificate expiredCertificate = pkiUtil.createCertificateWithSubject(BigInteger.valueOf(300L), "CN=" + PARTY_NAME3 + ",OU=Domibus,O=eDelivery,C=EU", DateUtils.addDays(Calendar.getInstance().getTime(), -30), DateUtils.addDays(Calendar.getInstance().getTime(), -20));
        final X509Certificate party4Certificate = pkiUtil.createCertificateWithSubject(BigInteger.valueOf(PARTY_NAME2_CERTIFICATE_SERIAL_NUMBER), "CN=" + PARTY_NAME4 + ",OU=Domibus,O=eDelivery,C=EU");

        //final recipient 1 and 2 are configured on party1 Access Point
        addParticipantConfiguration(FINAL_RECIPIENT1, PARTY_NAME1, TRANSPORT_PROFILE, PROCESS_IDENTIFIER, party1Certificate);
        addParticipantConfiguration(FINAL_RECIPIENT2, PARTY_NAME1, TRANSPORT_PROFILE, PROCESS_IDENTIFIER, party1Certificate);

        //final recipient 3 is configured on party2 Access Point
        addParticipantConfiguration(FINAL_RECIPIENT3, PARTY_NAME2, TRANSPORT_PROFILE, PROCESS_IDENTIFIER, party2Certificate);

        //final recipient 4 is configured on party3 Access Point; certificate is expired
        addParticipantConfiguration(FINAL_RECIPIENT4, PARTY_NAME3, TRANSPORT_PROFILE, PROCESS_IDENTIFIER, expiredCertificate);

        //final recipient 5 is configured on party4 Access Point
        addParticipantConfiguration(FINAL_RECIPIENT5, PARTY_NAME4, TRANSPORT_PROFILE, PROCESS_IDENTIFIER, party4Certificate);

        return new DynamicDiscoveryServicePEPPOL() {

            //String participantId, String participantIdScheme, String documentId, String processId, String processIdScheme
            @Override
            protected SMPServiceMetadata getServiceMetadata(
                    String finalRecipient,
                    String finalRecipientScheme,
                    String documentId,
                    String processId,
                    String processIdScheme) {
                final FinalRecipientConfiguration configuration = participantConfigurations.get(finalRecipient);
                if (configuration == null) {
                    throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not find the final recipient configuration for final recipient [" + finalRecipient + "]");
                }
                return configuration.getServiceMetadata();
            }

        };
    }

    public static void addParticipantConfiguration(String finalRecipient,
                                                   String partyName,
                                                   String transportProfile,
                                                   SMPProcessIdentifier processIdentifier,
                                                   X509Certificate certificate) {
        final String accessPointUrl = DynamicDiscoveryFactoryTest.getAccessPointUrl(partyName);
        SMPServiceMetadata serviceMetadata = DynamicDiscoveryFactoryTest.buildServiceMetadata(
                processIdentifier,
                transportProfile,
                accessPointUrl,
                certificate,
                finalRecipient);
        FinalRecipientConfiguration configuration = new FinalRecipientConfiguration(certificate, serviceMetadata, partyName);
        participantConfigurations.put(finalRecipient, configuration);
    }
}
