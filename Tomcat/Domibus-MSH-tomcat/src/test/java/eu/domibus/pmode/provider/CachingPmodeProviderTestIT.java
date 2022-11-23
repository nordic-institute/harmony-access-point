package eu.domibus.core.pmode.provider;

import eu.domibus.AbstractIT;
import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.ebms3.MessageExchangePattern;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.participant.FinalRecipientDao;
import eu.domibus.messaging.XmlProcessingException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


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

    @Test
    public void testGetFinalParticipantEndpointFromParty() {
        final CachingPModeProvider pmodeProvider = (CachingPModeProvider) pModeProviderFactory.createDomainPModeProvider(domainContextProvider.getCurrentDomain());

        //no final recipients saved in the database
        assertTrue(finalRecipientDao.findAll().isEmpty());

        String finalRecipient = "0001:recipient1";
        String finalRecipientURL = "http://localhost:8080/domibus/services/msh?domain=domain1";
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
    public void testGetFinalParticipantEndpointFromFinalParticipantEndpointURL() {
        final CachingPModeProvider pmodeProvider = (CachingPModeProvider) pModeProviderFactory.createDomainPModeProvider(domainContextProvider.getCurrentDomain());

        //no final recipients saved in the database
        assertTrue(finalRecipientDao.findAll().isEmpty());

        String finalRecipient = "0001:recipient1";
        String finalRecipientURL = "http://localhost:8080/domibus/services/msh?domain=domain1";
        Party party = new Party();
        party.setName("domibus-blue");
        final String partyEndpoint = "http://localhost:8080/domibus/services/msh?domain=default";
        party.setEndpoint(partyEndpoint);

        //get the endpoint URL from Pmode Party
        String receiverPartyEndpoint = pmodeProvider.getReceiverPartyEndpoint(party, finalRecipient);
        assertEquals(partyEndpoint, receiverPartyEndpoint);

        pmodeProvider.setReceiverPartyEndpoint(finalRecipient, finalRecipientURL);
        //final recipient should be saved in the database after we retrieved the URL
        assertEquals(1, finalRecipientDao.findAll().size());

        //get the endpoint URL from the database
        receiverPartyEndpoint = pmodeProvider.getReceiverPartyEndpoint(party, finalRecipient);
        assertEquals(finalRecipientURL, receiverPartyEndpoint);

        //clear the cache to simulate a second server which doesn't have the endpoint URL in the cache
        finalRecipientService.clearFinalRecipientAccessPointUrls(domainContextProvider.getCurrentDomain());

        //the endpoint URL should be retrieved from the database
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
}