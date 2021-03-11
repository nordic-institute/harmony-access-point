package eu.domibus.ext.delegate.services.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainTaskExtExecutor;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class DomainTaskExecutorExtDelegate implements DomainTaskExtExecutor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainTaskExecutorExtDelegate.class);

    protected DomibusExtMapper domibusExtMapper;

    protected DomainTaskExecutor domainTaskExecutor;

    public DomainTaskExecutorExtDelegate(DomibusExtMapper domibusExtMapper, DomainTaskExecutor domainTaskExecutor) {
        this.domibusExtMapper = domibusExtMapper;
        this.domainTaskExecutor = domainTaskExecutor;
    }

    @Override
    public void submitLongRunningTask(Runnable task, Runnable errorHandler, DomainDTO domainDTO) {
        LOG.trace("Submitting long running task with error handler for domain [{}]", domainDTO);

        final Domain domain = domibusExtMapper.domainDTOToDomain(domainDTO);
        domainTaskExecutor.submitLongRunningTask(task, errorHandler, domain);

        LOG.trace("Submitted long running task with error handler for domain [{}]", domainDTO);
    }

    @Override
    public void submitLongRunningTask(Runnable task, DomainDTO domainDTO) {
        LOG.trace("Submitting long running task for domain [{}]", domainDTO);

        final Domain domain = domibusExtMapper.domainDTOToDomain(domainDTO);
        domainTaskExecutor.submitLongRunningTask(task, domain);

        LOG.trace("Submitted long running task for domain [{}]", domainDTO);
    }
}
