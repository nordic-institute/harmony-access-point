package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.ext.services.DomibusSchedulerExtService;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TriggerChangeListener implements PluginPropertyChangeListener {

    @Autowired
    protected DomibusSchedulerExtService domibusSchedulerExt;

    private Map<String, String> propertyToJobMap = Stream.of(new String[][]{
            {FSPluginProperties.SEND_WORKER_INTERVAL, "fsPluginSendMessagesWorkerJob"},
            {FSPluginProperties.SENT_PURGE_WORKER_CRONEXPRESSION, "fsPluginPurgeSentWorkerJob"},
            {FSPluginProperties.FAILED_PURGE_WORKER_CRONEXPRESSION, "fsPluginPurgeFailedWorkerJob"},
            {FSPluginProperties.RECEIVED_PURGE_WORKER_CRONEXPRESSION, "fsPluginPurgeReceivedWorkerJob"},
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    @Override
    public boolean handlesProperty(String propertyName) {
        return propertyToJobMap.containsKey(propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        String jobName = propertyToJobMap.get(propertyName);
        if (StringUtils.equalsIgnoreCase(propertyName, FSPluginProperties.SEND_WORKER_INTERVAL)) {
            Integer repeatInterval = Integer.valueOf(propertyValue);
            domibusSchedulerExt.rescheduleJob(domainCode, jobName, repeatInterval);
        } else {
            domibusSchedulerExt.rescheduleJob(domainCode, jobName, propertyValue);
        }
    }

}
