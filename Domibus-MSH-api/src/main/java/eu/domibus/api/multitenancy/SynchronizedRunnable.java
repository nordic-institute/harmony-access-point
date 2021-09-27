package eu.domibus.api.multitenancy;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockTimeoutException;
import javax.persistence.NoResultException;

/**
 * Wrapper for the Runnable class to be executed. Attempts to lock via db record and in case it succeeds it runs the wrapped Runnable
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public class SynchronizedRunnable implements Runnable {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SynchronizedRunnable.class);

    private SynchronizationService synchronizationService;
    private String lockKey;
    private Runnable runnable;

    public SynchronizedRunnable(Runnable runnable, String lockKey, SynchronizationService synchronizationService) {
        this.runnable = runnable;
        this.lockKey = lockKey;
        this.synchronizationService = synchronizationService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void run() {
        LOG.trace("Trying to lock [{}]", lockKey);

        try {
            // if this blocks, it means that another process has a write lock on the db record
            synchronizationService.acquireLock(lockKey);
            LOG.trace("Acquired lock on key [{}]", lockKey);

            LOG.trace("Start executing task");
            runnable.run();
            LOG.trace("Finished executing task");
        } catch (NoResultException nre) {
            throw new DomainTaskException(String.format("Lock key [%s] not found!", lockKey), nre);
        } catch (LockTimeoutException lte) {
            LOG.warn("[{}] key lock could not be acquired. It is probably used by another process.", lockKey, lte);
        } catch (Exception ex) {
            LOG.error("Error while running synchronized task.", ex);
        }
    }
}
