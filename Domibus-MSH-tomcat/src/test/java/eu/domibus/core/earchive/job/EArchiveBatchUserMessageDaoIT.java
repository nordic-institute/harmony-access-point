package eu.domibus.core.earchive.job;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.JPAConstants;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.earchive.*;
import eu.domibus.core.message.UserMessageLogDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
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
    private EArchiveBatch eArchiveBatch;

    @Before
    public void setup() {
        eArchiveBatch = new EArchiveBatch();
        eArchiveBatch.setEArchiveBatchStatus(EArchiveBatchStatus.STARTING);
        eArchiveBatch.setBatchId(UUID.randomUUID().toString());
        eArchiveBatch.setRequestType(RequestType.CONTINUOUS);
        eArchiveBatch.setCreationTime(dateUtil.getUtcDate());
        eArchiveBatch.setCreatedBy("test");
        eArchiveBatchDao.create(eArchiveBatch);
        msg1 = messageDaoTestUtil.createTestMessage("msg1");
        userMessageLogDao.create(msg1);
    }

    @Test
    @Transactional
    public void create() {
        eArchiveBatchUserMessageDao.create(eArchiveBatch, msg1.getEntityId());

        List<EArchiveBatchUserMessage> all = em.createQuery("SELECT eaum FROM EArchiveBatchUserMessage eaum", EArchiveBatchUserMessage.class)
                .getResultList();
        Assert.assertEquals(1, all.size());
    }
}
