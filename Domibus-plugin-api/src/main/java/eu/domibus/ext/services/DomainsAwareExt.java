package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public interface DomainsAwareExt {
    void onDomainAdded(final DomainDTO domain);

    void onDomainRemoved(final DomainDTO domain);
}
