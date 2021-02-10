package eu.domibus.ext.services;

import eu.domibus.ext.exceptions.CacheExtServiceException;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
public interface CacheExtService {

    void evictCaches() throws CacheExtServiceException;
}
