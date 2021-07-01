package eu.domibus.core.message.dictionary;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import javax.persistence.PersistenceException;
import java.util.concurrent.Callable;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public abstract class AbstractDictionaryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractDictionaryService.class);

    protected <T> T findOrCreateEntity(Callable<T> findTask, Callable<T> findOrCreateTask, String entityDescription) {
        T entity = callTask(findTask);
        if (entity != null) {
            return entity;
        }

        synchronized (this) {
            try {
                LOG.debug("Dictionary entry [{}] not found, calling findOrCreate...", entityDescription);
                callTask(findOrCreateTask);
            } catch (PersistenceException | DataIntegrityViolationException e) {
                if (e.getCause() instanceof ConstraintViolationException) {
                    LOG.info("Constraint violation when trying to insert dictionary entry [{}], trying again (once)...", entityDescription);
                    callTask(findOrCreateTask);
                }
                throw e;
            }
            return callTask(findTask);
        }
    }

    private <T> T callTask(Callable<T> task) {
        try {
            return task.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not find or create dictionary entry", ex);
        }
    }

}
