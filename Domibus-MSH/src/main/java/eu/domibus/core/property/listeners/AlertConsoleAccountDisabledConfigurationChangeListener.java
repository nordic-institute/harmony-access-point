package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.alerts.configuration.account.disabled.console.ConsoleAccountDisabledConfigurationManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert properties that are related to login failure configuration for console users
 */
@Service
public class AlertConsoleAccountDisabledConfigurationChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    ConsoleAccountDisabledConfigurationManager consoleAccountDisabledConfigurationManager;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithAny(propertyName,
                DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_PREFIX);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        consoleAccountDisabledConfigurationManager.reset();
    }
}
