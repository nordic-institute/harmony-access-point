package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class DefaultDomainsAwareExt implements DomainsAwareExt {

    @Autowired
    DomibusConfigurationExtService domibusConfigurationExtService;

    @Override
    public void onDomainAdded(DomainDTO domain) {
        domibusConfigurationExtService.loadProperties(domain);
    }

    @Override
    public void onDomainRemoved(DomainDTO domain) {

    }
}
