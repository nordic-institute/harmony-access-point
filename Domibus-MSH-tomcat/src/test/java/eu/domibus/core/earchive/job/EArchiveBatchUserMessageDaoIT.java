package eu.domibus.core.earchive.job;

import eu.domibus.AbstractIT;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.JPAConstants;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.earchive.EArchiveBatchDao;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.EArchiveBatchUserMessage;
import eu.domibus.core.earchive.EArchiveBatchUserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author François Gautier
 * @since 5.0
 */
@Ignore("EDELIVERY-8927: Bamboo - Sonar Branch plan is failing due to IT test failures")
public class EArchiveBatchUserMessageDaoIT extends AbstractIT {

    @Autowired
    EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao;

    @Autowired
    EArchiveBatchDao eArchiveBatchDao;

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    private UserMessageLog msg1;
    private UserMessageLog msg2;
    private UserMessageLog msg3;
    private EArchiveBatchEntity eArchiveBatch;

    @Before
    public void setup() {
        eArchiveBatch = new EArchiveBatchEntity();
        eArchiveBatch.setBatchId(UUID.randomUUID().toString());
        eArchiveBatch.setRequestType(EArchiveRequestType.CONTINUOUS);
        eArchiveBatch.setCreationTime(dateUtil.getUtcDate());
        eArchiveBatch.setCreatedBy("test");
        eArchiveBatchDao.create(eArchiveBatch);
        msg1 = messageDaoTestUtil.createTestMessage("msg1");
        msg2 = messageDaoTestUtil.createTestMessage("msg2");
        msg3 = messageDaoTestUtil.createTestMessage("msg3");
        userMessageLogDao.create(msg1);
        userMessageLogDao.create(msg2);
        userMessageLogDao.create(msg3);
    }

    @Test
    @Transactional
    public void create() {
        eArchiveBatchUserMessageDao.create(eArchiveBatch, Arrays.asList(new EArchiveBatchUserMessage(msg1.getEntityId(), msg1.getUserMessage().getMessageId()),
                new EArchiveBatchUserMessage(msg2.getEntityId(), msg2.getUserMessage().getMessageId()),
                new EArchiveBatchUserMessage(msg3.getEntityId(), msg3.getUserMessage().getMessageId())
        ));

        List<EArchiveBatchUserMessage> all = em.createQuery("SELECT eaum FROM EArchiveBatchUserMessage eaum", EArchiveBatchUserMessage.class)
                .getResultList();
        Assert.assertEquals(3, all.size());
    }
}
