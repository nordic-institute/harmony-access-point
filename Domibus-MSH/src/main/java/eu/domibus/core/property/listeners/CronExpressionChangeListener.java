package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.property.DomibusPropertyChangeListener;
import eu.domibus.quartz.DomibusQuartzStarter;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of cron expression properties
 */
@Service
public class CronExpressionChangeListener implements DomibusPropertyChangeListener {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(CronExpressionChangeListener.class);

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected ApplicationContext applicationContext;

    Map<String, String> propertyToJobMap = Stream.of(new String[][]{
            {"domibus.account.unlock.cron", "activateSuspendedUserTrigger"}, //todo: handle also the super trigger
            {"domibus.certificate.check.cron", "activateSuspendedUserTrigger"}, //todo: handle also the super trigger
            {"domibus.plugin.account.unlock.cron", "activateSuspendedPluginUserTrigger"},
            {"domibus.passwordPolicies.check.cron", "userPasswordPolicyAlertTrigger"},
            {"domibus.plugin_passwordPolicies.check.cron", "pluginUserPasswordPolicyAlertTrigger"},
            {"domibus.payload.temp.job.retention.cron", "temporaryPayloadRetentionTrigger"},
            {"domibus.msh.retry.cron", "retryWorkerTrigger"},
            {"domibus.retentionWorker.cronExpression", "retentionWorkerTrigger"},
            {"domibus.msh.pull.cron", "pullRequestTrigger"},
            {"domibus.pull.retry.cron", "pullRetryWorkerTrigger"},
            {"domibus.alert.cleaner.cron", "alertCleanerTrigger"},
            {"domibus.alert.retry.cron", "alertRetryWorkerTrigger"},
//            { "domibus.alert.super.cleaner.cron", "alertSuperCleanerTrigger" },
//            { "domibus.alert.super.retry.cron", "alertRetrySuperWorkerTrigger" },
            {"domibus.ui.replication.sync.cron", "uiReplicationTrigger"},
            {"domibus.splitAndJoin.receive.expiration.cron", "splitAndJoinExpirationTrigger"},

    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    @Override
    public boolean handlesProperty(String propertyName) {
        return propertyToJobMap.containsKey(propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        String jobName = propertyToJobMap.get(propertyName);
        if (jobName == null) {
            LOGGER.warn("Could not find the coresponding job for the property [{}]", propertyName);
            return;
        }

//        DomainSchedulerFactoryConfiguration schedulerConf = applicationContext.getBean(DomainSchedulerFactoryConfiguration.class);
        DomibusQuartzStarter domibusQuartzStarter = applicationContext.getBean(DomibusQuartzStarter.class);
        final Domain domain = domainService.getDomain(domainCode);

        try {
            domibusQuartzStarter.rescheduleJob(domain, jobName, propertyValue);
        } catch (SchedulerException ex) {
            LOGGER.error("Could not reschedule [{}] ", propertyName, ex);
        }
    }

}
