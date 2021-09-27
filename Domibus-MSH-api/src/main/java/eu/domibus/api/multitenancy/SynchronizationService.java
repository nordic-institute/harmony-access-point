package eu.domibus.api.multitenancy;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface SynchronizationService {

    void acquireLock(String lockKey);

}
