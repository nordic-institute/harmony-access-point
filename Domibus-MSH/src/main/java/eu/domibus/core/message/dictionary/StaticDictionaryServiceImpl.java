package eu.domibus.core.message.dictionary;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.NotificationStatus;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    protected MessageStatusDao messageStatusDao;
    protected NotificationStatusDao notificationStatusDao;
    protected MshRoleDao mshRoleDao;
    protected DomibusConfigurationService domibusConfigurationService;
    protected DomainTaskExecutor domainTaskExecutor;
    protected DomainService domainService;

    public StaticDictionaryServiceImpl(MessageStatusDao messageStatusDao,
                                       NotificationStatusDao notificationStatusDao,
                                       MshRoleDao mshRoleDao,
                                       DomibusConfigurationService domibusConfigurationService,
                                       DomainTaskExecutor domainTaskExecutor,
                                       DomainService domainService) {
        this.messageStatusDao = messageStatusDao;
        this.notificationStatusDao = notificationStatusDao;
        this.mshRoleDao = mshRoleDao;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainTaskExecutor = domainTaskExecutor;
        this.domainService = domainService;
    }

    @Transactional
    public void createStaticDictionaryEntries() {
        LOG.debug("Start checking and creating static dictionary entries if missing");

        if (domibusConfigurationService.isSingleTenantAware()) {
            LOG.debug("Start checking and creating static dictionary entries in single tenancy mode");
            this.createStaticDictionaryEntriesForDomain();
            return;
        }

        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            LOG.debug("Start checking and creating static dictionary entries for domain [{}]", domain);
            domainTaskExecutor.submit(() -> this.createStaticDictionaryEntriesForDomain(), domain, true, 1L, TimeUnit.MINUTES);
        }
    }

    protected void createStaticDictionaryEntriesForDomain() {
        Arrays.stream(MessageStatus.values()).forEach(messageStatus -> messageStatusDao.findOrCreate(messageStatus));
        Arrays.stream(NotificationStatus.values()).forEach(notificationStatus -> notificationStatusDao.findOrCreate(notificationStatus));
        Arrays.stream(MSHRole.values()).forEach(mshRole -> mshRoleDao.findOrCreate(mshRole));
    }
}
