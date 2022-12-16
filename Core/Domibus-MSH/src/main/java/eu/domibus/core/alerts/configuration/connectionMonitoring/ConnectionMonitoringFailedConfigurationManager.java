package eu.domibus.core.alerts.configuration.connectionMonitoring;

import eu.domibus.core.alerts.configuration.common.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.generic.FrequencyAlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.monitoring.ConnectionMonitoringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Custom alert config manager for connection monitoring alerts
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ConnectionMonitoringFailedConfigurationManager extends FrequencyAlertConfigurationManager
        implements AlertConfigurationManager {

    @Autowired
    ConnectionMonitoringHelper connectionMonitoringHelper;

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
    protected ConnectionMonitoringModuleConfiguration createNewInstance(AlertType alertType) {
        return new ConnectionMonitoringModuleConfiguration();
    }

    private List<String> getEnabledParties() {
        connectionMonitoringHelper.ensureCorrectValueForProperty(domibusPropertiesPrefix + ".parties");
        return domibusPropertyProvider.getCommaSeparatedPropertyValues(domibusPropertiesPrefix + ".parties");
    }
}
