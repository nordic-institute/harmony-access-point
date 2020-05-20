package eu.domibus.core.property.listeners.multitenancy;

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

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of cron expression properties
 */
@Service
public class SuperCronExpressionChangeListener implements DomibusPropertyChangeListener {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(SuperCronExpressionChangeListener.class);

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    DomibusScheduler domibusScheduler;

    Map<String, String[]> propertyToJobMap = Stream.of(new String[][]{
            {DOMIBUS_PASSWORD_POLICIES_CHECK_CRON, "userPasswordPolicyAlertJob,superUserPasswordPolicyAlertJob"},
            {DOMIBUS_ACCOUNT_UNLOCK_CRON, "activateSuspendedUsersJob,activateSuspendedSuperUsersJob"},
            {DOMIBUS_ALERT_CLEANER_CRON, "alertCleanerJob,alertCleanerSuperJob"},
            {DOMIBUS_ALERT_RETRY_CRON, "alertRetryJob,alertRetryJSuperJob"},
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
        //here we have a convention: first job is domain, the second is for super-users;
        //TODO: The correct solution would be to have the same job name for both, as the job instance itself is found by looking in the corresponding BD schema
        String jobName = domain != null ? jobNames[0] : jobNames[1];
        try {
            domibusScheduler.rescheduleJob(domain, jobName, propertyValue);
        } catch (DomibusSchedulerException ex) {
            LOGGER.error("Could not reschedule [{}] ", propertyName, ex);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not reschedule job: " + jobName, ex);
        }
    }
}
