package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;

/**
 * Service used for operations related with domains.
 *
 * @author Tiago Miguel
 * @since 4.0
 */
public interface DomainExtService {

    /**
     * Gets the domain for the provided scheduler
     * @param schedulerName the scheduler name
     * @return the domain for the provided scheduler
     */
    DomainDTO getDomainForScheduler(String schedulerName);

    /**
     * Gets the domain having the provided domain code
     *
     * @param code The domain code
     * @return the domain for the provided scheduler
     */
    DomainDTO getDomain(String code);
}
