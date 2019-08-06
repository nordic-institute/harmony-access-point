package eu.domibus.core.property.listeners;

import eu.domibus.core.alerts.MailSender;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert email properties
 */
@Service
public class AlertMailChangeListener implements PluginPropertyChangeListener {

    @Autowired
    private MailSender mailSender;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithIgnoreCase(propertyName, "domibus.alert.sender.smtp.");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        mailSender.reset();
    }

}
