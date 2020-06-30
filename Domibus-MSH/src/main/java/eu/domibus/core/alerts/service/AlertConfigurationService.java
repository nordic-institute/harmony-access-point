package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.configuration.AlertModuleConfiguration;
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
    AlertModuleConfiguration getModuleConfiguration(AlertType alertType);

    /**
     * Return the mail subject base on the alert type.
     *
     * @param alertType the type of the alert.
     * @return the mail subject.
     */
    String getMailSubject(AlertType alertType);

    /**
     * Check if the alert module is enabled.
     *
     * @return whether the module is active or not.
     */
    Boolean isAlertModuleEnabled();

    /**
     * Check if the mail sending for alerts is enabled.
     *
     * @return whether the mail sending is active or not.
     */
    Boolean isSendEmailActive();

    /**
     * Clears/removes all configurations so that new ones will be created when calls to them are made;used when changing general alert enabling
     */
    void resetAll();
}
