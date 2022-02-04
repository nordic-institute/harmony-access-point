package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.core.spring.lock.LockDao;
import eu.domibus.core.spring.lock.LockEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;

import static eu.domibus.core.spring.DomibusContextRefreshedListener.SYNC_LOCK_KEY;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Ignore("EDELIVERY-8927: Bamboo - Sonar Branch plan is failing due to IT test failures")
public class LockDaoIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(LockDaoIT.class);

    @Autowired
    private LockDao lockDao;

    @Test
    @Transactional
    public void findSyncLock() {
        final LockEntity lock = lockDao.findByLockKeyWithLock(SYNC_LOCK_KEY);
        assertNotNull(lock);
    }

    @Test(expected = NoResultException.class)
    @Transactional
    public void doNotFindOtherLock() {
        final LockEntity lock = lockDao.findByLockKeyWithLock("non-existent-lock");
        Assert.fail();
    }

}
