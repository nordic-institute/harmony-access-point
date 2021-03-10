package eu.domibus.ext.delegate.services.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomainContextServiceDelegate implements DomainContextExtService {

    protected final DomainContextProvider domainContextProvider;

    protected final DomibusExtMapper domibusExtMapper;

    public DomainContextServiceDelegate(DomainContextProvider domainContextProvider, DomibusExtMapper domibusExtMapper) {
        this.domainContextProvider = domainContextProvider;
        this.domibusExtMapper = domibusExtMapper;
    }

    @Override
    public DomainDTO getCurrentDomain() {
        final Domain currentDomain = domainContextProvider.getCurrentDomain();
        return domibusExtMapper.domainToDomainDTO(currentDomain);
    }

    @Override
    public DomainDTO getCurrentDomainSafely() {
        final Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        return domibusExtMapper.domainToDomainDTO(currentDomain);
    }

    @Override
    public void setCurrentDomain(DomainDTO domainDTO) {
        final Domain domain = domibusExtMapper.domainDTOToDomain(domainDTO);
        domainContextProvider.setCurrentDomain(domain);
    }

    @Override
    public void clearCurrentDomain() {
        domainContextProvider.clearCurrentDomain();
    }
}
