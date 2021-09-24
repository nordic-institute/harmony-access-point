package eu.domibus.core.earchive;

import eu.domibus.AbstractIT;
import eu.domibus.common.JPAConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Transactional
public class EArchiveBatchDaoIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveBatchDaoIT.class);

    @Autowired
    EArchiveBatchDao eArchiveBatchDao;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;
    private EArchiveBatch firstContinuous;
    private EArchiveBatch secondContinuous;
    private EArchiveBatch firstManual;

    @Before
    @Transactional
    public void setup() {
        firstContinuous = new EArchiveBatch();
        secondContinuous = new EArchiveBatch();
        firstManual = new EArchiveBatch();

        create(firstContinuous, 10L, RequestType.CONTINUOUS);
        create(secondContinuous, 20L, RequestType.CONTINUOUS);
        create(firstManual, 30L, RequestType.MANUAL);
    }

    private void create(EArchiveBatch eArchiveBatch, Long lastPkUserMessage, RequestType continuous) {
        eArchiveBatch.setLastPkUserMessage(lastPkUserMessage);
        eArchiveBatch.setEArchiveBatchStatus(EArchiveBatchStatus.COMPLETED);
        eArchiveBatch.setRequestType(continuous);
        eArchiveBatchDao.merge(eArchiveBatch);
    }

    @Test
    public void findLastEntityIdArchived_notFound() {
        em.createQuery("delete from EArchiveBatch batch " +
                        "where batch.requestType = eu.domibus.core.earchive.RequestType.CONTINUOUS")
                .executeUpdate();

        Long lastEntityIdArchived = eArchiveBatchDao.findLastEntityIdArchived();

        Assert.assertNull(lastEntityIdArchived);
    }

    @Test
    public void findLastEntityIdArchived_found() {
        Long lastEntityIdArchived = eArchiveBatchDao.findLastEntityIdArchived();

        Assert.assertEquals((Long) 20L, lastEntityIdArchived);
    }
}
