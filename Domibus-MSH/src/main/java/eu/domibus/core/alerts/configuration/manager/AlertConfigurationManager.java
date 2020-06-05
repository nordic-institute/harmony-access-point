package eu.domibus.core.alerts.configuration.manager;

import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.configuration.model.AlertModuleConfiguration;

/**
 * Interface implemented by alert configuration managers/readers/loaders classes
 *
 * @author Ion Perpegel
 * @since 4.2
 */
public interface AlertConfigurationManager {

    AlertType getAlertType();

    AlertModuleConfiguration getConfiguration();

    void reset();

}
