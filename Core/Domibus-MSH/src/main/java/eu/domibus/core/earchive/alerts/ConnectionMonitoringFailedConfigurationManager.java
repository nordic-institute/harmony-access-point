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
 * Default alert config manager generated automatically for an alert type ( if not overridden)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ConnectionMonitoringFailedConfigurationManager
        extends BaseConfigurationManager<ConnectionMonitoringModuleConfiguration>
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConnectionMonitoringFailedConfigurationManager.class);

    public ConnectionMonitoringFailedConfigurationManager(AlertType alertType, String domibusPropertiesPrefix) {
        super(alertType, domibusPropertiesPrefix);
    }

    @Override
    protected Boolean isAlertActive() {
        List<String> enabledParties = getEnabledParties();
        return !CollectionUtils.isEmpty(enabledParties);
    }

    @Override
    public ConnectionMonitoringModuleConfiguration readConfiguration() {
        ConnectionMonitoringModuleConfiguration conf = super.readConfiguration();
        if (!conf.isActive()) {
            return conf;
        }

        conf.setEnabledParties(getEnabledParties());

        final int frequency = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_FREQUENCY_DAYS);
        conf.setFrequency(frequency);

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
