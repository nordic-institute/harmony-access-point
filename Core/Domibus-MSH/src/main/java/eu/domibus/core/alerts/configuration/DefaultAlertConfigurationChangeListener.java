package eu.domibus.core.alerts.configuration;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Generic bean that handles the change of alert properties based on Alert type
 */
public class DefaultAlertConfigurationChangeListener implements DomibusPropertyChangeListener {
    AlertType alertType;

    AlertConfigurationService alertConfigurationService;

    public DefaultAlertConfigurationChangeListener(AlertType alertType, AlertConfigurationService alertConfigurationService) {
        this.alertType = alertType;
        this.alertConfigurationService = alertConfigurationService;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithIgnoreCase(propertyName, alertType.getConfigurationProperty());
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        alertConfigurationService.getConfigurationManager(alertType).reset();
    }
}
