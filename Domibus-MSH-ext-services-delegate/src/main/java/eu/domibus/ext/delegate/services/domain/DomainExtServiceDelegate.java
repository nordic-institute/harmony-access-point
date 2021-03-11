package eu.domibus.ext.delegate.services.domain;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
@Service
public class DomainExtServiceDelegate implements DomainExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainExtServiceDelegate.class);

    protected DomainContextProvider domainContextProvider;

    protected DomainService domainService;

    protected DomibusExtMapper domibusExtMapper;

    public DomainExtServiceDelegate(DomainContextProvider domainContextProvider, DomainService domainService, DomibusExtMapper domibusExtMapper) {
        this.domainContextProvider = domainContextProvider;
        this.domainService = domainService;
        this.domibusExtMapper = domibusExtMapper;
    }

    @Override
    public DomainDTO getDomainForScheduler(String schedulerName) {
        return domibusExtMapper.domainToDomainDTO(domainService.getDomainForScheduler(schedulerName));
    }

    public DomainDTO getDomain(String code) {
        LOG.trace("Getting domain with code [{}]", code);

        Domain domain = domainService.getDomain(code);
        if (domain != null) {
            LOG.trace("Converting domain [{}]", domain);
            final DomainDTO domainDTO = domibusExtMapper.domainToDomainDTO(domain);
            LOG.trace("Converted domain [{}] to domainDTO [{}]", domain, domainDTO);
            return domainDTO;
        }
        LOG.trace("No domain with code [{}] found", code);
        return null;
    }
}
