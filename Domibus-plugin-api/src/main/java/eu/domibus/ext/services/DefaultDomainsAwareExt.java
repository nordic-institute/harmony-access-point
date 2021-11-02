package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;

/**
 * @author Ion Perpegel
 * @since 5.0
 *
 * Default implementation of external module class responsible with reacting to adding and removing of domains at runtime
 */
public class DefaultDomainsAwareExt implements DomainsAwareExt {

    private final DomibusPropertyManagerExt domibusPropertyManagerExt;

    public DefaultDomainsAwareExt(DomibusPropertyManagerExt domibusPropertyManagerExt) {
        this.domibusPropertyManagerExt = domibusPropertyManagerExt;
    }

    @Override
    public void onDomainAdded(DomainDTO domain) {
        domibusPropertyManagerExt.loadProperties(domain);
    }

    @Override
    public void onDomainRemoved(DomainDTO domain) {
    }
}
