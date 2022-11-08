package eu.domibus.core.alerts.service;

import eu.domibus.api.user.UserEntityBase;
import eu.domibus.core.alerts.configuration.AlertModuleConfiguration;
import eu.domibus.core.alerts.configuration.account.disabled.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.configuration.RepetitiveAlertConfiguration;
import eu.domibus.core.user.UserDaoBase;
import eu.domibus.core.user.ui.UserDao;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_POLICY_DEFAULT_PASSWORD_EXPIRATION;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_POLICY_EXPIRATION;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@Service
public class ConsoleUserAlertsServiceImpl extends UserAlertsServiceImpl {

    public static final String MAXIMUM_PASSWORD_AGE = DOMIBUS_PASSWORD_POLICY_EXPIRATION; //NOSONAR
    public static final String MAXIMUM_DEFAULT_PASSWORD_AGE = DOMIBUS_PASSWORD_POLICY_DEFAULT_PASSWORD_EXPIRATION; //NOSONAR

    protected final UserDao userDao;

    protected final AlertConfigurationService alertConfigurationService;

    public ConsoleUserAlertsServiceImpl(UserDao userDao, AlertConfigurationService alertConfigurationService) {
        this.userDao = userDao;
        this.alertConfigurationService = alertConfigurationService;
    }

//    @Autowired
//    private ConsoleLoginFailConfigurationManager consoleLoginFailConfigurationManager;

//    @Autowired
//    private ConsoleAccountEnabledConfigurationManager consoleAccountEnabledConfigurationManager;

//    @Autowired
//    ConsoleAccountDisabledConfigurationManager consoleAccountDisabledConfigurationManager;

//    @Autowired
//    private ConsolePasswordExpiredAlertConfigurationManager consolePasswordExpiredAlertConfigurationManager;
//
//    @Autowired
//    private ConsolePasswordImminentExpirationAlertConfigurationManager consolePasswordImminentExpirationAlertConfigurationManager;

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
        return AlertType.PASSWORD_IMMINENT_EXPIRATION;
    }

    @Override
    protected AlertType getAlertTypeForPasswordExpired() {
        return AlertType.PASSWORD_EXPIRED;
    }

    @Override
    protected EventType getEventTypeForPasswordImminentExpiration() {
        return EventType.PASSWORD_IMMINENT_EXPIRATION;
    }

    @Override
    protected EventType getEventTypeForPasswordExpired() {
        return EventType.PASSWORD_EXPIRED;
    }

    @Override
    protected UserDaoBase getUserDao() {
        return userDao;
    }

    @Override
    protected UserEntityBase.Type getUserType() {
        return UserEntityBase.Type.CONSOLE;
    }

    @Override
    protected AccountDisabledModuleConfiguration getAccountDisabledConfiguration() {
        return (AccountDisabledModuleConfiguration) alertConfigurationService.getConfiguration(AlertType.USER_ACCOUNT_DISABLED);
//        return consoleAccountDisabledConfigurationManager.getConfiguration();
    }

    @Override
//    protected AlertModuleConfigurationBase getAccountEnabledConfiguration() {
    protected AlertModuleConfiguration getAccountEnabledConfiguration() {
        return alertConfigurationService.getConfiguration(AlertType.USER_ACCOUNT_ENABLED);
//        return consoleAccountEnabledConfigurationManager.getConfiguration();
    }

    @Override
//    protected LoginFailureModuleConfiguration getLoginFailureConfiguration() {
    protected AlertModuleConfiguration getLoginFailureConfiguration() {
        return alertConfigurationService.getConfiguration(AlertType.USER_LOGIN_FAILURE);
//        return consoleLoginFailConfigurationManager.getConfiguration();
    }

    @Override
    protected RepetitiveAlertConfiguration getExpiredAlertConfiguration() {
        return (RepetitiveAlertConfiguration) alertConfigurationService.getConfiguration(AlertType.PASSWORD_EXPIRED);
//        return consolePasswordExpiredAlertConfigurationManager.getConfiguration();
    }

    @Override
    protected RepetitiveAlertConfiguration getImminentExpirationAlertConfiguration() {
        return (RepetitiveAlertConfiguration) alertConfigurationService.getConfiguration(AlertType.PASSWORD_IMMINENT_EXPIRATION);
//        return consolePasswordImminentExpirationAlertConfigurationManager.getConfiguration();
    }

}
