package eu.domibus.core.converter;

import eu.domibus.api.party.Party;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.party.PartyResponseRo;
import eu.domibus.core.party.ProcessRo;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;

/**
 * @author François Gautier
 * @since 5.0
 */
public class PartyCoreMapperTest extends AbstractMapperTest {

    @Autowired
    private PartyCoreMapper partyCoreMapper;

    @Test
    public void testConvertPartyResponseRo() {
        PartyResponseRo toConvert = (PartyResponseRo) objectService.createInstance(PartyResponseRo.class);
        toConvert.setEntityId(new Random().nextLong()+"");

        for (ProcessRo processRo : toConvert.getProcessesWithPartyAsInitiator()) {
            processRo.setEntityId(new Random().nextLong()+"");
        }

        for (ProcessRo processRo : toConvert.getProcessesWithPartyAsResponder()) {
            processRo.setEntityId(new Random().nextLong()+"");
        }

        final Party converted = partyCoreMapper.partyResponseRoToParty(toConvert);
        final PartyResponseRo convertedBack = partyCoreMapper.partyToPartyResponseRo(converted);
        // these fields are missing in Party, fill them so the assertion works
        convertedBack.setJoinedIdentifiers(toConvert.getJoinedIdentifiers());
        convertedBack.setJoinedProcesses(toConvert.getJoinedProcesses());
        convertedBack.setCertificateContent(toConvert.getCertificateContent());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertParty() {
        Party toConvert = (Party) objectService.createInstance(Party.class);
        final PartyResponseRo converted = partyCoreMapper.partyToPartyResponseRo(toConvert);
        final Party convertedBack = partyCoreMapper.partyResponseRoToParty(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertPartyConfiguration() {
        Party toConvert = (Party) objectService.createInstance(Party.class);
        final eu.domibus.common.model.configuration.Party converted = partyCoreMapper.partyToConfigurationParty(toConvert);
        final Party convertedBack = partyCoreMapper.configurationPartyToParty(converted);
        convertedBack.setProcessesWithPartyAsInitiator(toConvert.getProcessesWithPartyAsInitiator());
        convertedBack.setProcessesWithPartyAsResponder(toConvert.getProcessesWithPartyAsResponder());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertProcess() {
        Process toConvert = (Process) objectService.createInstance(Process.class);
        final eu.domibus.api.process.Process converted = partyCoreMapper.processToProcessAPI(toConvert);
        final Process convertedBack = partyCoreMapper.processAPIToProcess(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testTrustStore() {
        TrustStoreEntry toConvert = (TrustStoreEntry) objectService.createInstance(TrustStoreEntry.class);
        final TrustStoreRO converted = partyCoreMapper.trustStoreEntryToTrustStoreRO(toConvert);
        final TrustStoreEntry convertedBack = partyCoreMapper.trustStoreROToTrustStoreEntry(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertProcessRo() {
        ProcessRo toConvert = (ProcessRo) objectService.createInstance(ProcessRo.class);
        toConvert.setEntityId(new Random().nextLong()+"");

        final eu.domibus.api.process.Process converted = partyCoreMapper.processRoToProcessAPI(toConvert);
        final ProcessRo convertedBack = partyCoreMapper.processAPIToProcessRo(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }


}