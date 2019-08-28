package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.ext.services.DomibusSchedulerExtService;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TriggerChangeListener implements PluginPropertyChangeListener {

    @Autowired
    protected DomibusSchedulerExtService domibusSchedulerExt;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, FSPluginProperties.SEND_WORKER_INTERVAL);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        if (StringUtils.equalsIgnoreCase(propertyName, FSPluginProperties.SEND_WORKER_INTERVAL)) {
            Integer repeatInterval = Integer.valueOf(propertyValue);
            String jobName = "fsPluginSendMessagesWorkerJob";
            domibusSchedulerExt.rescheduleJob(domainCode, jobName, repeatInterval);
        }
    }

}
