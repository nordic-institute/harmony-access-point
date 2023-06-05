package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;

/**
 * Responsible for domain context operations
 *
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainContextExtService {

    DomainDTO getCurrentDomain();

    /**
     * Get the current domain. Does not throw exceptions in the attempt to get the domain.
     *
     * @return the current domain or null if there is no current domain set
     */
    DomainDTO getCurrentDomainSafely();

    /**
     * Sets the domain on the current thread
     *
     * @param domain the current domain
     */
    void setCurrentDomain(DomainDTO domain);

    /**
     * Clears the domain from the current thread
     */
    void clearCurrentDomain();
}
