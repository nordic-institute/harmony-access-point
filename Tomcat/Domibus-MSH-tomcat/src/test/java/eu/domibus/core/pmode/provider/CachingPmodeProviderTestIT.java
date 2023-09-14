package eu.domibus.core.pmode.provider;

import eu.domibus.AbstractIT;
import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.ebms3.MessageExchangePattern;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.participant.FinalRecipientDao;
import eu.domibus.core.property.DomibusPropertyResourceHelperImpl;
import eu.domibus.messaging.XmlProcessingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY;
import static org.junit.Assert.*;


/**
 * @author Cosmin Baciu
 * @since 5.0.2
 */
@Transactional
public class CachingPmodeProviderTestIT extends AbstractIT {

    @Autowired
    protected PModeProviderFactoryImpl pModeProviderFactory;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected FinalRecipientDao finalRecipientDao;

    @Autowired
    protected FinalRecipientService finalRecipientService;

    @Autowired
    DomibusPropertyResourceHelperImpl configurationPropertyResourceHelper;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Before
    public void setUp() throws Exception {
        finalRecipientDao.deleteAll(finalRecipientDao.findAll());
        domibusPropertyProvider.setProperty(domainContextProvider.getCurrentDomain(), DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, "true");
    }

    @After
    public void clean() {
        domibusPropertyProvider.setProperty(domainContextProvider.getCurrentDomain(), DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, "false");
    }

    @Test
    public void testGetReceiverEndpointURLFromParty() {
        final CachingPModeProvider pmodeProvider = (CachingPModeProvider) pModeProviderFactory.createDomainPModeProvider(domainContextProvider.getCurrentDomain());

        //no final recipients saved in the database
        assertTrue(finalRecipientDao.findAll().isEmpty());

        String finalRecipient = "0001:recipient1";
        Party party = new Party();
        party.setName("domibus-blue");
        final String partyEndpoint = "http://localhost:8080/domibus/services/msh?domain=default";
        party.setEndpoint(partyEndpoint);

        final String receiverPartyEndpoint = pmodeProvider.getReceiverPartyEndpoint(party, finalRecipient);
        //no final recipients saved in the database after we retrieved the URL
        assertTrue(finalRecipientDao.findAll().isEmpty());
        assertEquals(partyEndpoint, receiverPartyEndpoint);
    }

    @Test
    public void testGetReceiverEndpointURLFromFinalRecipientEndpointURL() {
        final CachingPModeProvider pmodeProvider = (CachingPModeProvider) pModeProviderFactory.createDomainPModeProvider(domainContextProvider.getCurrentDomain());

        //no final recipients saved in the database
        assertTrue(finalRecipientDao.findAll().isEmpty());

        String finalRecipient = "0001:recipient1";
        String finalRecipientURL = "http://localhost:8080/domibus/services/msh?domain=domain1";
        Party party = new Party();
        party.setName("domibus-blue");
        final String partyEndpoint = "http://localhost:8080/domibus/services/msh?domain=default";
        party.setEndpoint(partyEndpoint);

        //the final recipient URL is not saved in DB; the endpoint URL is retrieved from the Pmode Party
        String receiverPartyEndpoint = pmodeProvider.getReceiverPartyEndpoint(party, finalRecipient);
        assertEquals(partyEndpoint, receiverPartyEndpoint);

        //we save the final recipient URL in the database
        pmodeProvider.saveFinalRecipientEndpoint(finalRecipient, finalRecipientURL);
        //final recipient should be saved in the database after we retrieved the URL
        assertEquals(1, finalRecipientDao.findAll().size());

        //the retrieved URL should be taken from the final recipient database
        receiverPartyEndpoint = pmodeProvider.getReceiverPartyEndpoint(party, finalRecipient);
        assertEquals(finalRecipientURL, receiverPartyEndpoint);

        //clear the cache to simulate a second server which doesn't have the endpoint URL in the cache
        finalRecipientService.clearFinalRecipientAccessPointUrlsCache();

        //the retrieved URL should be taken from the final recipient database
        receiverPartyEndpoint = pmodeProvider.getReceiverPartyEndpoint(party, finalRecipient);
        assertEquals(finalRecipientURL, receiverPartyEndpoint);
    }

    @Test
    public void testX() throws XmlProcessingException, IOException {
        String selfParty = "domibus-blue";
        uploadPmode();
        final CachingPModeProvider pmodeProvider = (CachingPModeProvider) pModeProviderFactory.createDomainPModeProvider(domainContextProvider.getCurrentDomain());

        List<String> list = pModeProvider.findPartiesByInitiatorServiceAndAction("domibus-blue", Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION, getPushMeps());
        assertTrue(list.size() == 1);
        assertTrue(list.contains("domibus-red"));

        List<String> list2 = pModeProvider.findPartiesByResponderServiceAndAction("domibus-red", Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION, getPushMeps());
        assertTrue(list2.size() == 1);
        assertTrue(list2.contains("domibus-blue"));
    }

    private List<MessageExchangePattern> getPushMeps() {
        List<MessageExchangePattern> meps = new ArrayList<>();
        meps.add(MessageExchangePattern.ONE_WAY_PUSH);
        meps.add(MessageExchangePattern.TWO_WAY_PUSH_PUSH);
        meps.add(MessageExchangePattern.TWO_WAY_PUSH_PULL);
        meps.add(MessageExchangePattern.TWO_WAY_PULL_PUSH);
        return meps;
    }

    @Test
    public void checkMpcMismatch() {
        LegConfiguration legConfiguration = new LegConfiguration();
        final Mpc mpc = new Mpc();
        mpc.setQualifiedName("defaultMpc1");
        legConfiguration.setDefaultMpc(mpc);
        LegFilterCriteria legFilterCriteria = new LegFilterCriteria(null, null, null, null, null, null, null, null, "defaultMpc");
        final CachingPModeProvider pmodeProvider = (CachingPModeProvider) pModeProviderFactory.createDomainPModeProvider(domainContextProvider.getCurrentDomain());

        Set<String> mismatchedMPcs = new HashSet<>();
        String DOMIBUS_PMODE_LEGCONFIGURATION_MPC_VALIDATION_ENABLED = "domibus.pmode.legconfiguration.mpc.validation.enabled";
        DomibusProperty initialValue = configurationPropertyResourceHelper.getProperty(DOMIBUS_PMODE_LEGCONFIGURATION_MPC_VALIDATION_ENABLED);
        configurationPropertyResourceHelper.setPropertyValue(DOMIBUS_PMODE_LEGCONFIGURATION_MPC_VALIDATION_ENABLED, true, "false");

        boolean matchedMpc = pmodeProvider.checkMpcMismatch(legConfiguration, legFilterCriteria, mismatchedMPcs);

        assertEquals(mismatchedMPcs.size(), 1);
        assertFalse(matchedMpc);
        configurationPropertyResourceHelper.setPropertyValue(DOMIBUS_PMODE_LEGCONFIGURATION_MPC_VALIDATION_ENABLED, true, initialValue.getValue());
    }

    @Test
    public void filterMatchingLegConfigurations() {
        LegConfiguration legConfiguration1 = new LegConfiguration();
        LegConfiguration legConfiguration2 = new LegConfiguration();

        List<Process> matchingProcessesList = new ArrayList<>();
        Process process = new Process();
        process.setName("tc1Process");
        LinkedHashSet<LegConfiguration> candidateLegs = new LinkedHashSet<>();

        legConfiguration1.setName("leg0");
        final Mpc mpc1 = new Mpc();
        mpc1.setQualifiedName("defaultMpc");
        legConfiguration1.setDefaultMpc(mpc1);
        final Service service1 = new Service();
        service1.setName("testService0");
        service1.setValue("bdx:noprocess");
        service1.setServiceType("tc0");
        legConfiguration1.setService(service1);

        Action action1 = new Action();
        action1.setName("tc0Action");
        legConfiguration1.setAction(action1);

        legConfiguration2.setName("leg1");
        final Mpc mpc2 = new Mpc();
        mpc2.setQualifiedName("defaultMpc");
        legConfiguration2.setDefaultMpc(mpc2);
        final Service service2 = new Service();
        service2.setName("testService");
        service2.setValue("bdx:noprocess");
        service2.setServiceType("tc1");
        legConfiguration2.setService(service2);
        Action action2 = new Action();
        action2.setName("tc1Action");
        legConfiguration2.setAction(action2);

        candidateLegs.add(legConfiguration1);
        candidateLegs.add(legConfiguration2);
        process.setLegs(candidateLegs);
        matchingProcessesList.add(process);

        LegFilterCriteria legFilterCriteria = new LegFilterCriteria(null, null, null, null, null, "testService", "tc1Action", null, "defaultMpc");
        final CachingPModeProvider pmodeProvider = (CachingPModeProvider) pModeProviderFactory.createDomainPModeProvider(domainContextProvider.getCurrentDomain());

        Set<LegConfiguration> legConfigurationList = pmodeProvider.filterMatchingLegConfigurations(matchingProcessesList, legFilterCriteria);

        assertEquals(1, legConfigurationList.size());
        assertEquals("tc1Action", legConfigurationList.iterator().next().getAction().getName());
    }
}
