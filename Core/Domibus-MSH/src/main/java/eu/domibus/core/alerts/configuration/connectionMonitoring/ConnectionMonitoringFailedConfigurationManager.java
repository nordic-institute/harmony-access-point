package eu.domibus.core.alerts.configuration.connectionMonitoring;

import eu.domibus.core.alerts.configuration.common.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.generic.FrequencyAlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Default alert config manager generated automatically for an alert type (if not overridden)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ConnectionMonitoringFailedConfigurationManager extends FrequencyAlertConfigurationManager
        implements AlertConfigurationManager {

    public ConnectionMonitoringFailedConfigurationManager(AlertType alertType) {
        super(alertType);
    }

    @Override
    protected Boolean isAlertActive() {
        return !CollectionUtils.isEmpty(getEnabledParties());
    }

    @Override
    public ConnectionMonitoringModuleConfiguration readConfiguration() {
        ConnectionMonitoringModuleConfiguration conf = (ConnectionMonitoringModuleConfiguration) super.readConfiguration();
        if (!conf.isActive()) {
            return conf;
        }

        conf.setEnabledParties(getEnabledParties());

        return conf;
    }

    @Override
    protected ConnectionMonitoringModuleConfiguration createAlertConfiguration(AlertType alertType) {
        return new ConnectionMonitoringModuleConfiguration();
    }

    private List<String> getEnabledParties() {
        return domibusPropertyProvider.getCommaSeparatedPropertyValues(domibusPropertiesPrefix + ".parties");
    }
}
