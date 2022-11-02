package eu.domibus.core.earchive.alerts;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.alerts.model.common.AlertType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * General bean that handles the change of alert properties
 */
@Component
public class DefaultAlertConfigurationChangeListener implements DomibusPropertyChangeListener {
    AlertType alertType;

    public DefaultAlertConfigurationChangeListener(AlertType alertType) {
        this.alertType = alertType;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithIgnoreCase(propertyName, alertType.getConfigurationProperty());
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        alertType.getConfigurationManager().reset();
    }
}
