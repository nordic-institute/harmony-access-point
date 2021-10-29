package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;

/**
 * @author Ion Perpegel
 * @since 5.0
 *
 * Interface implemented by external modules that want to react to adding and removing of domains at runtime
 */
public interface DomainsAwareExt {
    void onDomainAdded(final DomainDTO domain);

    void onDomainRemoved(final DomainDTO domain);
}
