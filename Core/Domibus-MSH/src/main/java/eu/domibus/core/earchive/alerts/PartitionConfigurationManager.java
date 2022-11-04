package eu.domibus.core.earchive.alerts;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Default alert config manager generated automatically for an alert type (if not overridden)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PartitionConfigurationManager
        extends FrequencyAlertConfigurationManager
        implements AlertConfigurationManager {

    public PartitionConfigurationManager(AlertType alertType, String domibusPropertiesPrefix) {
        super(alertType, domibusPropertiesPrefix);
    }

    @Override
    protected FrequencyAlertConfiguration createAlertConfiguration(AlertType alertType) {
        return new FrequencyAlertConfiguration(alertType);
    }

    @Override
    protected Boolean isAlertActive() {
        return true;
    }

    @Override
    protected AlertLevel getAlertLevel() {
        return AlertLevel.HIGH;
    }

    @Override
    protected String getMailSubject() {
        return null;
    }
}
