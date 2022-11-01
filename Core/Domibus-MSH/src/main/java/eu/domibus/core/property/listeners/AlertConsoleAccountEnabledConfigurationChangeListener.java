package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
//import eu.domibus.core.alerts.configuration.account.enabled.console.ConsoleAccountEnabledConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Handles the change of alert properties that are related to login failure configuration for console users
 */
@Service
public class AlertConsoleAccountEnabledConfigurationChangeListener implements DomibusPropertyChangeListener {

//    @Autowired
//    private ConsoleAccountEnabledConfigurationManager consoleAccountEnabledConfigurationManager;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithAny(propertyName,
                DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_PREFIX);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        AlertType.USER_ACCOUNT_ENABLED.getConfigurationManager().reset();
//        consoleAccountEnabledConfigurationManager.reset();
    }
}
