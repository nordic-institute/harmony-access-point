package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.ext.services.DomibusSchedulerExtService;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.*;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Stream.of;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the rescheduling of fs-plugin quartz jobs.
 */
@Component
public class TriggerChangeListener implements PluginPropertyChangeListener {

    @Autowired
    protected DomibusSchedulerExtService domibusSchedulerExt;

    public static final Map<String, String> CRON_PROPERTY_NAMES_TO_JOB_MAP = unmodifiableMap(of(
            new String[][]{
                    {SEND_WORKER_INTERVAL, "fsPluginSendMessagesWorkerJob"},
                    {SENT_PURGE_WORKER_CRONEXPRESSION, "fsPluginPurgeSentWorkerJob"},
                    {FAILED_PURGE_WORKER_CRONEXPRESSION, "fsPluginPurgeFailedWorkerJob"},
                    {RECEIVED_PURGE_WORKER_CRONEXPRESSION, "fsPluginPurgeReceivedWorkerJob"},
                    {LOCKS_PURGE_WORKER_CRONEXPRESSION, "fsPluginPurgeLocksWorkerJob"},
            }).collect(Collectors.toMap(data -> data[0], data -> data[1])));

    @Override
    public boolean handlesProperty(String propertyName) {
        return CRON_PROPERTY_NAMES_TO_JOB_MAP.containsKey(propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        String jobName = CRON_PROPERTY_NAMES_TO_JOB_MAP.get(propertyName);
        if (StringUtils.endsWithIgnoreCase(propertyName, SEND_WORKER_INTERVAL)) {
            rescheduleWithRepeatInterval(domainCode, jobName, propertyValue);
        } else {
            rescheduleWithCronExpression(domainCode, jobName, propertyValue);
        }
    }

    private void rescheduleWithCronExpression(String domainCode, String jobName, String cronExpression) {
        domibusSchedulerExt.rescheduleJob(domainCode, jobName, cronExpression);
    }

    protected void rescheduleWithRepeatInterval(String domainCode, String jobName, String repeatInterval) {
        Integer interval = 0;
        try {
            interval = Integer.valueOf(repeatInterval);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid repeat interval: " + repeatInterval, e);
        }

        domibusSchedulerExt.rescheduleJob(domainCode, jobName, interval);
    }

}
