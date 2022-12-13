package eu.domibus.core.alerts.configuration.partitions;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.generic.FrequencyAlertConfiguration;
import eu.domibus.core.alerts.configuration.generic.FrequencyAlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Custom alert config manager for partition alerts that derives from frequency manager and rewrites the active and level
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PartitionConfigurationManager extends FrequencyAlertConfigurationManager
        implements AlertConfigurationManager {

    public PartitionConfigurationManager(AlertType alertType) {
        super(alertType);
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
