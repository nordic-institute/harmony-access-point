package eu.domibus.plugin.ws.backend;

import eu.domibus.common.MessageStatus;
import eu.domibus.plugin.ws.WSPluginDaoTestConfig;
import eu.domibus.test.dao.InMemoryDataBaseConfig;
import org.hamcrest.CoreMatchers;
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
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class, WSPluginDaoTestConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
@Transactional
public class WSBackendMessageLogDaoIT {

    @Autowired
    private WSBackendMessageLogDao wsBackendMessageLogDao;

    @PersistenceContext(unitName = "domibusEM")
    private javax.persistence.EntityManager em;
    private WSBackendMessageLogEntity entityFailed;
    private WSBackendMessageLogEntity entityRetried1;
    private WSBackendMessageLogEntity entityRetried2;

    @Before
    public void setUp() {
        entityFailed = create(WSBackendMessageStatus.SEND_FAILURE);
        entityRetried1 = create(WSBackendMessageStatus.WAITING_FOR_RETRY);
        entityRetried2 = create(WSBackendMessageStatus.WAITING_FOR_RETRY);
        createEntityAndFlush(Arrays.asList(entityFailed,
                entityRetried1,
                entityRetried2));
    }

    @Test
    public void findByMessageId_notFound() {
        WSBackendMessageLogEntity byMessageId = wsBackendMessageLogDao.findByMessageId("");
        Assert.assertNull(byMessageId);
    }

    @Test
    public void findByMessageId_findOne() {
        WSBackendMessageLogEntity byMessageId = wsBackendMessageLogDao.findByMessageId(entityFailed.getMessageId());
        Assert.assertNotNull(byMessageId);
    }

    @Test
    public void findRetryMessages() {
        List<WSBackendMessageLogEntity> messages = wsBackendMessageLogDao.findRetryMessages();
        Assert.assertNotNull(messages);
        Assert.assertEquals(2, messages.size());
        Assert.assertThat(messages, CoreMatchers.hasItems(entityRetried1, entityRetried2));
    }

    private void createEntityAndFlush(List<WSBackendMessageLogEntity> entities) {
        for (WSBackendMessageLogEntity entity : entities) {
            wsBackendMessageLogDao.create(entity);
        }
        em.flush();
    }

    private WSBackendMessageLogEntity create(WSBackendMessageStatus status) {
        WSBackendMessageLogEntity entity = new WSBackendMessageLogEntity();
        entity.setMessageId(UUID.randomUUID().toString());
        entity.setMessageStatus(MessageStatus.WAITING_FOR_RETRY);
        entity.setBackendMessageStatus(status);
        entity.setSendAttempts(1);
        entity.setSendAttemptsMax(3);
        entity.setNextAttempt(yesterday());
        return entity;
    }

    private Date yesterday() {
        return Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant());
    }
}