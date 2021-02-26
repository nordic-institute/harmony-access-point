package eu.domibus.plugin.ws.message;

import eu.domibus.plugin.ws.WSPluginDaoTestConfig;
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
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
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

    @PersistenceContext(unitName = "domibusEM")
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

        WSMessageLogEntity entity3 = new WSMessageLogEntity();
        entity3.setMessageId("messageID_3");
        entity3.setRefToMessageId("refToMessageID_3");
        entity3.setConversationId("conversationID_3");
        entity3.setOriginalSender("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1");
        entity3.setFinalRecipient("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4");
        entity3.setFromPartyId("domibus-blue2");
        entity3.setReceived(DateUtils.addDays(new Date(), -1));
        wsMessageLogDao.create(entity3);

        WSMessageLogEntity entity4 = new WSMessageLogEntity();
        entity4.setMessageId("messageID_4");
        entity4.setRefToMessageId("refToMessageID_4");
        entity4.setConversationId("conversationID_4");
        entity4.setOriginalSender("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1");
        entity4.setFinalRecipient("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4");
        entity4.setFromPartyId("domibus-blue2");
        entity4.setReceived(new Date());
        wsMessageLogDao.create(entity4);


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

        wsMessageLogEntityList =  wsMessageLogDao.findAllWithFilter(null, null, "conversationID_3",
                null, null, null, null, null, 0);
        Assert.assertTrue(CollectionUtils.isNotEmpty(wsMessageLogEntityList));
        Assert.assertEquals(1, wsMessageLogEntityList.size());
        Assert.assertEquals("messageID_3", wsMessageLogEntityList.get(0).getMessageId());

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        wsMessageLogEntityList =  wsMessageLogDao.findAllWithFilter(null, null, null,
                null, null, null, now.minus(Period.ofDays(3)), now, 0);
        Assert.assertTrue(CollectionUtils.isNotEmpty(wsMessageLogEntityList));
        Assert.assertEquals(3, wsMessageLogEntityList.size());

    }
}