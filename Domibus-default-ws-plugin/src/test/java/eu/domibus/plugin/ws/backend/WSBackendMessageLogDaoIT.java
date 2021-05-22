package eu.domibus.plugin.ws.backend;

import eu.domibus.common.JPAConstants;
import eu.domibus.common.MessageStatus;
import eu.domibus.ext.services.DateExtService;
import eu.domibus.plugin.ws.AbstractBackendWSIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
@Transactional
public class WSBackendMessageLogDaoIT extends AbstractBackendWSIT {

    @Autowired
    private WSBackendMessageLogDao wsBackendMessageLogDao;

    @Autowired
    private DateExtService dateExtService;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
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
        //todo fix it
//        Assert.assertThat(messages, CoreMatchers.hasItems(entityRetried1, entityRetried2));
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