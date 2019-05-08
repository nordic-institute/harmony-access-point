package eu.domibus.ext.delegate.services.domain;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
@Service
public class DomainExtServiceDelegate implements DomainExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainExtServiceDelegate.class);

    @Autowired
    DomainContextProvider domainContextProvider;

    @Autowired
    DomainService domainService;

    @Autowired
    DomainExtConverter domainConverter;

    @Override
    public DomainDTO getDomainForScheduler(String schedulerName) {
        return domainConverter.convert(domainService.getDomainForScheduler(schedulerName), DomainDTO.class);
    }

    public DomainDTO getDomain(String code) {
        LOG.trace("Getting domain with code [{}]", code);

        Domain domain = domainService.getDomain(code);
        if (domain != null) {
            LOG.trace("Converting domain [{}]", domain);
            final DomainDTO domainDTO = domainConverter.convert(domain, DomainDTO.class);
            LOG.trace("Converted domain [{}] to domainDTO [{}]", domain, domainDTO);
            return domainDTO;
        }
        LOG.trace("No domain with code [{}] found", code);
        return null;
    }
}
