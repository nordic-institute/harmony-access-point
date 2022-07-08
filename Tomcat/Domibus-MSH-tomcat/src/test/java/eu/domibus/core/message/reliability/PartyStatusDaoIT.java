package eu.domibus.core.message.reliability;

import eu.domibus.AbstractIT;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@Transactional
public class PartyStatusDaoIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyStatusDaoIT.class);

    @Autowired
    private PartyStatusDao partyStatusDao;

    @Test
    @Transactional
    public void findPartyByName() {
        createPartyStatus("one", "SUCCESS");
        PartyStatusEntity found = partyStatusDao.findByName("one");
        assertEquals(found.getConnectivityStatus(), "SUCCESS");
        assertEquals(found.getPartyName(), "one");

        PartyStatusEntity nonExisting = partyStatusDao.findByName("doesnotexist");
        assertNull(nonExisting);
    }

    @Test
    @Transactional
    public void existsWithName() {
        createPartyStatus("red", "ERROR");
        assertTrue(partyStatusDao.existsWithName("red"));
        assertFalse(partyStatusDao.existsWithName("ReD")); //case sensitive test
    }


    private void createPartyStatus(String partyName, String status){
        PartyStatusEntity partyStatusEntity = new PartyStatusEntity();
        partyStatusEntity.setPartyName(partyName);
        partyStatusEntity.setConnectivityStatus(status);
        partyStatusDao.create(partyStatusEntity);
    }

}
