package eu.domibus.core.alerts.configuration;

import eu.domibus.core.alerts.model.common.AlertType;

public interface AlertConfigurationReader<AMC extends AlertModuleConfiguration> {
    AlertType getAlertType();

    AMC readConfiguration();
}
