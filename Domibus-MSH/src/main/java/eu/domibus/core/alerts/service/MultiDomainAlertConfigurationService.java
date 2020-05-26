package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Retrieve the configuration for the different type of alert submodules.
 */
public interface MultiDomainAlertConfigurationService {

    /**
     * @return message communication module configuration
     */
    MessagingModuleConfiguration getMessageCommunicationConfiguration();

    /**
     * Clears/removes the message communication configuration so that a new one will be created when calls to it are made
     */
    void clearMessageCommunicationConfiguration();

    /**
     * @return account disabled module configuration
     */
    AccountDisabledModuleConfiguration getAccountDisabledConfiguration();

    /**
     * @return account enabled module configuration
     */
    AccountEnabledModuleConfiguration getAccountEnabledConfiguration();

    /**
     * Clears/removes the account disabled configuration so that a new one will be created when calls to it are made
     */
    void clearAccountDisabledConfiguration();

    /**
     * @return login failure module configuration
     */
    LoginFailureModuleConfiguration getLoginFailureConfiguration();

    /**
     * Clear login failure module configuration
     */
    void clearLoginFailureConfiguration();

    /**
     * @return certificate imminent expiration module configuration
     */
    ImminentExpirationCertificateModuleConfiguration getImminentExpirationCertificateConfiguration();

    /**
     * @return certificate expired module configuration
     */
    ExpiredCertificateModuleConfiguration getExpiredCertificateConfiguration();

    /**
     * @return alert common configuration
     */
    CommonConfiguration getCommonConfiguration();

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
     * @return alert events module configuration
     */
    RepetitiveAlertModuleConfiguration getRepetitiveAlertConfiguration(AlertType alertType);

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
     * @return account disabled module configuration for plugin users
     */
    AccountDisabledModuleConfiguration getPluginAccountDisabledConfiguration();

    /**
     * @return account enabled module configuration for plugin users
     */
    AccountEnabledModuleConfiguration getPluginAccountEnabledConfiguration();

    /**
     * Clears/removes the common configuration configuration so that a new one will be created when calls to it are made
     */
    void clearCommonConfiguration();

    /**
     * Clears/removes the password expiration alert configuration so that a new one will be created when calls to it are made
     */
    void clearPasswordExpirationAlertConfiguration(AlertType alertType);

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

    /**
     * Clears/removes all configurations so that new ones will be created when calls to them are made;used when changing general alert enabling
     */
    void clearAllConfigurations();

    /**
     * Clears/removes the plugin account disabled configuration so that a new one will be created when calls to it are made
     */
    void clearPluginAccountDisabledConfiguration();
}
