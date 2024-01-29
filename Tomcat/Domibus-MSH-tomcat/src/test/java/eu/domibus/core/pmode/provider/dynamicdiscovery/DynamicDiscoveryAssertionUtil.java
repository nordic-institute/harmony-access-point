package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.dynamicdyscovery.DynamicDiscoveryLookupEntity;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.pmode.multitenancy.MultiDomainPModeProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryServicePEPPOLConfigurationMockup.DOMAIN;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@Service
public class DynamicDiscoveryAssertionUtil {

    @Autowired
    DynamicDiscoveryLookupDao dynamicDiscoveryLookupDao;

    @Autowired
    MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    MultiDomainPModeProvider multiDomainPModeProvider;

    public void verifyNumberOfEntriesInTheTruststore(int expectedTruststoreEntriesAfterLookup) {
        List<TrustStoreEntry> trustStoreEntries = multiDomainCertificateProvider.getTrustStoreEntries(DOMAIN);
        assertEquals(expectedTruststoreEntriesAfterLookup, trustStoreEntries.size());
    }

    //verify that the party is present in the list of available parties in the Pmode
    public void verifyIfPartyIsPresentInTheListOfPartiesFromPmode(int expectedPmodePartiesAfterLookup, String partyName) {
        final List<Party> pmodePartiesList = verifyListOfPartiesFromPmodeSize(expectedPmodePartiesAfterLookup);
        final Party party1FromPmode = pmodePartiesList.stream().filter(party -> StringUtils.equals(partyName, party.getName())).findFirst().orElse(null);

        assertNotNull(party1FromPmode);
    }

    public List<Party> verifyListOfPartiesFromPmodeSize(int expectedPmodePartiesList) {
        final eu.domibus.common.model.configuration.Configuration configuration = ((DynamicDiscoveryPModeProvider) multiDomainPModeProvider.getCurrentPModeProvider()).getConfiguration();
        final List<Party> pmodePartiesList = configuration.getBusinessProcesses().getParties();
        assertEquals(expectedPmodePartiesList, pmodePartiesList.size());
        return pmodePartiesList;
    }

    //verify that the party is present in the responder parties for all process
    public void verifyIfPartyIsPresentInTheResponderPartiesForAllProcesses(int expectedResponderParties, String partyName) {
        final eu.domibus.common.model.configuration.Configuration configuration = ((DynamicDiscoveryPModeProvider) multiDomainPModeProvider.getCurrentPModeProvider()).getConfiguration();
        configuration.getBusinessProcesses().getProcesses().forEach(process -> assertTrue(processContainsResponseParty(process, partyName, expectedResponderParties)));
    }

    //verify that the final recipient was added in the database
    public void verifyThatDynamicDiscoveryLookupWasAddedInTheDatabase(int expectedFinalRecipientUrlsInDatabase, String finalRecipient, String partyNameAccessPointForFinalRecipient) {
        final List<DynamicDiscoveryLookupEntity> allDBfinalRecipientEntities = dynamicDiscoveryLookupDao.findAll();
        assertEquals(expectedFinalRecipientUrlsInDatabase, allDBfinalRecipientEntities.size());

        final List<DynamicDiscoveryLookupEntity> lookupEntities = allDBfinalRecipientEntities.stream().filter(finalRecipientEntity -> StringUtils.equals(finalRecipientEntity.getFinalRecipientValue(), finalRecipient)).collect(Collectors.toList());
        //we expect only 1 final recipient entry in the DB for the final recipient
        assertEquals(1, lookupEntities.size());
        final DynamicDiscoveryLookupEntity lookupEntity = lookupEntities.get(0);
        assertEquals(finalRecipient, lookupEntity.getFinalRecipientValue());
        assertEquals("http://localhost:9090/" + partyNameAccessPointForFinalRecipient + "/msh", lookupEntity.getFinalRecipientUrl());
    }

    protected boolean processContainsResponseParty(Process process, String partyName, int expectedResponderPartiesAfterLookup) {
        final Set<Party> responderParties = process.getResponderParties();
        assertEquals(expectedResponderPartiesAfterLookup, responderParties.size());
        return responderParties.stream().filter(party -> party.getName().equals(partyName)).findAny().isPresent();
    }
}
