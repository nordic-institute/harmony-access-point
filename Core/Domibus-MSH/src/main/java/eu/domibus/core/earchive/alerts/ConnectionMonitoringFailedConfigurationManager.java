package eu.domibus.core.earchive.alerts;

import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.connectionMonitpring.ConnectionMonitoringModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_FREQUENCY_DAYS;

/**
 * Default alert config manager generated automatically for an alert type (if not overridden)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ConnectionMonitoringFailedConfigurationManager
        extends FrequencyAlertConfigurationManager
        implements AlertConfigurationManager {

    public ConnectionMonitoringFailedConfigurationManager(AlertType alertType, String domibusPropertiesPrefix) {
        super(alertType, domibusPropertiesPrefix);
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
