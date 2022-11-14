package eu.domibus.core.alerts.configuration.common;

import eu.domibus.core.alerts.configuration.common.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.common.AlertModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Retrieve the configuration for the different type of alert submodules.
 */
public interface AlertConfigurationService {

    /**
     * Return the alert configuration of a specific alert type
     *
     * @param alertType the alert type.
     * @return the configuration object.
     */
    AlertModuleConfiguration getConfiguration(AlertType alertType);

    AlertConfigurationManager getConfigurationManager(AlertType alertType);

    /**
     * Return the mail subject base on the alert type.
     *
     * @param alertType the type of the alert.
     * @return the mail subject.
     */
    String getMailSubject(AlertType alertType);

    /**
     * Clears/removes all configurations so that new ones will be created when calls to them are made;used when changing general alert enabling
     */
    void resetAll();
}
