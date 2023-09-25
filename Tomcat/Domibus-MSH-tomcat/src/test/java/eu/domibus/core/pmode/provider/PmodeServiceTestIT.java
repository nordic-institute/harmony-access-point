package eu.domibus.core.pmode.provider;

import eu.domibus.AbstractIT;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryLookupDao;
import eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryLookupService;
import eu.domibus.test.common.PKIUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
@Transactional
public class PmodeServiceTestIT extends AbstractIT {


    @Autowired
    protected DynamicDiscoveryLookupDao dynamicDiscoveryLookupDao;

    @Autowired
    protected DynamicDiscoveryLookupService dynamicDiscoveryLookupService;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    PModeService pModeService;

    @Before
    public void setUp() throws Exception {
        dynamicDiscoveryLookupDao.deleteAll(dynamicDiscoveryLookupDao.findAll());
        domibusPropertyProvider.setProperty(domainContextProvider.getCurrentDomain(), DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, "true");
    }

    @After
    public void clean() {
        domibusPropertyProvider.setProperty(domainContextProvider.getCurrentDomain(), DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, "false");
    }

    @Test
    public void testGetReceiverEndpointURLFromParty() {
        //no final recipients saved in the database
        assertTrue(dynamicDiscoveryLookupDao.findAll().isEmpty());

        String finalRecipient = "0001:recipient1";
        Party party = new Party();
        party.setName("domibus-blue");
        final String partyEndpoint = "http://localhost:8080/domibus/services/msh?domain=default";
        party.setEndpoint(partyEndpoint);

        final String receiverPartyEndpoint = pModeService.getReceiverPartyEndpoint(party.getName(), party.getEndpoint(), finalRecipient);
        //no final recipients saved in the database after we retrieved the URL
        assertTrue(dynamicDiscoveryLookupDao.findAll().isEmpty());
        assertEquals(partyEndpoint, receiverPartyEndpoint);
    }

    @Test
    public void testGetReceiverEndpointURLFromFinalRecipientEndpointURL() {
        //no final recipients saved in the database
        assertTrue(dynamicDiscoveryLookupDao.findAll().isEmpty());

        String finalRecipient = "0001:recipient1";
        String finalRecipientURL = "http://localhost:8080/domibus/services/msh?domain=domain1";
        Party party = new Party();
        final String partyName = "domibus-blue";
        party.setName(partyName);
        final String partyEndpoint = "http://localhost:8080/domibus/services/msh?domain=default";
        party.setEndpoint(partyEndpoint);

        PKIUtil pkiUtil = new PKIUtil();
        final X509Certificate partyCertificate = pkiUtil.createCertificateWithSubject(BigInteger.valueOf(111), "CN=" + partyName + ",OU=Domibus,O=eDelivery,C=EU");

        //the final recipient URL is not saved in DB; the endpoint URL is retrieved from the Pmode Party
        String receiverPartyEndpoint = pModeService.getReceiverPartyEndpoint(party.getName(), party.getEndpoint(), finalRecipient);
        assertEquals(partyEndpoint, receiverPartyEndpoint);

        //we save the final recipient URL in the database
        dynamicDiscoveryLookupService.saveDynamicDiscoveryLookupTime(finalRecipient, finalRecipientURL, partyName, "myPartyType", Arrays.asList("process1"), partyName, partyCertificate);

        //final recipient should be saved in the database after we retrieved the URL
        assertEquals(1, dynamicDiscoveryLookupDao.findAll().size());

        //the retrieved URL should be taken from the final recipient database
        receiverPartyEndpoint = pModeService.getReceiverPartyEndpoint(party.getName(), party.getEndpoint(), finalRecipient);
        assertEquals(finalRecipientURL, receiverPartyEndpoint);

        //clear the cache to simulate a second server which doesn't have the endpoint URL in the cache
        dynamicDiscoveryLookupService.clearFinalRecipientAccessPointUrlsCache();

        //the retrieved URL should be taken from the final recipient database
        receiverPartyEndpoint = pModeService.getReceiverPartyEndpoint(party.getName(), party.getEndpoint(), finalRecipient);
        assertEquals(finalRecipientURL, receiverPartyEndpoint);
    }
}
