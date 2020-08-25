package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.ext.services.DomibusSchedulerExtService;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.*;

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

    private Map<String, String> propertyToJobMap = Stream.of(new String[][]{
            {PROPERTY_PREFIX + SEND_WORKER_INTERVAL, "fsPluginSendMessagesWorkerJob"},
            {PROPERTY_PREFIX + SENT_PURGE_WORKER_CRONEXPRESSION, "fsPluginPurgeSentWorkerJob"},
            {PROPERTY_PREFIX + FAILED_PURGE_WORKER_CRONEXPRESSION, "fsPluginPurgeFailedWorkerJob"},
            {PROPERTY_PREFIX + RECEIVED_PURGE_WORKER_CRONEXPRESSION, "fsPluginPurgeReceivedWorkerJob"},
            {PROPERTY_PREFIX + LOCKS_PURGE_WORKER_CRONEXPRESSION, "fsPluginPurgeLocksWorkerJob"},
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    @Override
    public boolean handlesProperty(String propertyName) {
        return propertyToJobMap.containsKey(propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        String jobName = propertyToJobMap.get(propertyName);
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
