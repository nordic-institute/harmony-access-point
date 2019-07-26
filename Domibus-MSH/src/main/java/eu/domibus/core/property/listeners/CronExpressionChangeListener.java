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

import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;

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

    @Autowired
    DomibusQuartzStarter domibusQuartzStarter;

    Map<String, String> propertyToJobMap = Stream.of(new String[][]{
            {DOMIBUS_ACCOUNT_UNLOCK_CRON, "activateSuspendedUsersJob"}, //todo: handle also the super Job
            {DOMIBUS_CERTIFICATE_CHECK_CRON, "saveCertificateAndLogRevocationJob"},
            {DOMIBUS_PLUGIN_ACCOUNT_UNLOCK_CRON, "activateSuspendedPluginUsersJob"},
            {DOMIBUS_PASSWORD_POLICIES_CHECK_CRON, "userPasswordPolicyAlertJob"},
            {DOMIBUS_PLUGIN_PASSWORD_POLICIES_CHECK_CRON, "pluginUserPasswordPolicyAlertJob"},
            {DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_CRON, "temporaryPayloadRetentionJob"},
            {DOMIBUS_MSH_RETRY_CRON, "retryWorkerJob"},
            {DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION, "retentionWorkerJob"},
            {DOMIBUS_MSH_PULL_CRON, "pullRequestWorkerJob"},
            {DOMIBUS_PULL_RETRY_CRON, "pullRetryWorkerJob"},
            {DOMIBUS_ALERT_CLEANER_CRON, "alertCleanerJob"},
            {DOMIBUS_ALERT_RETRY_CRON, "alertRetryJob"},
            {DOMIBUS_ALERT_SUPER_CLEANER_CRON, "alertCleanerSuperJob"},
            {DOMIBUS_ALERT_SUPER_RETRY_CRON, "alertRetryJSuperJob"},
            {DOMIBUS_UI_REPLICATION_SYNC_CRON, "uiReplicationJob"},
            {DOMIBUS_SPLIT_AND_JOIN_RECEIVE_EXPIRATION_CRON, "splitAndJoinExpirationJob"},

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

        final Domain domain = domainCode == null ? null : domainService.getDomain(domainCode);

        try {
            domibusQuartzStarter.rescheduleJob(domain, jobName, propertyValue);
        } catch (SchedulerException ex) {
            LOGGER.error("Could not reschedule [{}] ", propertyName, ex);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not reschedule job: " + jobName, ex);
        }
    }

}
