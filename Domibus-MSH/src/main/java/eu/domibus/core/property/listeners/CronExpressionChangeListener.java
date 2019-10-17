package eu.domibus.core.property.listeners;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.api.scheduler.DomibusSchedulerException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class CronExpressionChangeListener implements DomibusPropertyChangeListener {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(CronExpressionChangeListener.class);

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    DomibusScheduler domibusScheduler;

    Map<String, String[]> propertyToJobMap = Stream.of(new String[][]{
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
            //TODO: maybe it is better to split these 2 props into another listener
            // and there to differentiate between normal and super user jobs by knowing who changed the property
            {DOMIBUS_ALERT_CLEANER_CRON, "alertCleanerJob,alertCleanerSuperJob"},
            {DOMIBUS_ALERT_RETRY_CRON, "alertRetryJob,alertRetryJSuperJob"},
            {DOMIBUS_UI_REPLICATION_SYNC_CRON, "uiReplicationJob"},
            {DOMIBUS_SPLIT_AND_JOIN_RECEIVE_EXPIRATION_CRON, "splitAndJoinExpirationJob"},
    }).collect(Collectors.toMap(data -> data[0], data -> data[1].split(",")));

    @Override
    public boolean handlesProperty(String propertyName) {
        return propertyToJobMap.containsKey(propertyName);
    }

    @Override
    @Transactional(noRollbackFor = DomibusCoreException.class)
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        String[] jobNames = propertyToJobMap.get(propertyName);
        if (jobNames == null) {
            LOGGER.warn("Could not find the corresponding job for the property [{}]", propertyName);
            return;
        }

        final Domain domain = domainCode == null ? null : domainService.getDomain(domainCode);

        for( String jobName: jobNames) {
            try {
                domibusScheduler.rescheduleJob(domain, jobName, propertyValue);
            } catch (DomibusSchedulerException ex) {
                LOGGER.error("Could not reschedule [{}] ", propertyName, ex);
                throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not reschedule job: " + jobName, ex);
            }
        }
    }
}
