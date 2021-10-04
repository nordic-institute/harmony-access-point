package eu.domibus.api.multitenancy.lock;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface SynchronizationService {

    void acquireLock(String lockKey);

}
