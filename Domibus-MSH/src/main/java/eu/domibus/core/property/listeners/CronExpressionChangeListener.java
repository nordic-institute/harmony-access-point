package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.property.DomibusPropertyProviderImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageListenerContainerInitializer;
import eu.domibus.property.DomibusPropertyChangeListener;
import eu.domibus.quartz.DomainSchedulerFactoryConfiguration;
import eu.domibus.quartz.DomibusQuartzStarter;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Handles the change of cron expression properties
 */
@Service
public class CronExpressionChangeListener implements DomibusPropertyChangeListener {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(CronExpressionChangeListener.class);

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected ApplicationContext applicationContext;

    private String[] handledProperties = new String[]{
            "domibus.account.unlock.cron",
            "domibus.certificate.check.cron",
            "domibus.plugin.account.unlock.cron",
            "domibus.passwordPolicies.check.cron",
            "domibus.plugin_passwordPolicies.check.cron",
            "domibus.payload.temp.job.retention.cron",
            "domibus.msh.retry.cron",
            "domibus.retentionWorker.cronExpression",
            "domibus.msh.pull.cron",
            "domibus.pull.retry.cron",
            "domibus.alert.cleaner.cron",
            "domibus.alert.retry.cron",
            //"domibus.alert.super.cleaner.cron",
            //"domibus.alert.super.retry.cron",
            "domibus.ui.replication.sync.cron",
            "domibus.splitAndJoin.receive.expiration.cron",
    };

    @Override
    public boolean handlesProperty(String propertyName) {
        return Arrays.stream(handledProperties).anyMatch(p -> p.equalsIgnoreCase(propertyName));
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        DomainSchedulerFactoryConfiguration domainSchedulerFactoryConfiguration = applicationContext.getBean(DomainSchedulerFactoryConfiguration.class);
        DomibusQuartzStarter domibusQuartzStarter= applicationContext.getBean(DomibusQuartzStarter.class);
        final Domain domain = domainService.getDomain(domainCode);

        try {
            switch (propertyName) {
                case "domibus.retentionWorker.cronExpression":
                    domibusQuartzStarter.rescheduleJob(domain, "retentionWorkerJob", propertyValue);
                    break;
            }
        }catch (SchedulerException ex) {
            LOGGER.error("Could not reschedule [{}] ", propertyName, ex);
        }
    }
}
