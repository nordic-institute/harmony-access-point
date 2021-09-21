package eu.domibus.core.earchive.job;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.earchive.EArchiveBatch;
import eu.domibus.core.earchive.EArchiveBatchDao;
import eu.domibus.core.earchive.EArchiveBatchStatus;
import eu.domibus.core.earchive.RequestType;
import eu.domibus.core.message.UserMessageLogDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class EArchiveBatchDaoIT extends AbstractIT {

    @Autowired
    EArchiveBatchDao eArchiveBatchDao;

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    private UserMessageLog msg1;
    private EArchiveBatch eArchiveBatch;

    @Before
    public void setup() {
        eArchiveBatch = createBatch(1L);
        eArchiveBatchDao.create(eArchiveBatch);
        eArchiveBatchDao.create(createBatch(2L));
        msg1 = messageDaoTestUtil.createTestMessage("msg1");
        userMessageLogDao.create(msg1);
    }

    private EArchiveBatch createBatch(long lastPkUserMessage) {
        EArchiveBatch eArchiveBatch = new EArchiveBatch();
        eArchiveBatch.setEArchiveBatchStatus(EArchiveBatchStatus.STARTING);
        eArchiveBatch.setBatchId(UUID.randomUUID().toString());
        eArchiveBatch.setRequestType(RequestType.CONTINUOUS);
        eArchiveBatch.setCreationTime(dateUtil.getUtcDate());
        eArchiveBatch.setCreatedBy("test");
        eArchiveBatch.setLastPkUserMessage(lastPkUserMessage);
        return eArchiveBatch;
    }

    @Test
    @Transactional
    public void findEArchiveBatchByBatchId() {
        EArchiveBatch eArchiveBatchByBatchId = eArchiveBatchDao.findEArchiveBatchByBatchId(eArchiveBatch.getEntityId());

        Assert.assertNotNull(eArchiveBatchByBatchId);
    }

    @Test
    @Transactional
    public void findLastEntityIdArchived() {
        Long result = eArchiveBatchDao.findLastEntityIdArchived();
        Assert.assertEquals(2L, (long) result);
    }
}
