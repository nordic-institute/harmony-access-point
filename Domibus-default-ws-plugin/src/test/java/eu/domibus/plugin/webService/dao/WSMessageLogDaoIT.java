package eu.domibus.plugin.webService.dao;

import eu.domibus.plugin.webService.WSPluginDaoTestConfig;
import eu.domibus.plugin.webService.entity.WSMessageLogEntity;
import eu.domibus.test.dao.InMemoryDataBaseConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class, WSPluginDaoTestConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class WSMessageLogDaoIT {

    @Autowired
    private WSMessageLogDao wsMessageLogDao;

    @PersistenceContext(unitName = "domibusJTA")
    private javax.persistence.EntityManager em;


    @Before
    public void setUp() throws Exception {
        WSMessageLogEntity entity1 = new WSMessageLogEntity();
        entity1.setMessageId("messageID_1");
        entity1.setOriginalSender("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1");
        entity1.setFinalRecipient("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4");
        entity1.setFromPartyId("domibus-blue");
        entity1.setReceived(DateUtils.addDays(new Date(), -3));
        wsMessageLogDao.create(entity1);

        WSMessageLogEntity entity2 = new WSMessageLogEntity();
        entity2.setMessageId("messageID_2");
        entity2.setRefToMessageId("refToMessageID_2");
        entity2.setOriginalSender("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1");
        entity2.setFinalRecipient("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4");
        entity2.setFromPartyId("domibus-blue");
        entity2.setReceived(DateUtils.addDays(new Date(), -2));
        wsMessageLogDao.create(entity2);


    }

    @Test
    public void findByMessageId_notFound() {
        WSMessageLogEntity byMessageId = wsMessageLogDao.findByMessageId("");
        Assert.assertNull(byMessageId);
    }

    @Test
    @Transactional
    public void findByMessageId_findOne() {

        WSMessageLogEntity entity = new WSMessageLogEntity();
        entity.setMessageId("messageId");
        wsMessageLogDao.create(entity);
        em.flush();

        WSMessageLogEntity byMessageId = wsMessageLogDao.findByMessageId("messageId");
        Assert.assertNotNull(byMessageId);
    }

    @Test
    @Transactional
    public void findAll_WithFilter() {
       List<WSMessageLogEntity> wsMessageLogEntityList =  wsMessageLogDao.findAllWithFilter(null, "domibus-blue", null,
                null, null, null, null, null, 0);
       Assert.assertTrue(CollectionUtils.isNotEmpty(wsMessageLogEntityList));
       Assert.assertEquals(2, wsMessageLogEntityList.size());
       Assert.assertEquals("messageID_1", wsMessageLogEntityList.get(0).getMessageId());

        wsMessageLogEntityList =  wsMessageLogDao.findAllWithFilter(null, "domibus-blue", null,
                "refToMessageID_2", null, null, null, null, 0);
        Assert.assertTrue(CollectionUtils.isNotEmpty(wsMessageLogEntityList));
        Assert.assertEquals(1, wsMessageLogEntityList.size());
        Assert.assertEquals("messageID_2", wsMessageLogEntityList.get(0).getMessageId());


    }
}