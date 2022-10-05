package eu.domibus.core.pmode.provider;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.participant.FinalRecipientDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
        finalRecipientService.clearFinalRecipientAccessPointUrls();

        //the endpoint URL should be retrieved from the database
        receiverPartyEndpoint = pmodeProvider.getReceiverPartyEndpoint(party, finalRecipient);
        assertEquals(finalRecipientURL, receiverPartyEndpoint);
    }
}
