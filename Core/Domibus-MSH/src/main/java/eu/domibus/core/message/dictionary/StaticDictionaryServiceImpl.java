package eu.domibus.core.message.dictionary;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class StaticDictionaryServiceImpl implements StaticDictionaryService {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(StaticDictionaryServiceImpl.class);

    protected StaticDictionaryServiceHelper staticDictionaryServiceHelper;
    protected DomibusConfigurationService domibusConfigurationService;
    protected DomainTaskExecutor domainTaskExecutor;
    protected DomainService domainService;

    public StaticDictionaryServiceImpl(StaticDictionaryServiceHelper staticDictionaryServiceHelper, DomibusConfigurationService domibusConfigurationService, DomainTaskExecutor domainTaskExecutor, DomainService domainService) {
        this.staticDictionaryServiceHelper = staticDictionaryServiceHelper;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainTaskExecutor = domainTaskExecutor;
        this.domainService = domainService;
    }

    public void createStaticDictionaryEntries() {
        LOG.debug("Start checking and creating static dictionary entries if missing");

        Runnable createEntriesCall = createEntriesCall();

        if (domibusConfigurationService.isSingleTenantAware()) {
            LOG.debug("Start checking and creating static dictionary entries in single tenancy mode");
            createEntriesCall.run();
            return;
        }

        final List<Domain> domains = domainService.getDomains();
        createEntries(domains);
    }

    @Override
    public void onDomainAdded(final Domain domain) {
        createEntries(domain);
    }

    @Override
    public void onDomainRemoved(Domain domain) {
        // I'd say nothing to do here
    }

    private void createEntries(Domain domain) {
        createEntries(Arrays.asList(domain));
    }

    private void createEntries(List<Domain> domains) {
        Runnable createEntriesCall = createEntriesCall();
        for (Domain domain : domains) {
            LOG.debug("Start checking and creating static dictionary entries for domain [{}]", domain);
            domainTaskExecutor.submit(createEntriesCall, domain, true, 3L, TimeUnit.MINUTES);
        }
    }

    private Runnable createEntriesCall() {
        return () -> {
            try {
                staticDictionaryServiceHelper.createEntries();
            } catch (Exception e) {
                LOG.error("Error while creating static dictionary entries", e);
            }
        };
    }

}
