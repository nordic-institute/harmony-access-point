package eu.domibus.api.multitenancy.lock;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public interface SynchronizationService {

    void acquireLock(String lockKey);

}
