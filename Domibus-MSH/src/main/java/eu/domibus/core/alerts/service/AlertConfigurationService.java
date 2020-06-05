package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.configuration.account.disabled.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.configuration.certificate.ExpiredCertificateModuleConfiguration;
import eu.domibus.core.alerts.configuration.certificate.ImminentExpirationCertificateModuleConfiguration;
import eu.domibus.core.alerts.configuration.common.CommonConfiguration;
import eu.domibus.core.alerts.configuration.login.LoginFailureModuleConfiguration;
import eu.domibus.core.alerts.configuration.messaging.MessagingModuleConfiguration;
import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Retrieve the configuration for the different type of alert submodules.
 */
public interface AlertConfigurationService {

    void clearPluginAccountEnabledConfiguration();

    /**
     * @return login failure module configuration
     */
    LoginFailureModuleConfiguration getConsoleLoginFailureConfiguration();

    /**
     * Clear login failure module configuration
     */
    void clearConsoleLoginFailureConfiguration();

    void clearConsolePasswordImminentExpirationAlertConfigurationManager();

    /**
     * @return certificate imminent expiration module configuration
     */
    ImminentExpirationCertificateModuleConfiguration getImminentExpirationCertificateConfiguration();

    /**
     * @return certificate expired module configuration
     */
    ExpiredCertificateModuleConfiguration getExpiredCertificateConfiguration();

    /**
     * Return alert level based on alert(type)
     *
     * @param alert the alert.
     * @return the level.
     */
    AlertLevel getAlertLevel(Alert alert);

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
     * With the introduction of multitenancy, a super user has been created.
     * It has its own property definition for some of the domibus properties.
     * The following methods are helper methods to retrieve super or domain alert property name
     * depending on the context.
     */

    /**
     * @return name for the alert email active property.
     */
    String getSendEmailActivePropertyName();

    /**
     * @return name of the alert retry max attempts property.
     */
    String getAlertRetryMaxAttemptPropertyName();

    /**
     * @return name of the alert time between retry property.
     */
    String getAlertRetryTimePropertyName();

    /**
     * @return name of the property for adding Domibus instance/server name to email subject
     */
    String getAlertSuperServerNameSubjectPropertyName();

    /**
     * @return login failure module configuration for plugin users
     */
    LoginFailureModuleConfiguration getPluginLoginFailureConfiguration();

    /**
     * @return account enabled module configuration for plugin users
     */
    AlertModuleConfigurationBase getPluginAccountEnabledConfiguration();

    PasswordExpirationAlertModuleConfiguration getConsolePasswordExpiredAlertConfigurationManager();

    void clearConsolePasswordExpiredAlertConfigurationManager();

    PasswordExpirationAlertModuleConfiguration getConsolePasswordImminentExpirationAlertConfigurationManager();

    PasswordExpirationAlertModuleConfiguration getPluginPasswordExpiredAlertConfigurationManager();

    void clearPluginPasswordExpiredAlertConfigurationManager();

    PasswordExpirationAlertModuleConfiguration getPluginPasswordImminentExpirationAlertConfigurationManager();

    /**
     * Clears/removes the plugin login failure configuration so that a new one will be created when calls to it are made
     */
    void clearPluginLoginFailureConfiguration();

    /**
     * Clears/removes the imminent certification expiration configuration so that a new one will be created when calls to it are made
     */
    void clearImminentExpirationCertificateConfiguration();

    /**
     * Clears/removes the expired certificate configuration so that a new one will be created when calls to it are made
     */
    void clearExpiredCertificateConfiguration();

    void clearPluginPasswordImminentExpirationAlertConfigurationManager();

    /**
     * Clears/removes all configurations so that new ones will be created when calls to them are made;used when changing general alert enabling
     */
    void clearAllConfigurations();
}
