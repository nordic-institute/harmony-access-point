package eu.domibus.core.alerts.configuration.manager;

import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.configuration.model.AlertModuleConfiguration;

public interface AlertConfigurationManager {

    AlertType getAlertType();

    AlertModuleConfiguration getConfiguration();

    void reset();

}
