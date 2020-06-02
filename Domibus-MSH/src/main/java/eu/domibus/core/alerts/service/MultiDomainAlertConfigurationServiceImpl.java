package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.UserAuthenticationConfiguration;
import eu.domibus.core.alerts.configuration.manager.*;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.*;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;


/**
 * @author Thomas Dussart, Ion Perpegel
 * @since 4.0
 */
@Service
public class MultiDomainAlertConfigurationServiceImpl implements MultiDomainAlertConfigurationService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(MultiDomainAlertConfigurationServiceImpl.class);

    static final String DOMIBUS_ALERT_SUPER_INSTANCE_NAME_SUBJECT = DOMIBUS_INSTANCE_NAME;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    List<AlertConfigurationManager> alertConfigurationManagers;

    @Autowired
    private MessagingConfigurationManager messagingConfigurationManager;

    @Autowired
    ConsoleAccountDisabledConfigurationManager accountDisabledConfigurationManager;

    @Autowired
    private ConfigurationLoader<AlertModuleConfigurationBase> accountEnabledConfigurationLoader;

    @Autowired
    private PluginAccountDisabledConfigurationManager pluginAccountDisabledConfigurationManager;

    @Autowired
    private ConfigurationLoader<AlertModuleConfigurationBase> pluginAccountEnabledConfigurationLoader;

    @Autowired
    private ConsoleLoginFailConfigurationManager consoleLoginFailConfigurationManager;

    @Autowired
    private PluginLoginFailConfigurationManager pluginLoginFailConfigurationManager;

    @Autowired
    private ImminentExpirationCertificateConfigurationManager imminentExpirationCertificateConfigurationManager;

    @Autowired
    private ExpiredCertificateConfigurationManager expiredCertificateConfigurationManager;

    @Autowired
    private CommonConfigurationManager commonConfigurationManager;

    @Autowired
    private RepetitiveAlertConfigurationHolder passwordExpirationAlertsConfigurationHolder;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Override
    public MessagingModuleConfiguration getMessageCommunicationConfiguration() {
        return messagingConfigurationManager.getConfiguration();
    }

    @Override
    public AccountDisabledModuleConfiguration getAccountDisabledConfiguration() {
        return accountDisabledConfigurationManager.getConfiguration();
    }

    @Override
    public AlertModuleConfigurationBase getAccountEnabledConfiguration() {
        return accountEnabledConfigurationLoader.getConfiguration(new ConsoleAccountEnabledConfigurationReader()::readConfiguration);
    }

    @Override
    public LoginFailureModuleConfiguration getLoginFailureConfiguration() {
        return consoleLoginFailConfigurationManager.getConfiguration();
    }

    @Override
    public ImminentExpirationCertificateModuleConfiguration getImminentExpirationCertificateConfiguration() {
        return imminentExpirationCertificateConfigurationManager.getConfiguration();
    }

    @Override
    public ExpiredCertificateModuleConfiguration getExpiredCertificateConfiguration() {
        return expiredCertificateConfigurationManager.getConfiguration();
    }

    @Override
    public CommonConfiguration getCommonConfiguration() {
        return commonConfigurationManager.getConfiguration();
    }

    @Override
    public void clearCommonConfiguration() {
        commonConfigurationManager.reset();
    }

    @Override
    public void clearLoginFailureConfiguration() {
        consoleLoginFailConfigurationManager.reset();
    }

    @Override
    public void clearPasswordExpirationAlertConfiguration(AlertType alertType) {
        this.passwordExpirationAlertsConfigurationHolder.clearConfiguration(alertType);
    }

    @Override
    public void clearPluginLoginFailureConfiguration() {
        pluginLoginFailConfigurationManager.reset();
    }

    @Override
    public void clearImminentExpirationCertificateConfiguration() {
        imminentExpirationCertificateConfigurationManager.reset();
    }

    @Override
    public void clearExpiredCertificateConfiguration() {
        expiredCertificateConfigurationManager.reset();
    }

    @Override
    public void clearPluginAccountDisabledConfiguration() {
        pluginAccountDisabledConfigurationManager.reset();
    }

    @Override
    public void clearAccountDisabledConfiguration() {
        accountDisabledConfigurationManager.reset();
    }

    @Override
    public void clearMessageCommunicationConfiguration() {
        messagingConfigurationManager.reset();
    }

    @Override
    public AlertLevel getAlertLevel(Alert alert) {
        return getModuleConfiguration(alert.getAlertType()).getAlertLevel(alert);
    }

    @Override
    public String getMailSubject(AlertType alertType) {
        return getModuleConfiguration(alertType).getMailSubject();
    }

    @Override
    public void clearAllConfigurations() {
        this.clearCommonConfiguration();
        this.clearLoginFailureConfiguration();
        this.clearAccountDisabledConfiguration();
        this.clearPluginLoginFailureConfiguration();
        this.clearPluginAccountDisabledConfiguration();
        this.clearMessageCommunicationConfiguration();
        this.clearExpiredCertificateConfiguration();
        this.clearImminentExpirationCertificateConfiguration();
        passwordExpirationAlertsConfigurationHolder.clearConfiguration();
    }

    @Override
    public Boolean isAlertModuleEnabled() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
    }

    @Override
    public String getSendEmailActivePropertyName() {
        return DOMIBUS_ALERT_MAIL_SENDING_ACTIVE;
    }

    @Override
    public String getAlertRetryMaxAttemptPropertyName() {
        return DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS;
    }

    @Override
    public String getAlertRetryTimePropertyName() {
        return DOMIBUS_ALERT_RETRY_TIME;
    }

    @Override
    public String getAlertSuperServerNameSubjectPropertyName() {
        return DOMIBUS_ALERT_SUPER_INSTANCE_NAME_SUBJECT;
    }

    abstract class AccountEnabledConfigurationReader {
        protected abstract AlertType getAlertType();

        protected abstract String getModuleName();

        protected abstract String getAlertActivePropertyName();

        protected abstract String getAlertLevelPropertyName();

        protected abstract String getAlertEmailSubjectPropertyName();

        protected AlertModuleConfigurationBase readConfiguration() {

            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            try {
                final Boolean alertActive = isAlertModuleEnabled();
                final Boolean accountEnabledActive = domibusPropertyProvider.getBooleanProperty(getAlertActivePropertyName());
                if (!alertActive || !accountEnabledActive) {
                    LOG.debug("domain:[{}] [{}] module is inactive for the following reason: global alert module active:[{}], account disabled module active:[{}]"
                            , currentDomain, getModuleName(), alertActive, accountEnabledActive);
                    return new AlertModuleConfigurationBase(getAlertType());
                }

                final AlertLevel level = AlertLevel.valueOf(domibusPropertyProvider.getProperty(getAlertLevelPropertyName()));
                final String mailSubject = domibusPropertyProvider.getProperty(getAlertEmailSubjectPropertyName());

                LOG.info("[{}] module activated for domain:[{}]", getModuleName(), currentDomain);
                return new AlertModuleConfigurationBase(getAlertType(), level, mailSubject);

            } catch (Exception e) {
                LOG.warn("An error occurred while reading [{}] module configuration for domain:[{}], ", getModuleName(), currentDomain, e);
                return new AlertModuleConfigurationBase(getAlertType());
            }
        }
    }

    class ConsoleAccountEnabledConfigurationReader extends AccountEnabledConfigurationReader {

        @Override
        protected AlertType getAlertType() {
            return AlertType.USER_ACCOUNT_ENABLED;
        }

        @Override
        protected String getModuleName() {
            return "Alert account enabled";
        }

        @Override
        protected String getAlertActivePropertyName() {
            return DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_ACTIVE;
        }

        @Override
        protected String getAlertLevelPropertyName() {
            return DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_LEVEL;
        }

        @Override
        protected String getAlertEmailSubjectPropertyName() {
            return DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_SUBJECT;
        }

    }

    class PluginAccountEnabledConfigurationReader extends AccountEnabledConfigurationReader {

        @Override
        protected AlertType getAlertType() {
            return AlertType.PLUGIN_USER_ACCOUNT_ENABLED;
        }

        @Override
        protected String getModuleName() {
            return "Alert plugin account enabled";
        }

        @Override
        protected String getAlertActivePropertyName() {
            return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_ACTIVE;
        }

        @Override
        protected String getAlertLevelPropertyName() {
            return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_LEVEL;
        }

        @Override
        protected String getAlertEmailSubjectPropertyName() {
            return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_SUBJECT;
        }
    }

    @Override
    public RepetitiveAlertModuleConfiguration getRepetitiveAlertConfiguration(AlertType alertType) {
//        ConfigurationLoader<RepetitiveAlertModuleConfiguration> configurationLoader = passwordExpirationAlertsConfigurationHolder.get(alertType);
//        return configurationLoader.getConfiguration(new RepetitiveAlertConfigurationReader(alertType)::readConfiguration);

        ConfigurationLoader<RepetitiveAlertModuleConfiguration> configurationLoader = passwordExpirationAlertsConfigurationHolder.getOrCreate(alertType);
        switch (alertType) {
            case PASSWORD_IMMINENT_EXPIRATION:
                return configurationLoader.getConfiguration(new PasswordImminentExpirationRepetitiveAlertConfigurationReader()::readConfiguration);
            case PASSWORD_EXPIRED:
                return configurationLoader.getConfiguration(new PasswordExpiredRepetitiveAlertConfigurationReader()::readConfiguration);
            case PLUGIN_PASSWORD_IMMINENT_EXPIRATION:
                return configurationLoader.getConfiguration(new PluginPasswordImminentExpirationRepetitiveAlertConfigurationReader()::readConfiguration);
            case PLUGIN_PASSWORD_EXPIRED:
                return configurationLoader.getConfiguration(new PluginPasswordExpiredRepetitiveAlertConfigurationReader()::readConfiguration);
            default:
                LOG.error("Invalid alert type[{}]", alertType);
                throw new IllegalArgumentException("Invalid alert type");
        }
    }

    abstract class RepetitiveAlertConfigurationReader implements UserAuthenticationConfiguration {

        protected abstract AlertType getAlertType();

        public RepetitiveAlertModuleConfiguration readConfiguration() {
            Domain domain = domainContextProvider.getCurrentDomainSafely();
            final String moduleName = getAlertType().getTitle();
            final String property = getAlertType().getConfigurationProperty();
            try {
                if (shouldCheckExtAuthEnabled()) {
                    LOG.debug("domain:[{}] [{}] module is inactive for the following reason: external authentication provider is enabled", domain, moduleName);
                    return new RepetitiveAlertModuleConfiguration(getAlertType());
                }

                final Boolean alertModuleActive = isAlertModuleEnabled();
                final Boolean eventActive = Boolean.valueOf(domibusPropertyProvider.getProperty(property + ".active"));
                if (!alertModuleActive || !eventActive) {
                    LOG.debug("domain:[{}] Alert {} module is inactive for the following reason: global alert module active[{}], event active[{}]",
                            domain, moduleName, alertModuleActive, eventActive);
                    return new RepetitiveAlertModuleConfiguration(getAlertType());
                }

                final Integer delay = Integer.valueOf(domibusPropertyProvider.getProperty(property + ".delay_days"));
                final Integer frequency = Integer.valueOf(domibusPropertyProvider.getProperty(property + ".frequency_days"));
                final AlertLevel alertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(property + ".level"));
                final String mailSubject = domibusPropertyProvider.getProperty(property + ".mail.subject");

                LOG.info("Alert {} module activated for domain:[{}]", moduleName, domain);
                return new RepetitiveAlertModuleConfiguration(getAlertType(), delay, frequency, alertLevel, mailSubject);
            } catch (Exception e) {
                LOG.warn("An error occurred while reading {} alert module configuration for domain:[{}], ", moduleName, domain, e);
                return new RepetitiveAlertModuleConfiguration(getAlertType());
            }
        }
    }

    class PasswordImminentExpirationRepetitiveAlertConfigurationReader extends RepetitiveAlertConfigurationReader {
        @Override
        protected AlertType getAlertType() {
            return AlertType.PASSWORD_IMMINENT_EXPIRATION;
        }

        @Override
        public boolean shouldCheckExtAuthEnabled() {
            return domibusConfigurationService.isExtAuthProviderEnabled();
        }
    }

    class PasswordExpiredRepetitiveAlertConfigurationReader extends RepetitiveAlertConfigurationReader {
        @Override
        protected AlertType getAlertType() {
            return AlertType.PASSWORD_EXPIRED;
        }

        @Override
        public boolean shouldCheckExtAuthEnabled() {
            return domibusConfigurationService.isExtAuthProviderEnabled();
        }
    }

    class PluginPasswordImminentExpirationRepetitiveAlertConfigurationReader extends RepetitiveAlertConfigurationReader {
        @Override
        protected AlertType getAlertType() {
            return AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION;
        }

        @Override
        public boolean shouldCheckExtAuthEnabled() {
            return false;
        }
    }

    class PluginPasswordExpiredRepetitiveAlertConfigurationReader extends RepetitiveAlertConfigurationReader {
        @Override
        protected AlertType getAlertType() {
            return AlertType.PLUGIN_PASSWORD_EXPIRED;
        }

        @Override
        public boolean shouldCheckExtAuthEnabled() {
            return false;
        }
    }

    @Override
    public LoginFailureModuleConfiguration getPluginLoginFailureConfiguration() {
        return pluginLoginFailConfigurationManager.getConfiguration();
    }

    @Override
    public AccountDisabledModuleConfiguration getPluginAccountDisabledConfiguration() {
        return pluginAccountDisabledConfigurationManager.getConfiguration();
    }

    @Override
    public AlertModuleConfigurationBase getPluginAccountEnabledConfiguration() {
        return pluginAccountEnabledConfigurationLoader.getConfiguration(new PluginAccountEnabledConfigurationReader()::readConfiguration);
    }

    private AlertModuleConfiguration getModuleConfiguration(AlertType alertType) {
        Optional<AlertConfigurationManager> res = alertConfigurationManagers.stream().filter(el -> el.getAlertType() == alertType).findFirst();
        if (!res.isPresent()) {
            LOG.error("Invalid alert type[{}]", alertType);
            throw new IllegalArgumentException("Invalid alert type");
        }
        return res.get().getConfiguration();
    }
}
