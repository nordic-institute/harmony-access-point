package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import eu.domibus.core.user.UserDaoBase;
import eu.domibus.core.user.UserEntityBase;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.configuration.account.disabled.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.configuration.login.LoginFailureModuleConfiguration;
import eu.domibus.core.user.plugin.AuthenticationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_POLICY_PLUGIN_DEFAULT_PASSWORD_EXPIRATION;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_POLICY_PLUGIN_EXPIRATION;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@Service
public class PluginUserAlertsServiceImpl extends UserAlertsServiceImpl {

    protected static final String MAXIMUM_PASSWORD_AGE = DOMIBUS_PASSWORD_POLICY_PLUGIN_EXPIRATION; //NOSONAR
    protected static final String MAXIMUM_DEFAULT_PASSWORD_AGE = DOMIBUS_PASSWORD_POLICY_PLUGIN_DEFAULT_PASSWORD_EXPIRATION; //NOSONAR

    @Autowired
    protected AuthenticationDAO userDao;

    @Autowired
    private AlertConfigurationService alertsConfiguration;

    @Override
    protected String getMaximumDefaultPasswordAgeProperty() {
        return MAXIMUM_DEFAULT_PASSWORD_AGE;
    }

    @Override
    protected String getMaximumPasswordAgeProperty() {
        return MAXIMUM_PASSWORD_AGE;
    }

    @Override
    protected AlertType getAlertTypeForPasswordImminentExpiration() {
        return AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION;
    }

    @Override
    protected AlertType getAlertTypeForPasswordExpired() {
        return AlertType.PLUGIN_PASSWORD_EXPIRED;
    }

    @Override
    protected EventType getEventTypeForPasswordImminentExpiration() {
        return EventType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION;
    }

    @Override
    protected EventType getEventTypeForPasswordExpired() {
        return EventType.PLUGIN_PASSWORD_EXPIRED;
    }

    @Override
    protected UserDaoBase getUserDao() {
        return userDao;
    }

    @Override
    protected UserEntityBase.Type getUserType() {
        return UserEntityBase.Type.PLUGIN;
    }

    @Override
    protected AccountDisabledModuleConfiguration getAccountDisabledConfiguration() {
        return alertsConfiguration.getPluginAccountDisabledConfiguration();
    }

    @Override
    protected AlertModuleConfigurationBase getAccountEnabledConfiguration() {
        return alertsConfiguration.getPluginAccountEnabledConfiguration();
    }

    @Override
    protected LoginFailureModuleConfiguration getLoginFailureConfiguration() {
        return alertsConfiguration.getPluginLoginFailureConfiguration();
    }

    @Override
    protected PasswordExpirationAlertModuleConfiguration getExpiredAlertConfiguration() {
        return alertsConfiguration.getPluginPasswordExpiredAlertConfigurationManager();
    }

    @Override
    protected PasswordExpirationAlertModuleConfiguration getImminentExpirationAlertConfiguration() {
        return alertsConfiguration.getPluginPasswordImminentExpirationAlertConfigurationManager();
    }

}
