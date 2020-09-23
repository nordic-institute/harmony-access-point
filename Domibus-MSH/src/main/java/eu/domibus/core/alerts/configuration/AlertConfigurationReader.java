package eu.domibus.core.alerts.configuration;

import eu.domibus.core.alerts.model.common.AlertType;

/**
 * Interface extended by alert configuration readers
 *
 * @author Ion Perpegel
 * @since 4.2
 */
public interface AlertConfigurationReader<AMC extends AlertModuleConfiguration> {
    AlertType getAlertType();

    AMC readConfiguration();
}
