package eu.domibus.core.message.reliability;

import eu.domibus.AbstractIT;
import eu.domibus.core.property.DomibusPropertyResourceHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SMART_RETRY_ENABLED;
import static org.junit.Assert.*;

@Transactional
public class ReliabilityServiceIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(ReliabilityServiceIT.class);

    @Autowired
    private ReliabilityService reliabilityService;
    @Autowired
    private PartyStatusDao partyStatusDao;
    @Autowired
    DomibusPropertyResourceHelper domibusPropertyResourceHelper;


    @Before
    public void before() {
        try {
            uploadPmode(18001);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlProcessingException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Transactional
    public void isPartyReachable() {
        String testedPartyName = "one";
        createPartyStatus(testedPartyName, "SUCCESS");

        assertNotNull(partyStatusDao.findByName(testedPartyName));
        assertTrue(reliabilityService.isPartyReachable(testedPartyName));
    }

    @Test
    @Transactional
    public void isPartyNotReachable() {
        String testedPartyName = "one";
        createPartyStatus(testedPartyName, "ERROR");

        assertNotNull(partyStatusDao.findByName(testedPartyName));
        assertFalse(reliabilityService.isPartyReachable(testedPartyName));
    }

    @Test
    @Transactional
    public void isPartyReachableFirstTime() {
        String testedPartyName = "one";

        assertNull(partyStatusDao.findByName(testedPartyName)); //does not exist
        assertTrue(reliabilityService.isPartyReachable(testedPartyName)); //assume it's reachable if status entry doesn't exist yet (optimistic retry)
    }

    @Test
    @Transactional
    public void isSmartRetryEnabledForParty() {
        String testedPartyRed = "domibus-red";
        String testedPartyBlue = "domibus-blue";
        domibusPropertyResourceHelper.setPropertyValue(DOMIBUS_SMART_RETRY_ENABLED, true, testedPartyRed);

        assertTrue(reliabilityService.isSmartRetryEnabledForParty(testedPartyRed));
        assertFalse(reliabilityService.isSmartRetryEnabledForParty(testedPartyBlue));
    }

    @Test
    @Transactional
    public void isSmartRetryEnabledForLongList() {
        String testedPartyRed = "domibus-red";
        String testedPartyBlue = "domibus-blue";
        String longListOfPartiesWithDuplicates = String.join(",",testedPartyRed, testedPartyBlue,
                testedPartyRed, testedPartyBlue,
                testedPartyRed, "", "", testedPartyBlue);
        domibusPropertyResourceHelper.setPropertyValue(DOMIBUS_SMART_RETRY_ENABLED, true, longListOfPartiesWithDuplicates.toString());

        assertTrue(reliabilityService.isSmartRetryEnabledForParty(testedPartyRed));
        assertTrue(reliabilityService.isSmartRetryEnabledForParty(testedPartyBlue));
        assertFalse(reliabilityService.isSmartRetryEnabledForParty("random"));
    }



    private void createPartyStatus(String partyName, String status){
        PartyStatusEntity partyStatusEntity = new PartyStatusEntity();
        partyStatusEntity.setPartyName(partyName);
        partyStatusEntity.setConnectivityStatus(status);
        partyStatusDao.create(partyStatusEntity);
    }

}
