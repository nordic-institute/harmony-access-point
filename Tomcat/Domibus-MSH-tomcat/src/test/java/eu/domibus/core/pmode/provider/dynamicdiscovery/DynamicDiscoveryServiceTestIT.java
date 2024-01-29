package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.AbstractIT;
import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.dynamicdyscovery.DynamicDiscoveryLookupEntity;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.model.*;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.certificate.CertificateTestUtils;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.jms.JMSManagerImpl;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.core.pmode.multitenancy.MultiDomainPModeProvider;
import eu.domibus.core.util.DateUtilImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.ProcessingType;
import eu.domibus.test.common.PKIUtil;
import eu.europa.ec.dynamicdiscovery.model.identifiers.SMPProcessIdentifier;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.jms.Topic;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryServicePEPPOLConfigurationMockup.*;
import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
@DirtiesContext
public class DynamicDiscoveryServiceTestIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryServiceTestIT.class);
    public static final String DYNAMIC_DISCOVERY_PMODE_WITH_SIGN_AND_ENCRYPTION = "dataset/pmode/PModeDynamicDiscovery.xml";
    public static final String DYNAMIC_DISCOVERY_PMODE_WITH_SIGN_ONLY = "dataset/pmode/PModeDynamicDiscoverySignOnly.xml";

    public static final String CERTIFICATE_POLICY_ANY = "2.5.29.32.0";
    public static final String CERTIFICATE_POLICY_QCP_NATURAL = "0.4.0.194112.1.0";
    public static final String CERTIFICATE_POLICY_QCP_LEGAL = "0.4.0.194112.1.1";
    public static final String CERTIFICATE_POLICY_QCP_NATURAL_QSCD = "0.4.0.194112.1.2";
    public static final String CERTIFICATE_POLICY_QCP_LEGAL_QSCD = "0.4.0.194112.1.3";

    @Autowired
    MultiDomainPModeProvider multiDomainPModeProvider;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    DynamicDiscoveryLookupDao dynamicDiscoveryLookupDao;

    @Autowired
    DynamicDiscoveryLookupService dynamicDiscoveryLookupService;

    @Autowired
    private MultiDomainCryptoServiceImpl multiDomainCryptoService;

    @Autowired
    SignalService signalService;

    @Autowired
    DynamicDiscoveryDeleteFinalRecipientsFromCacheCommandTask dynamicDiscoveryDeleteFinalRecipientsFromCacheCommandTask;

    @Autowired
    DynamicDiscoveryDeletePmodePartiesCommandTask dynamicDiscoveryDeletePmodePartiesCommandTask;

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    @Autowired
    CertificateHelper certificateHelper;

    @Autowired
    DynamicDiscoveryAssertionUtil dynamicDiscoveryAssertionUtil;

    @Autowired
    CertificateTestUtils certificateTestUtils;

    @Autowired
    DynamicDiscoveryServicePEPPOL dynamicDiscoveryService;

    @Autowired
    DomainContextProvider domainContextProvider;

    @Configuration
    static class ContextConfiguration {

    }

    @Before
    public void setUp() throws Exception {
        resetInitialTruststore();
    }

    private void resetInitialTruststore() {
        try {
            Domain domain = DomainService.DEFAULT_DOMAIN;
            multiDomainCryptoService.resetTrustStore(domain);
            String storePassword = "test123";
            Path path = Paths.get(domibusConfigurationService.getConfigLocation(), "keystores", "gateway_truststore_original.jks");
            byte[] content = Files.readAllBytes(path);
            KeyStoreContentInfo storeInfo = certificateHelper.createStoreContentInfo(DOMIBUS_TRUSTSTORE_NAME, "gateway_truststore.jks", content, storePassword);
            multiDomainCryptoService.replaceTrustStore(domain, storeInfo);
        } catch (Exception e) {
            LOG.info("Error restoring initial keystore", e);
        }
    }


    private void initializePmodeAndProperties(String pmodeFilePath) throws XmlProcessingException, IOException {
        uploadPmode(SERVICE_PORT, pmodeFilePath, null);

        domibusPropertyProvider.setProperty(DOMAIN, DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, "true");
        domibusPropertyProvider.setProperty(DOMAIN, DomibusPropertyMetadataManagerSPI.DOMIBUS_DEPLOYMENT_CLUSTERED, "true");

        domibusPropertyProvider.setProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION, DynamicDiscoveryClientSpecification.PEPPOL.getName());
        domibusPropertyProvider.setProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4, "peppol-transport-as4-v2_0");
    }

    @After
    public void clean() {
        domibusPropertyProvider.setProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION, DynamicDiscoveryClientSpecification.OASIS.getName());
        domibusPropertyProvider.setProperty(DOMAIN, DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, "false");
        domibusPropertyProvider.setProperty(DOMAIN, DomibusPropertyMetadataManagerSPI.DOMIBUS_DEPLOYMENT_CLUSTERED, "false");
        domibusPropertyProvider.setProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4, "bdxr-transport-ebms3-as4-v1p0");
    }

    //start tests

    @Test
    public void lookupAndUpdateConfigurationForPartyToId() throws EbMS3Exception, SQLException, XmlProcessingException, IOException {
        initializePmodeAndProperties(DYNAMIC_DISCOVERY_PMODE_WITH_SIGN_AND_ENCRYPTION);

        certificateTestUtils.resetTruststore("keystores/gateway_truststore_dyn_disc.jks", "test123");

        //clean up
        cleanBeforeLookup();

        final String finalRecipient1PartyName = DynamicDiscoveryServicePEPPOLConfigurationMockup.participantConfigurations.get(FINAL_RECIPIENT1).getPartyName();
        LOG.info("---first lookup for final recipient [{}] and party [{}]", FINAL_RECIPIENT1, finalRecipient1PartyName);
        //we expect the party and the certificate are added
        doLookupForFinalRecipient(FINAL_RECIPIENT1, finalRecipient1PartyName, 3, 2, 1, 1);

        //we expect that one entry is added in the database for the first lookup
        final DynamicDiscoveryLookupEntity finalRecipient1PartyEntityFirstLookup = dynamicDiscoveryLookupDao.findByFinalRecipient(FINAL_RECIPIENT1);
        assertNotNull(finalRecipient1PartyEntityFirstLookup);
        assertEquals(PARTY_NAME1, finalRecipient1PartyEntityFirstLookup.getCn());
        final Date dynamicDiscoveryTimeFirstLookup = finalRecipient1PartyEntityFirstLookup.getDynamicDiscoveryTime();
        assertNotNull(dynamicDiscoveryTimeFirstLookup);
        assertNotNull(finalRecipient1PartyEntityFirstLookup.getFingerprint());
        assertNotNull(finalRecipient1PartyEntityFirstLookup.getSubject());
        assertNotNull(finalRecipient1PartyEntityFirstLookup.getSerial());

        try {
            //sleep 100 milliseconds so that the second DDC lookup time is after the first lookup
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error sleeping thread", e);
        }

        LOG.info("---second lookup for final recipient [{}] and party [{}]", FINAL_RECIPIENT1, finalRecipient1PartyName);
        //we expect that no new certificates are added in the truststore and no new parties are added in the Pmode
        doLookupForFinalRecipient(FINAL_RECIPIENT1, finalRecipient1PartyName, 3, 2, 1, 1);

        //we expect that the DDC lookup time was updated
        final DynamicDiscoveryLookupEntity finalRecipient1PartyEntitySecondLookup = dynamicDiscoveryLookupDao.findByFinalRecipient(FINAL_RECIPIENT1);
        assertNotNull(finalRecipient1PartyEntitySecondLookup);
        assertEquals(PARTY_NAME1, finalRecipient1PartyEntitySecondLookup.getCn());
        final Date dynamicDiscoveryTimeSecondLookup = finalRecipient1PartyEntitySecondLookup.getDynamicDiscoveryTime();
        assertNotNull(dynamicDiscoveryTimeSecondLookup);

        LOG.debug("first lookup [{}], second lookup [{}]", dynamicDiscoveryTimeFirstLookup, dynamicDiscoveryTimeSecondLookup);
        //we expect that the DDC lookup time was updated
        assertTrue(dynamicDiscoveryTimeSecondLookup.compareTo(dynamicDiscoveryTimeFirstLookup) > 0);

        final String finalRecipient2PartyName = DynamicDiscoveryServicePEPPOLConfigurationMockup.participantConfigurations.get(FINAL_RECIPIENT2).getPartyName();
        LOG.info("---first lookup for final recipient [{}] and party [{}]", FINAL_RECIPIENT2, finalRecipient2PartyName);
        //FINAL_RECIPIENT1 is associated with PARTY_NAME1 which was already added in the previous lookup, we expect that no new certificates are added in the truststore and no new parties are added in the PMode
        //we expect the URL for FINAL_RECIPIENT2 is saved in the database
        doLookupForFinalRecipient(FINAL_RECIPIENT2, finalRecipient2PartyName, 3, 2, 1, 2);

        final String finalRecipient3PartyName = DynamicDiscoveryServicePEPPOLConfigurationMockup.participantConfigurations.get(FINAL_RECIPIENT3).getPartyName();
        LOG.info("---first lookup for final recipient [{}] and party [{}]", FINAL_RECIPIENT3, finalRecipient3PartyName);
        //we expect the party and the certificate are added
        //we expect the URL for FINAL_RECIPIENT2 is saved in the database
        doLookupForFinalRecipient(FINAL_RECIPIENT3, finalRecipient3PartyName, 4, 3, 2, 3);
    }

    @Test
    public void lookupAndUpdateConfigurationForToPartyIdWithMultipleThreads() throws EbMS3Exception, XmlProcessingException, IOException {
        initializePmodeAndProperties(DYNAMIC_DISCOVERY_PMODE_WITH_SIGN_AND_ENCRYPTION);

        //clean up
        cleanBeforeLookup();

        //before starting the test we verify the initial setup
        //we have the blue_gw and red_gw already present in the truststore
        dynamicDiscoveryAssertionUtil.verifyNumberOfEntriesInTheTruststore(2);
        //the blue_gw party is present already in the Pmode
        dynamicDiscoveryAssertionUtil.verifyIfPartyIsPresentInTheListOfPartiesFromPmode(1, "blue_gw");

        int numberOfThreads = 20;

        //we create the final recipient name, party name and the party Access Point X509 certificate
        PKIUtil pkiUtil = new PKIUtil();
        for (int index = 0; index < numberOfThreads; index++) {

            final String finalRecipient = String.format(FINAL_RECIPIENT_MULTIPLE_THREADS_FORMAT, index);
            final String partyName = String.format(PARTY_NAME_MULTIPLE_THREADS_FORMAT, index);
            //we create the certificate for the party Access Point associated to the final recipient
            final Long certificateSerialNumber = Long.valueOf(String.format(PARTY_NAME_MULTIPLE_THREADS_CERTIFICATE_SERIAL_NUMBER_FORMAT, index));
            final X509Certificate partyCertificate = pkiUtil.createCertificateWithSubject(BigInteger.valueOf(certificateSerialNumber), "CN=" + partyName + ",OU=Domibus,O=eDelivery,C=EU");

            //we add the configuration so that the final recipient can be lookup up and Endpoint is returned(simulating the lookup in SMP)
            DynamicDiscoveryServicePEPPOLConfigurationMockup.addParticipantConfiguration(
                    finalRecipient,
                    partyName,
                    "peppol-transport-as4-v2_0",
                    new SMPProcessIdentifier("bdx:noprocess", "tc1"),
                    partyCertificate
            );
        }

        LOG.info("Starting [{}] threads", numberOfThreads);
        List<Thread> threads = new ArrayList<>();
        //we start all threads
        for (int index = 0; index < numberOfThreads; index++) {
            final String finalRecipient = String.format(FINAL_RECIPIENT_MULTIPLE_THREADS_FORMAT, index);
            final String partyName = String.format(PARTY_NAME_MULTIPLE_THREADS_FORMAT, index);

            LOG.info("---lookup for final recipient [{}] and party [{}]", finalRecipient, partyName);

            final Thread thread = new Thread(() -> {
                try {
                    //perform lookup
                    doLookupForFinalRecipient(finalRecipient);
                } catch (EbMS3Exception e) {
                    LOG.error("Error while looking up for final recipient [{}]", finalRecipient);
                    throw new RuntimeException(e);
                }
            });
            thread.start();
            threads.add(thread);
        }

        LOG.info("Waiting for threads to finish");
        //we wait for all threads to finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                LOG.error("Error joining thread [{}]", thread);
                throw new RuntimeException(e);
            }
        }

        //we expect 22 certificates in the truststore: we have 2 initial certificates in the truststore(blue_gw and red_gw) + we do 20 unique lookups
        dynamicDiscoveryAssertionUtil.verifyNumberOfEntriesInTheTruststore(22);

        for (int index = 0; index < numberOfThreads; index++) {
            final String finalRecipient = String.format(FINAL_RECIPIENT_MULTIPLE_THREADS_FORMAT, index);
            final String partyName = String.format(PARTY_NAME_MULTIPLE_THREADS_FORMAT, index);

            //we expect 21 parties in the Pmode list(equal to the number of unique lookups + 1(the initial blue_gw party)) and 20 in the responder parties for the unique lookups
            dynamicDiscoveryAssertionUtil.verifyIfPartyIsPresentInTheListOfPartiesFromPmode(numberOfThreads + 1, partyName);
            dynamicDiscoveryAssertionUtil.verifyIfPartyIsPresentInTheResponderPartiesForAllProcesses(numberOfThreads, partyName);

            //we verify if the final recipient was added in the database
            dynamicDiscoveryAssertionUtil.verifyThatDynamicDiscoveryLookupWasAddedInTheDatabase(numberOfThreads, finalRecipient, partyName);
        }
    }

    @Test
    public void dynamicDiscoveryInSMPTriggeredOnce_secondTimeConfigurationIsLoadedFromCache() throws EbMS3Exception, XmlProcessingException, IOException {
        initializePmodeAndProperties(DYNAMIC_DISCOVERY_PMODE_WITH_SIGN_AND_ENCRYPTION);

        //clean up
        cleanBeforeLookup();

        final String finalRecipient1 = FINAL_RECIPIENT1;
        final String finalRecipient1PartyName = DynamicDiscoveryServicePEPPOLConfigurationMockup.participantConfigurations.get(finalRecipient1).getPartyName();
        final UserMessage userMessage = buildUserMessage(finalRecipient1);
        //it triggers dynamic discovery lookup in SMP  because toParty is empty
        //id adds in the PMode the discovered party and it adds in the truststore the certificate of the discovered party
        assertNotNull(multiDomainPModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PUSH));

        final DynamicDiscoveryLookupEntity dynamicDiscoveryLookupEntity = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient1);
        assertNotNull(dynamicDiscoveryLookupEntity);
        final Date firstDynamicDiscoveryTime = dynamicDiscoveryLookupEntity.getDynamicDiscoveryTime();

        try {
            //sleep 100 milliseconds so that the second DDC lookup time should be after the first lookup
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error sleeping thread", e);
        }

        //it should take the responder party details from the cache; to verify if dynamic discovery lookup in SMP is triggered again, we check the dynamic discovery time of the final recipient entity; if the dynamic discovery time is not changed,
        //it means that the SMP lookup was not performed; as a reminder, each time an SMP lookup is done for a final recipient, we update the dynamic discovery time of the final recipient entity)
        assertNotNull(multiDomainPModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PUSH));

        //verify that the dynamic discovery was not triggered again
        dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient1).getDynamicDiscoveryTime();

        final Date dynamicDiscoveryTimeLatest = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient1).getDynamicDiscoveryTime();
        assertEquals(firstDynamicDiscoveryTime, dynamicDiscoveryTimeLatest);
    }

    @Test
    public void dynamicDiscoveryInSMPTriggeredOnce_clearPmode_secondTimeDynamicDiscoveryIsTriggeredAgain() throws EbMS3Exception, XmlProcessingException, IOException {
        initializePmodeAndProperties(DYNAMIC_DISCOVERY_PMODE_WITH_SIGN_AND_ENCRYPTION);

        //clean up
        cleanBeforeLookup();

        final String finalRecipient1 = FINAL_RECIPIENT1;
        final String finalRecipient1PartyName = DynamicDiscoveryServicePEPPOLConfigurationMockup.participantConfigurations.get(finalRecipient1).getPartyName();
        final UserMessage userMessage = buildUserMessage(finalRecipient1);
        //it triggers dynamic discovery lookup in SMP  because toParty is empty
        //id adds in the PMode the discovered party and it adds in the truststore the certificate of the discovered party
        assertNotNull(multiDomainPModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PUSH));

        final DynamicDiscoveryLookupEntity dynamicDiscoveryLookupEntity = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient1);
        assertNotNull(dynamicDiscoveryLookupEntity);
        final Date firstDynamicDiscoveryTime = dynamicDiscoveryLookupEntity.getDynamicDiscoveryTime();

        //refresh the PMode so that the receiver party is removed from the PMode cache memory
        refreshPmode();

        try {
            //sleep 100 milliseconds so that the second DDC lookup time is after the first lookup
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error sleeping thread", e);
        }

        //it should trigger a dynamic discovery because the receiver party is missing from the Pmode; to verify if dynamic discovery lookup in SMP is triggered again, we check the dynamic discovery time of the final recipient entity; if the dynamic discovery time is changed,
        //it means that the SMP lookup was performed again; as a reminder, each time an SMP lookup is done for a final recipient, we update the dynamic discovery time of the final recipient entity)
        assertNotNull(multiDomainPModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PUSH));

        //verify that the dynamic discovery was triggered again
        final Date dynamicDiscoveryTimeLatest = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient1).getDynamicDiscoveryTime();
        assertNotEquals(firstDynamicDiscoveryTime, dynamicDiscoveryTimeLatest);
    }

    @Test
    public void dynamicDiscoveryInSMPTriggeredOnce_clearTruststore_secondTimeDynamicDiscoveryIsTriggeredAgain() throws EbMS3Exception, XmlProcessingException, IOException {
        initializePmodeAndProperties(DYNAMIC_DISCOVERY_PMODE_WITH_SIGN_AND_ENCRYPTION);

        //clean up
        cleanBeforeLookup();

        final String finalRecipient1 = FINAL_RECIPIENT1;
        final String finalRecipient1PartyName = DynamicDiscoveryServicePEPPOLConfigurationMockup.participantConfigurations.get(finalRecipient1).getPartyName();
        final UserMessage userMessage = buildUserMessage(finalRecipient1);
        //it triggers dynamic discovery lookup in SMP  because toParty is empty
        //id adds in the PMode the discovered party and it adds in the truststore the certificate of the discovered party
        assertNotNull(multiDomainPModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PUSH));

        final DynamicDiscoveryLookupEntity dynamicDiscoveryLookupEntity = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient1);
        assertNotNull(dynamicDiscoveryLookupEntity);
        final Date firstDynamicDiscoveryTime = dynamicDiscoveryLookupEntity.getDynamicDiscoveryTime();

        //clean the truststore so that the receiver party public certificate is removed from the truststore
        cleanTruststore();

        try {
            //sleep 100 milliseconds so that the second DDC lookup time is after the first lookup
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error sleeping thread", e);
        }

        //it should trigger a dynamic discovery because the receiver party public certificate is missing from the truststore; to verify if dynamic discovery lookup in SMP is triggered again, we check the dynamic discovery time of the final recipient entity; if the dynamic discovery time is changed,
        //it means that the SMP lookup was performed again; as a reminder, each time an SMP lookup is done for a final recipient, we update the dynamic discovery time of the final recipient entity)
        assertNotNull(multiDomainPModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PUSH));

        //verify that the dynamic discovery was triggered again
        final Date dynamicDiscoveryTimeLatest = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient1).getDynamicDiscoveryTime();
        assertNotEquals(firstDynamicDiscoveryTime, dynamicDiscoveryTimeLatest);
    }

    @Test
    public void c1SubmitsMessageOnServer1_messageSendingFromC2ToC3IsDoneOnServer2() throws EbMS3Exception, XmlProcessingException, IOException {
        initializePmodeAndProperties(DYNAMIC_DISCOVERY_PMODE_WITH_SIGN_AND_ENCRYPTION);

        //clean up
        cleanBeforeLookup();

        final UserMessage userMessage = buildUserMessage(FINAL_RECIPIENT1);
        //it triggers dynamic discovery lookup in SMP  because toParty is empty
        //id adds in the PMode the discovered party and it adds in the truststore the certificate of the discovered party
        assertNotNull(multiDomainPModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PUSH));

        //we simulate the following scenario
        //- message is submitted on server 1 and the dynamic discovery is triggered at submission time: Pmode and truststore are updated normally
        //- message is sent from server 2 via JMS listener; in this case the receiver party is not in the Pmode memory
        //we clean the Pmode memory
        refreshPmode();

        //it should trigger a lookup in SMP and the PMode context should be retrieved successfully
        assertNotNull(multiDomainPModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PUSH));
    }

    @Test
    public void c1SubmitsMessageWithPartyConfiguredInPmodeAndSignOnlyPolicy() throws EbMS3Exception, XmlProcessingException, IOException {
        initializePmodeAndProperties(DYNAMIC_DISCOVERY_PMODE_WITH_SIGN_ONLY);

        //clean up
        cleanBeforeLookup();

        final UserMessage userMessage = buildUserMessage(FINAL_RECIPIENT1);
        //we change the action to match the process tc2Process
        userMessage.getAction().setValue("TC1Leg2");

        //we set the party identifier for the party already configured in the Pmode
        final To toParty = userMessage.getPartyInfo().getTo();
        final PartyId fromPartyId = new PartyId();
        fromPartyId.setValue(PARTY_NAME5);
        fromPartyId.setType("urn:fdc:peppol.eu:2017:identifiers:ap");
        final PartyRole partyRole = new PartyRole();
        partyRole.setValue("urn:fdc:peppol.eu:2017:roles:ap:as4");
        toParty.setToRole(partyRole);
        toParty.setToPartyId(fromPartyId);

        //no dynamic discovery should be triggered and no check of the public certificate is done; exception is not raised
        final MessageExchangeConfiguration userMessageExchangeContext = multiDomainPModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PUSH);
        assertNotNull(userMessageExchangeContext);
    }

    @Test(expected = AuthenticationException.class)
    public void c1SubmitsMessageToPartyWithExpiredCertificate() throws EbMS3Exception, XmlProcessingException, IOException {
        initializePmodeAndProperties(DYNAMIC_DISCOVERY_PMODE_WITH_SIGN_AND_ENCRYPTION);

        //clean up
        cleanBeforeLookup();

        final UserMessage userMessage = buildUserMessage(FINAL_RECIPIENT4);
        //it triggers dynamic discovery lookup in SMP  because toParty is empty
        //it throws an exception because the discovered certificate is expired
        multiDomainPModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PUSH);
    }

    @Test
    public void deleteDDCCertificatesNotDiscoveredInTheLastPeriod() throws EbMS3Exception, KeyStoreException, SQLException, XmlProcessingException, IOException {
        initializePmodeAndProperties(DYNAMIC_DISCOVERY_PMODE_WITH_SIGN_AND_ENCRYPTION);

        //uncomment to access the H2 console in the browser; JDBC URL is jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false
        //Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();

        //clean up
        cleanBeforeLookup();

        //we check the number of parties in the Pmode before running the test
        dynamicDiscoveryAssertionUtil.verifyListOfPartiesFromPmodeSize(1);

        //we check the number of parties in the truststore before running the test
        dynamicDiscoveryAssertionUtil.verifyNumberOfEntriesInTheTruststore(2);

        final String finalRecipient1 = FINAL_RECIPIENT1;//party1
        final String finalRecipient2 = FINAL_RECIPIENT2;//party1
        final String finalRecipient3 = FINAL_RECIPIENT3;//party2
        //we skip finalRecipient4 because it's configured with party3 which has an expired certificate
        final String finalRecipient5 = FINAL_RECIPIENT5;//party4
        String partyName1 = PARTY_NAME1;
        String partyName2 = PARTY_NAME2;
        String partyName4 = PARTY_NAME4;

        //---finalRecipient1, party1, certificate1

        //we expect the party and the certificate are added in the PMode and truststore
        doLookupForFinalRecipient(finalRecipient1, partyName1, 3, 2, 1, 1);

        //we set the DDC time for the finalRecipient1 to 25h ago
        Date ddcTimeFinalRecipient1 = DateUtils.addHours(new Date(), 25 * -1);
        setDynamicDiscoveryTime(finalRecipient1, ddcTimeFinalRecipient1);

        //---finalRecipient2, party1, certificate1
        //we expect the party and the certificate are not added in the PMode and truststore
        doLookupForFinalRecipient(finalRecipient2, partyName1, 3, 2, 1, 2);
        //we set the DDC time for the finalRecipient2 to 2h ago
        Date ddcTimeFinalRecipient2 = DateUtils.addHours(new Date(), 2 * -1);
        setDynamicDiscoveryTime(finalRecipient2, ddcTimeFinalRecipient2);

        //finalRecipient3, party2, certificate2
        //we expect the party and the certificate are added in the PMode and truststore
        doLookupForFinalRecipient(finalRecipient3, partyName2, 4, 3, 2, 3);
        //we set the DDC time for the finalRecipient3 to 4h ago
        Date ddcTimeFinalRecipient3 = DateUtils.addHours(new Date(), 4 * -1);
        setDynamicDiscoveryTime(finalRecipient3, ddcTimeFinalRecipient3);

        //finalRecipient5, party4, certificate4
        //we expect the party and the certificate are added in the PMode and truststore
        doLookupForFinalRecipient(finalRecipient5, partyName4, 5, 4, 3, 4);
        //we keep the final recipient5 discovery time to the current time

        //we expect 5 entries in the truststore and 4 entries the Pmode
        dynamicDiscoveryAssertionUtil.verifyListOfPartiesFromPmodeSize(4);
        dynamicDiscoveryAssertionUtil.verifyNumberOfEntriesInTheTruststore(5);

        //we set the retention to 3h and start clean up
        dynamicDiscoveryLookupService.deleteDDCLookupEntriesNotDiscoveredInTheLastPeriod(3);

        //start assertions

        //we expect 4 entries in the truststore and 3 entries the Pmode: party 2 was deleted from the Pmode and certificate 2 deleted from the truststore
        dynamicDiscoveryAssertionUtil.verifyListOfPartiesFromPmodeSize(3);
        dynamicDiscoveryAssertionUtil.verifyNumberOfEntriesInTheTruststore(4);

        //we expect that the entry for finalRecipient1 was deleted from the database; DDC time is old
        DynamicDiscoveryLookupEntity lookupEntryFinalRecipient1AfterJobRan = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient1);
        assertNull(lookupEntryFinalRecipient1AfterJobRan);
        //we expect that the certificate1 is still present in the truststore; it's still used by finalRecipient2
        assertNotNull(multiDomainCertificateProvider.getCertificateFromTruststore(DOMAIN, partyName1));
        //we expect that the party1 is still present in the PMode responder parties; it's still used by finalRecipient2
        assertTrue(isPartyPresentInTheResponderParties(partyName1));
        //we expect that the party1 is still present in the PMode parties list
        assertNotNull(pModeProvider.getPartyByName(partyName1));

        DynamicDiscoveryLookupEntity lookupEntryFinalRecipient2AfterJobRan = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient2);
        assertNotNull(lookupEntryFinalRecipient2AfterJobRan);


        //we expect that the entry for finalRecipient3 was deleted from the database; DDC time is old
        DynamicDiscoveryLookupEntity lookupEntryFinalRecipient3AfterJobRan = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient3);
        assertNull(lookupEntryFinalRecipient3AfterJobRan);
        //we expect that the certificate2 was deleted from the truststore
        assertNull(multiDomainCertificateProvider.getCertificateFromTruststore(DOMAIN, partyName2));
        //we expect that the party2 was deleted from the PMode responder parties
        assertFalse(isPartyPresentInTheResponderParties(partyName2));
        //we expect that the party2 was deleted from the PMode parties list
        assertNull(pModeProvider.getPartyByName(partyName2));

        //we expect that the entry for finalRecipient5 is still present int the database; DDC time is new
        DynamicDiscoveryLookupEntity lookupEntryFinalRecipient5AfterJobRan = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient5);
        assertNotNull(lookupEntryFinalRecipient5AfterJobRan);
        //we expect that the certificate4 is still present in the truststore
        assertNotNull(multiDomainCertificateProvider.getCertificateFromTruststore(DOMAIN, partyName4));
        //we expect that the partyName4 is still present in the PMode responder parties
        assertTrue(isPartyPresentInTheResponderParties(partyName4));
    }

    @Test
    public void testSignalForPartyAndFinalRecipientDeletion() throws EbMS3Exception, XmlProcessingException, IOException {
        initializePmodeAndProperties(DYNAMIC_DISCOVERY_PMODE_WITH_SIGN_AND_ENCRYPTION);

        //clean up
        cleanBeforeLookup();

        final String finalRecipient1 = FINAL_RECIPIENT1;//party1
        String partyName1 = PARTY_NAME1;

        dynamicDiscoveryAssertionUtil.verifyListOfPartiesFromPmodeSize(1);

        //---finalRecipient1, party1, certificate1
        //we expect the party and the certificate are added in the PMode and truststore
        doLookupForFinalRecipient(finalRecipient1, partyName1, 3, 2, 1, 1);
        //we set the DDC time for the finalRecipient1 to 25h ago
        Date ddcTimeFinalRecipient1 = DateUtils.addHours(new Date(), 25 * -1);
        setDynamicDiscoveryTime(finalRecipient1, ddcTimeFinalRecipient1);

        domibusPropertyProvider.setProperty(DOMAIN, DomibusConfigurationService.CLUSTER_DEPLOYMENT, "true");
        try {
            //we verify that the URL for final recipient is present in the cache
            assertTrue(dynamicDiscoveryLookupService.getFinalRecipientAccessPointUrls().containsKey(finalRecipient1));
            //we simulate the signal to the other servers

            ReflectionTestUtils.setField(signalService, "jmsManager", new JMSManagerImpl() {
                @Override
                public void sendMessageToTopic(JmsMessage message, Topic destination, boolean excludeOrigin) {
                    final String commandName = message.getProperties().get(Command.COMMAND);
                    if (dynamicDiscoveryDeletePmodePartiesCommandTask.canHandle(commandName)) {
                        dynamicDiscoveryDeletePmodePartiesCommandTask.execute(message.getProperties());
                    }
                    if (dynamicDiscoveryDeleteFinalRecipientsFromCacheCommandTask.canHandle(commandName)) {
                        dynamicDiscoveryDeleteFinalRecipientsFromCacheCommandTask.execute(message.getProperties());
                    }
                }
            });
            signalService.signalDeleteFinalRecipientCache(Arrays.asList(finalRecipient1));
            //we verify that the URL for final recipient was removed from the cache
            assertFalse(dynamicDiscoveryLookupService.getFinalRecipientAccessPointUrls().containsKey(finalRecipient1));

            //we verify that the party is present in the PMode party list
            assertNotNull(pModeProvider.getPartyByName(partyName1));
            signalService.signalDeletePmodeParties(Arrays.asList(partyName1));
            assertNull(pModeProvider.getPartyByName(partyName1));

            //check that there are no processes which contain the party name in the responder parties
            assertFalse(isPartyPresentInTheResponderParties(partyName1));
        } finally {
            domibusPropertyProvider.setProperty(DOMAIN, DomibusConfigurationService.CLUSTER_DEPLOYMENT, "false");
        }
    }

    protected boolean isPartyPresentInTheResponderParties(String partyName) {

        final Collection<Process> dynamicSenderProcesses = ((DynamicDiscoveryPModeProvider) multiDomainPModeProvider.getCurrentPModeProvider()).getDynamicProcesses(MSHRole.SENDING);
        return dynamicSenderProcesses.stream().filter(process -> {
            final Set<Party> responderParties = process.getResponderParties();
            return responderParties.stream().filter(party -> party.getName().equals(partyName)).count() > 0;
        }).count() > 0;
    }

    protected void setDynamicDiscoveryTime(String finalRecipient, Date date) {
        final DynamicDiscoveryLookupEntity finalRecipient1Entity = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient);
        finalRecipient1Entity.setDynamicDiscoveryTime(date);
        dynamicDiscoveryLookupDao.createOrUpdate(finalRecipient1Entity);
    }


    private void cleanBeforeLookup() {
        cleanTruststore();
        deleteAllFinalRecipients();
        refreshPmode();
    }

    private void refreshPmode() {
        multiDomainPModeProvider.refresh();
    }

    private void cleanTruststore() {
        List<String> initialAliasesInTheTruststore = new ArrayList<>();
        initialAliasesInTheTruststore.add("blue_gw");
        initialAliasesInTheTruststore.add("red_gw");
        keepOnlyInitialCertificates(initialAliasesInTheTruststore);

        multiDomainCryptoService.resetTrustStore(DOMAIN);
    }

    private void doLookupForFinalRecipient(String finalRecipient) throws EbMS3Exception {
        final UserMessage userMessage = buildUserMessage(finalRecipient);
        final DynamicDiscoveryPModeProvider dynamicDiscoveryPModeProvider = (DynamicDiscoveryPModeProvider) multiDomainPModeProvider.getCurrentPModeProvider();
        final eu.domibus.common.model.configuration.Configuration initialPModeConfiguration = dynamicDiscoveryPModeProvider.getConfiguration();
        final Collection<Process> pmodeProcesses = initialPModeConfiguration.getBusinessProcesses().getProcesses();

        //do the lookup based on final recipient
        dynamicDiscoveryPModeProvider.lookupAndUpdateConfigurationForToPartyId(getFinalRecipientCacheKey(finalRecipient), userMessage, pmodeProcesses);
    }

    private void doLookupForFinalRecipient(String finalRecipient,
                                           String partyNameAccessPointForFinalRecipient,
                                           int expectedTruststoreEntriesAfterLookup,
                                           int expectedPmodePartiesAfterLookup,
                                           int expectedResponderPartiesAfterLookup,
                                           int expectedDDCLookupEntriesInDatabase) throws EbMS3Exception {
        final UserMessage userMessage = buildUserMessage(finalRecipient);
        final DynamicDiscoveryPModeProvider dynamicDiscoveryPModeProvider = (DynamicDiscoveryPModeProvider) multiDomainPModeProvider.getCurrentPModeProvider();
        final eu.domibus.common.model.configuration.Configuration initialPModeConfiguration = dynamicDiscoveryPModeProvider.getConfiguration();
        final Collection<Process> pmodeProcesses = initialPModeConfiguration.getBusinessProcesses().getProcesses();

        //do the lookup based on final recipient
        dynamicDiscoveryPModeProvider.lookupAndUpdateConfigurationForToPartyId(getFinalRecipientCacheKey(finalRecipient), userMessage, pmodeProcesses);

        //verify that the party to was added in the UserMessage
        final String toParty = userMessage.getPartyInfo().getToParty();
        assertEquals(partyNameAccessPointForFinalRecipient, toParty);

        //verify that the party certificate was added in the truststore
        dynamicDiscoveryAssertionUtil.verifyNumberOfEntriesInTheTruststore(expectedTruststoreEntriesAfterLookup);

        //verify that the party was added in the list of available parties in the Pmode
        dynamicDiscoveryAssertionUtil.verifyIfPartyIsPresentInTheListOfPartiesFromPmode(expectedPmodePartiesAfterLookup, partyNameAccessPointForFinalRecipient);

        //verify that the party was added in the responder parties for all process
        dynamicDiscoveryAssertionUtil.verifyIfPartyIsPresentInTheResponderPartiesForAllProcesses(expectedResponderPartiesAfterLookup, partyNameAccessPointForFinalRecipient);

        //verify that the final recipient was added in the database
        dynamicDiscoveryAssertionUtil.verifyThatDynamicDiscoveryLookupWasAddedInTheDatabase(expectedDDCLookupEntriesInDatabase, finalRecipient, partyNameAccessPointForFinalRecipient);
    }


    private void deleteAllFinalRecipients() {
        //we delete the final recipient entries from the database and we clear the memory cache
        final List<DynamicDiscoveryLookupEntity> allLookupEntities = dynamicDiscoveryLookupDao.findAll();
        dynamicDiscoveryLookupDao.deleteAll(allLookupEntities);
        dynamicDiscoveryLookupService.clearFinalRecipientAccessPointUrlsCache();
    }

    private List<TrustStoreEntry> keepOnlyInitialCertificates(List<String> initialAliasesInTheTruststore) {
        List<TrustStoreEntry> trustStoreEntries = multiDomainCertificateProvider.getTrustStoreEntries(DOMAIN);
        //we delete any other certificates apart from blue and red to have a clean state
        final Collection<TrustStoreEntry> certificatesToBeDeleted = trustStoreEntries.stream().filter(trustStoreEntry -> !initialAliasesInTheTruststore.contains(trustStoreEntry.getName())).collect(Collectors.toList());
        certificatesToBeDeleted.stream().forEach(trustStoreEntry -> multiDomainCertificateProvider.removeCertificate(DOMAIN, trustStoreEntry.getName()));

        //check that we have only the initial certificates
        trustStoreEntries = multiDomainCertificateProvider.getTrustStoreEntries(DOMAIN);
        assertEquals(2, trustStoreEntries.size());

        return trustStoreEntries;
    }


    protected String getFinalRecipientCacheKey(String finalRecipient) {
        return finalRecipient + "cacheKey";
    }

    private UserMessage buildUserMessage(String finalRecipient) {
        UserMessage userMessage = new UserMessage();
        final MpcEntity mpc = new MpcEntity();
        mpc.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC");
        userMessage.setMpc(mpc);

        Set<MessageProperty> messageProperties = new HashSet<>();
        final MessageProperty finalRecipientProperty = new MessageProperty();
        finalRecipientProperty.setName("finalRecipient");
        finalRecipientProperty.setValue(finalRecipient);
        messageProperties.add(finalRecipientProperty);
        userMessage.setMessageProperties(messageProperties);

        ServiceEntity serviceEntity = new ServiceEntity();
        serviceEntity.setValue("bdx:noprocess");
        serviceEntity.setType("tc1");
        userMessage.setService(serviceEntity);

        final ActionEntity actionEntity = new ActionEntity();
        actionEntity.setValue("TC1Leg1");
        userMessage.setAction(actionEntity);

        final PartyInfo partyInfo = new PartyInfo();
        From from = new From();
        final PartyId fromPartyId = new PartyId();
        fromPartyId.setValue("domibus-blue");
        fromPartyId.setType("urn:fdc:peppol.eu:2017:identifiers:ap");
        final PartyRole partyRole = new PartyRole();
        partyRole.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        from.setFromRole(partyRole);
        from.setFromPartyId(fromPartyId);
        partyInfo.setFrom(from);


        partyInfo.setTo(new To());

        userMessage.setPartyInfo(partyInfo);
        return userMessage;
    }

    @Test
    public void testGetAllowedSMPCertificatePolicyOIDsPropertyNotDefined() {
        final Domain currentDomain = domainContextProvider.getCurrentDomain();

        final String initialValue = domibusPropertyProvider.getProperty(currentDomain, DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION);

        domibusPropertyProvider.setProperty(currentDomain,
                DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION,
                "");

        assertTrue(CollectionUtils.isEmpty(dynamicDiscoveryService.getAllowedSMPCertificatePolicyOIDs()));

        domibusPropertyProvider.setProperty(currentDomain,
                DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION,
                initialValue);
    }

    @Test
    public void testGetAllowedSMPCertificatePolicyOIDsPropertyWithOne() {
        final Domain currentDomain = domainContextProvider.getCurrentDomain();

        final String initialValue = domibusPropertyProvider.getProperty(currentDomain, DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION);

        domibusPropertyProvider.setProperty(currentDomain,
                DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION,
                CERTIFICATE_POLICY_QCP_NATURAL);

        final List<String> allowedSMPCertificatePolicyOIDs = dynamicDiscoveryService.getAllowedSMPCertificatePolicyOIDs();
        assertEquals(1, allowedSMPCertificatePolicyOIDs.size());
        assertTrue(allowedSMPCertificatePolicyOIDs.contains(CERTIFICATE_POLICY_QCP_NATURAL));

        domibusPropertyProvider.setProperty(currentDomain,
                DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION,
                initialValue);
    }


    @Test
    public void testGetAllowedSMPCertificatePolicyOIDsPropertyWithMultipleAndSpaces() {
        final Domain currentDomain = domainContextProvider.getCurrentDomain();

        final String initialValue = domibusPropertyProvider.getProperty(currentDomain, DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION);

        String newValue = CERTIFICATE_POLICY_QCP_NATURAL
                + "," + CERTIFICATE_POLICY_QCP_LEGAL
                + ", " + CERTIFICATE_POLICY_QCP_NATURAL_QSCD
                + " ,     " + CERTIFICATE_POLICY_QCP_LEGAL_QSCD;
        domibusPropertyProvider.setProperty(currentDomain,
                DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION,
                newValue);

        final List<String> allowedSMPCertificatePolicyOIDs = dynamicDiscoveryService.getAllowedSMPCertificatePolicyOIDs();
        assertEquals(4, allowedSMPCertificatePolicyOIDs.size());
        assertEquals(CERTIFICATE_POLICY_QCP_NATURAL, allowedSMPCertificatePolicyOIDs.get(0));
        assertEquals(CERTIFICATE_POLICY_QCP_LEGAL, allowedSMPCertificatePolicyOIDs.get(1));
        assertEquals(CERTIFICATE_POLICY_QCP_NATURAL_QSCD, allowedSMPCertificatePolicyOIDs.get(2));
        assertEquals(CERTIFICATE_POLICY_QCP_LEGAL_QSCD, allowedSMPCertificatePolicyOIDs.get(3));

        domibusPropertyProvider.setProperty(currentDomain,
                DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION,
                initialValue);
    }

    @Test
    public void testIsValidEndpointForNullPeriod() {
        boolean result = dynamicDiscoveryService.isValidEndpoint(null, null);

        assertTrue("If period is not given the endpoint must be considered as valid!", result);
    }

    @Test
    public void testIsValidEndpointForValidPeriod() {
        Date currentDate = Calendar.getInstance().getTime();

        final DateUtilImpl dateUtil = new DateUtilImpl();

        final OffsetDateTime startDate = dateUtil.convertDateToOffsetDateTime(DateUtils.addDays(currentDate, -1));
        final OffsetDateTime endDate = dateUtil.convertDateToOffsetDateTime(DateUtils.addDays(currentDate, +1));
        boolean result = dynamicDiscoveryService.isValidEndpoint(startDate, endDate);

        assertTrue(result);
    }

    @Test
    public void testIsValidEndpointForNotYetValid() {
        final DateUtilImpl dateUtil = new DateUtilImpl();
        Date currentDate = Calendar.getInstance().getTime();
        OffsetDateTime fromDate = dateUtil.convertDateToOffsetDateTime(DateUtils.addDays(currentDate, 1));
        OffsetDateTime toDate = dateUtil.convertDateToOffsetDateTime(DateUtils.addDays(currentDate, 2));

        boolean result = dynamicDiscoveryService.isValidEndpoint(fromDate, toDate);
        assertFalse(result);
    }

    @Test
    public void testIsValidEndpointForExpired() {
        final DateUtilImpl dateUtil = new DateUtilImpl();
        Date currentDate = Calendar.getInstance().getTime();
        OffsetDateTime fromDate = dateUtil.convertDateToOffsetDateTime(DateUtils.addDays(currentDate, -2));
        OffsetDateTime toDate = dateUtil.convertDateToOffsetDateTime(DateUtils.addDays(currentDate, -1));

        boolean result = dynamicDiscoveryService.isValidEndpoint(fromDate, toDate);
        assertFalse(result);
    }

    @Test(expected = DomibusCoreException.class)
    public void testSmlZoneEmpty() throws EbMS3Exception {
        final Domain currentDomain = domainContextProvider.getCurrentDomain();

        final String initialValue = domibusPropertyProvider.getProperty(currentDomain, DOMIBUS_SMLZONE);

        domibusPropertyProvider.setProperty(currentDomain, DOMIBUS_SMLZONE, "");
        dynamicDiscoveryService.lookupInformation("domain",
                "participantId_expired",
                "participantIdScheme",
                "scheme::value",
                "urn:epsosPatientService::List",
                "ehealth-procid-qns");

        domibusPropertyProvider.setProperty(currentDomain, DOMIBUS_SMLZONE, initialValue);
    }
}
