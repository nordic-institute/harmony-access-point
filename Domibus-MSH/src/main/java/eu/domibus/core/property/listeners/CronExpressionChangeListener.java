package eu.domibus.core.property.listeners;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
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
public class CronExpressionChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(CronExpressionChangeListener.class);

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected ApplicationContext applicationContext;

    Map<String, String> propertyToJobMap = Stream.of(new String[][]{
            {"domibus.account.unlock.cron", "activateSuspendedUsersJob"}, //todo: handle also the super Job
            {"domibus.certificate.check.cron", "saveCertificateAndLogRevocationJob"},
            {"domibus.plugin.account.unlock.cron", "activateSuspendedPluginUsersJob"},
            {"domibus.passwordPolicies.check.cron", "userPasswordPolicyAlertJob"},
            {"domibus.plugin_passwordPolicies.check.cron", "pluginUserPasswordPolicyAlertJob"},
            {"domibus.payload.temp.job.retention.cron", "temporaryPayloadRetentionJob"},
            {"domibus.msh.retry.cron", "retryWorkerJob"},
            {"domibus.retentionWorker.cronExpression", "retentionWorkerJob"},
            {"domibus.msh.pull.cron", "pullRequestWorkerJob"},
            {"domibus.pull.retry.cron", "pullRetryWorkerJob"},
            {"domibus.alert.cleaner.cron", "alertCleanerJob"},
            {"domibus.alert.retry.cron", "alertRetryJob"},
            {"domibus.alert.super.cleaner.cron", "alertCleanerSuperJob"},
            {"domibus.alert.super.retry.cron", "alertRetryJSuperJob"},
            {"domibus.ui.replication.sync.cron", "uiReplicationJob"},
            {"domibus.splitAndJoin.receive.expiration.cron", "splitAndJoinExpirationJob"},

    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    @Override
    public boolean handlesProperty(String propertyName) {
        return propertyToJobMap.containsKey(propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        String jobName = propertyToJobMap.get(propertyName);
        if (jobName == null) {
            LOGGER.warn("Could not find the corresponding job for the property [{}]", propertyName);
            return;
        }

        DomibusQuartzStarter domibusQuartzStarter = applicationContext.getBean(DomibusQuartzStarter.class);
        final Domain domain = domainCode == null ? null : domainService.getDomain(domainCode);

        try {
            domibusQuartzStarter.rescheduleJob(domain, jobName, propertyValue);
        } catch (SchedulerException ex) {
            LOGGER.error("Could not reschedule [{}] ", propertyName, ex);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not reschedule job: " + jobName, ex);
        }
    }

}
