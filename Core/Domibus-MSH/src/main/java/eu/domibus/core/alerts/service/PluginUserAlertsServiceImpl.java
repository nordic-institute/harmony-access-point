package eu.domibus.core.alerts.service;

import eu.domibus.api.user.UserEntityBase;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.alerts.configuration.common.AlertModuleConfiguration;
import eu.domibus.core.alerts.configuration.account.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.configuration.generic.RepetitiveAlertConfiguration;
import eu.domibus.core.user.UserDaoBase;
import eu.domibus.core.user.plugin.AuthenticationDAO;
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

    protected final AuthenticationDAO userDao;

    private final AlertConfigurationService alertConfigurationService;

    public PluginUserAlertsServiceImpl(AuthenticationDAO userDao, AlertConfigurationService alertConfigurationService) {
        this.userDao = userDao;
        this.alertConfigurationService = alertConfigurationService;
    }

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
        return (AccountDisabledModuleConfiguration) alertConfigurationService.getConfiguration(AlertType.PLUGIN_USER_ACCOUNT_DISABLED);
    }

    @Override
    protected AlertModuleConfiguration getAccountEnabledConfiguration() {
        return alertConfigurationService.getConfiguration(AlertType.USER_ACCOUNT_ENABLED);
    }

    @Override
    protected AlertModuleConfiguration getLoginFailureConfiguration() {
        return alertConfigurationService.getConfiguration(AlertType.PLUGIN_USER_LOGIN_FAILURE);
    }

    @Override
    protected RepetitiveAlertConfiguration getExpiredAlertConfiguration() {
        return (RepetitiveAlertConfiguration) alertConfigurationService.getConfiguration(AlertType.PLUGIN_PASSWORD_EXPIRED);
    }

    @Override
    protected RepetitiveAlertConfiguration getImminentExpirationAlertConfiguration() {
        return (RepetitiveAlertConfiguration) alertConfigurationService.getConfiguration(AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION);
    }

}
