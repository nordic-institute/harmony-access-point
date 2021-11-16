package eu.domibus.core.earchive;

import eu.domibus.AbstractIT;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.common.JPAConstants;
import eu.domibus.common.MessageDaoTestUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static eu.domibus.api.earchive.EArchiveBatchStatus.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Test filter variations for querying the EArchiveBatchEntities
 * Because we are using @RunWith(value = Parameterized.class) the following rule ensures startup of the spring test context
 * - SpringMethodRule provides the instance-level and method-level functionality for TestContextManager.
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@RunWith(value = Parameterized.class)
@Transactional
public class EArchiveBatchDaoBatchFiltersIT extends AbstractIT {
    // test data
    private static final String BATCH_ID_01 = "BATCH_ID_01@" + UUID.randomUUID();

    private static final String BATCH_ID_02 = "BATCH_ID_02@" + UUID.randomUUID();

    private static final String BATCH_ID_03 = "BATCH_ID_03@" + UUID.randomUUID();

    private static final String BATCH_ID_04 = "BATCH_ID_04@" + UUID.randomUUID();
    private UserMessageLog uml1;


    @Parameterized.Parameters(name = "{index}: {0}")
    // test desc. result batchIds, filter
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {"With filter status queued ", singletonList(BATCH_ID_04), 1L, new EArchiveBatchFilter(singletonList(QUEUED), null, null, null, null, null, null, null)},
                {"With filter status exported ", singletonList(BATCH_ID_02), 1L, new EArchiveBatchFilter(singletonList(EXPORTED), null, null, null, null, null, null, null)},
                {"With filter status exported and reexported ", singletonList(BATCH_ID_02), 1L, new EArchiveBatchFilter(singletonList(EXPORTED), null, null, null, null, null, null, null)},
                {"With filter by type", singletonList(BATCH_ID_02), 1L, new EArchiveBatchFilter(singletonList(EXPORTED), singletonList(EArchiveRequestType.MANUAL), null, null, null, null, null, null)},
                // Note batches are ordered from latest to oldest
                {"With filter: request date", asList(BATCH_ID_04, BATCH_ID_03, BATCH_ID_02), 3L, new EArchiveBatchFilter(null, null, DateUtils.addDays(Calendar.getInstance().getTime(), -28), DateUtils.addDays(Calendar.getInstance().getTime(), -12), null, null, null, null)},
                {"With filter: get All ", asList(BATCH_ID_04, BATCH_ID_03, BATCH_ID_02, BATCH_ID_01), 4L, new EArchiveBatchFilter(null, null, null, null, null, null, null, null)},
                {"With filter: test page size", asList(BATCH_ID_04, BATCH_ID_03), 4L, new EArchiveBatchFilter(null, null, null, null, null, null, null, 2)},
                {"With filter: test page start", asList(BATCH_ID_02, BATCH_ID_01), 4L, new EArchiveBatchFilter(null, null, null, null, null, null, 1, 2)},
        });
    }

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    EArchiveBatchDao eArchiveBatchDao;

    @Autowired
    EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao;
    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;

    String testName;
    List<String> expectedBatchIds;
    Long total;
    EArchiveBatchFilter filter;

    public EArchiveBatchDaoBatchFiltersIT(String testName, List<String> expectedBatchIds, Long total, EArchiveBatchFilter filter) {
        this.testName = testName;
        this.expectedBatchIds = expectedBatchIds;
        this.total = total;
        this.filter = filter;
    }

    @Before
    @Transactional
    public void setup() {
        Date currentDate = Calendar.getInstance().getTime();
        uml1 = messageDaoTestUtil.createUserMessageLog("uml1-" + UUID.randomUUID(), currentDate);
        // prepare database -> create batches
        create(BATCH_ID_01, DateUtils.addDays(currentDate, -30), 1L, 10L, EArchiveRequestType.CONTINUOUS, ARCHIVED);
        create(BATCH_ID_02, DateUtils.addDays(currentDate, -25), 11L, 20L, EArchiveRequestType.MANUAL, EXPORTED);
        create(BATCH_ID_03, DateUtils.addDays(currentDate, -22), 21L, 30L, EArchiveRequestType.MANUAL, EXPIRED);
        create(BATCH_ID_04, DateUtils.addDays(currentDate, -15), 31L, 40L, EArchiveRequestType.CONTINUOUS, QUEUED);
    }

    private void create(String batchId, Date dateRequested, Long firstPkUserMessage, Long lastPkUserMessage, EArchiveRequestType continuous, EArchiveBatchStatus status) {
        EArchiveBatchEntity batch = new EArchiveBatchEntity();
        batch.setBatchId(batchId);
        batch.setDateRequested(dateRequested);
        batch.setFirstPkUserMessage(firstPkUserMessage);
        batch.setLastPkUserMessage(lastPkUserMessage);
        batch.setRequestType(continuous);
        batch.setEArchiveBatchStatus(status);

        EArchiveBatchEntity merge = eArchiveBatchDao.merge(batch);
        EArchiveBatchEntity batchCreated = em.createQuery("select batch from EArchiveBatchEntity batch where batch.batchId = :batchId", EArchiveBatchEntity.class)
                .setParameter("batchId", batchId)
                .getResultList()
                .get(0);
        List<EArchiveBatchUserMessage> select_batch_from_eArchiveBatchUserMessage_batch = em.createQuery("select batch from EArchiveBatchUserMessage batch", EArchiveBatchUserMessage.class).getResultList();

        List<EArchiveBatchUserMessage> eArchiveBatchUserMessages = new ArrayList<>();
        for (int i = firstPkUserMessage.intValue(); i < lastPkUserMessage; i++) {
            EArchiveBatchUserMessage entity = new EArchiveBatchUserMessage(uml1.getEntityId(), uml1.getUserMessage().getMessageId());
            entity.seteArchiveBatch(merge);
            EArchiveBatchUserMessage merge1 = eArchiveBatchUserMessageDao.merge(entity);
            eArchiveBatchUserMessages.add(merge1);
        }

        List<EArchiveBatchUserMessage> result = em.createQuery("select batch from EArchiveBatchUserMessage batch", EArchiveBatchUserMessage.class).getResultList();


        List<EArchiveBatchUserMessage> end = em.createQuery("select batch from EArchiveBatchUserMessage batch", EArchiveBatchUserMessage.class).getResultList();
        merge.seteArchiveBatchUserMessages(eArchiveBatchUserMessages);
        eArchiveBatchDao.update(merge);
    }

    @Test
    public void testGetBatchRequestList() {
        // given-when
        List<EArchiveBatchEntity> resultList = eArchiveBatchDao.getBatchRequestList(filter);
        // then
        Assert.assertEquals(expectedBatchIds.size(), resultList.size());
        Assert.assertArrayEquals(expectedBatchIds.toArray(), resultList.stream().map(EArchiveBatchEntity::getBatchId).toArray());
    }

    @Test
    public void testGetBatchRequestListCount() {
        // given-when
        Long count = eArchiveBatchDao.getBatchRequestListCount(filter);
        // then
        Assert.assertEquals(total, count);
    }
}