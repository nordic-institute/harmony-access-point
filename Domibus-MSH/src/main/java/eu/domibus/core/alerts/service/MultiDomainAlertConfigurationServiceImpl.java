package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.*;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;


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
    private ConfigurationLoader<MessagingModuleConfiguration> messagingConfigurationLoader;

    @Autowired
    private ConfigurationLoader<AccountDisabledModuleConfiguration> accountDisabledConfigurationLoader;

    @Autowired
    private ConfigurationLoader<AccountEnabledModuleConfiguration> accountEnabledConfigurationLoader;

    @Autowired
    private ConfigurationLoader<AccountDisabledModuleConfiguration> pluginAccountDisabledConfigurationLoader;

    @Autowired
    private ConfigurationLoader<AccountEnabledModuleConfiguration> pluginAccountEnabledConfigurationLoader;

    @Autowired
    private ConfigurationLoader<LoginFailureModuleConfiguration> loginFailureConfigurationLoader;

    @Autowired
    private ConfigurationLoader<LoginFailureModuleConfiguration> pluginLoginFailureConfigurationLoader;

    @Autowired
    private ConfigurationLoader<ImminentExpirationCertificateModuleConfiguration> imminentExpirationCertificateConfigurationLoader;

    @Autowired
    private ConfigurationLoader<ExpiredCertificateModuleConfiguration> expiredCertificateConfigurationLoader;

    @Autowired
    private ConfigurationLoader<CommonConfiguration> commonConfigurationConfigurationLoader;

    @Autowired
    private RepetitiveAlertConfigurationHolder passwordExpirationAlertsConfigurationHolder;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public MessagingModuleConfiguration getMessageCommunicationConfiguration() {
        return messagingConfigurationLoader.getConfiguration(this::readMessageConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountDisabledModuleConfiguration getAccountDisabledConfiguration() {
        return accountDisabledConfigurationLoader.getConfiguration(new ConsoleAccountDisabledConfigurationReader()::readConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountEnabledModuleConfiguration getAccountEnabledConfiguration() {
        return accountEnabledConfigurationLoader.getConfiguration(new ConsoleAccountEnabledConfigurationReader()::readConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginFailureModuleConfiguration getLoginFailureConfiguration() {
        return loginFailureConfigurationLoader.getConfiguration(new ConsoleLoginFailConfigurationReader()::readConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImminentExpirationCertificateModuleConfiguration getImminentExpirationCertificateConfiguration() {
        return imminentExpirationCertificateConfigurationLoader.getConfiguration(this::readImminentExpirationCertificateConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExpiredCertificateModuleConfiguration getExpiredCertificateConfiguration() {
        return expiredCertificateConfigurationLoader.getConfiguration(this::readExpiredCertificateConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommonConfiguration getCommonConfiguration() {
        return commonConfigurationConfigurationLoader.getConfiguration(this::readCommonConfiguration);
    }

    @Override
    public void clearCommonConfiguration() {
        commonConfigurationConfigurationLoader.resetConfiguration();
    }

    @Override
    public void clearLoginFailureConfiguration() {
        loginFailureConfigurationLoader.resetConfiguration();
    }

    @Override
    public void clearPasswordExpirationAlertConfiguration(AlertType alertType) {
        this.passwordExpirationAlertsConfigurationHolder.clearConfiguration(alertType);
    }

    @Override
    public void clearPluginLoginFailureConfiguration() {
        this.pluginLoginFailureConfigurationLoader.resetConfiguration();
    }

    @Override
    public void clearImminentExpirationCertificateConfiguration() {
        this.imminentExpirationCertificateConfigurationLoader.resetConfiguration();
    }

    @Override
    public void clearExpiredCertificateConfiguration() {
        this.expiredCertificateConfigurationLoader.resetConfiguration();
    }

    @Override
    public void clearPluginAccountDisabledConfiguration() {
        this.pluginAccountDisabledConfigurationLoader.resetConfiguration();
    }

    @Override
    public void clearAccountDisabledConfiguration() {
        accountDisabledConfigurationLoader.resetConfiguration();
    }

    @Override
    public void clearMessageCommunicationConfiguration() {
        messagingConfigurationLoader.resetConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlertLevel getAlertLevel(Alert alert) {
        return getModuleConfiguration(alert.getAlertType()).getAlertLevel(alert);
    }

    /**
     * {@inheritDoc}
     */
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

    // TODO: refactor to avoid these repetitions cause we can easily miss to add here the newly added alert types
    private AlertModuleConfiguration getModuleConfiguration(AlertType alertType) {
        switch (alertType) {
            case MSG_STATUS_CHANGED:
                return getMessageCommunicationConfiguration();
            case USER_ACCOUNT_DISABLED:
                return getAccountDisabledConfiguration();
            case USER_ACCOUNT_ENABLED:
                return getAccountEnabledConfiguration();
            case PLUGIN_USER_ACCOUNT_DISABLED:
                return getPluginAccountDisabledConfiguration();
            case PLUGIN_USER_ACCOUNT_ENABLED:
                return getPluginAccountEnabledConfiguration();
            case USER_LOGIN_FAILURE:
                return getLoginFailureConfiguration();
            case PLUGIN_USER_LOGIN_FAILURE:
                return getPluginLoginFailureConfiguration();
            case CERT_IMMINENT_EXPIRATION:
                return getImminentExpirationCertificateConfiguration();
            case CERT_EXPIRED:
                return getExpiredCertificateConfiguration();
            case PASSWORD_IMMINENT_EXPIRATION:
            case PASSWORD_EXPIRED:
            case PLUGIN_PASSWORD_IMMINENT_EXPIRATION:
            case PLUGIN_PASSWORD_EXPIRED:
                return getRepetitiveAlertConfiguration(alertType);
            default:
                LOG.error("Invalid alert type[{}]", alertType);
                throw new IllegalArgumentException("Invalid alert type");
        }
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

    protected CommonConfiguration readCommonConfiguration() {
        final boolean emailActive = domibusPropertyProvider.getBooleanProperty(getSendEmailActivePropertyName());
        final Integer alertLifeTimeInDays = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME);

        if (!emailActive) {
            return new CommonConfiguration(alertLifeTimeInDays);
        }

        return readDomainEmailConfiguration(alertLifeTimeInDays);
    }

    private CommonConfiguration readDomainEmailConfiguration(Integer alertLifeTimeInDays) {
        final String alertEmailSender = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_EMAIL);
        final String alertEmailReceiver = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RECEIVER_EMAIL);

        boolean misConfigured = false;
        if (StringUtils.isEmpty(alertEmailReceiver) || StringUtils.isEmpty(alertEmailSender)) {
            misConfigured = true;
        } else {
            List<String> emailsToValidate = new ArrayList<>(Arrays.asList(alertEmailSender));
            emailsToValidate.addAll(Arrays.asList(alertEmailReceiver.split(";")));
            for (String email : emailsToValidate) {
                misConfigured = !isValidEmail(email);
                if (misConfigured) {
                    break;
                }
            }
        }
        if (misConfigured) {
            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            LOG.error("Alert module can not send email, mail sender property name:[{}]/value[{}] and receiver property name:[{}]/value[{}] are mandatory in the domain [{}].",
                    DOMIBUS_ALERT_SENDER_EMAIL, alertEmailSender, DOMIBUS_ALERT_RECEIVER_EMAIL, alertEmailReceiver, currentDomain);
            throw new IllegalArgumentException("Invalid email address configured for the alert module.");
        }
        return new CommonConfiguration(alertLifeTimeInDays, alertEmailSender, alertEmailReceiver);
    }

    private boolean isValidEmail(String email) {
        try {
            InternetAddress address = new InternetAddress(email);
            address.validate();
            return true;
        } catch (AddressException ae) {
            LOG.trace("Email address [{}] is not valid:", email, ae);
            return false;
        }
    }

    protected MessagingModuleConfiguration readMessageConfiguration() {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean messageAlertActive = domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            if (!alertActive || !messageAlertActive) {
                LOG.debug("domain:[{}] Alert message status change module is inactive for the following reason:global alert module active[{}], message status change module active[{}]",
                        currentDomain, alertActive, messageAlertActive);
                return new MessagingModuleConfiguration();
            }
            final String messageCommunicationStates = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            final String messageCommunicationLevels = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            final String mailSubject = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT);

            if (StringUtils.isEmpty(messageCommunicationStates) || StringUtils.isEmpty(messageCommunicationLevels)) {
                LOG.warn("Message status change alert module misconfiguration -> states[{}], levels[{}]", messageCommunicationStates, messageCommunicationLevels);
                return new MessagingModuleConfiguration();
            }
            final String[] states = messageCommunicationStates.split(",");
            final String[] levels = messageCommunicationLevels.split(",");
            final boolean eachStatusHasALevel = (states.length == levels.length);
            LOG.debug("Each message status has his own level[{}]", eachStatusHasALevel);

            MessagingModuleConfiguration messagingConfiguration = new MessagingModuleConfiguration(mailSubject);
            IntStream.
                    range(0, states.length).
                    mapToObj(i -> new AbstractMap.SimpleImmutableEntry<>(MessageStatus.valueOf(states[i]), AlertLevel.valueOf(levels[eachStatusHasALevel ? i : 0]))).
                    forEach(entry -> messagingConfiguration.addStatusLevelAssociation(entry.getKey(), entry.getValue())); //NOSONAR
            LOG.info("Alert message status change module activated for domain:[{}]", currentDomain);
            return messagingConfiguration;
        } catch (Exception ex) {
            LOG.warn("Error while configuring message communication alerts for domain:[{}], message alert module will be discarded.", currentDomain, ex);
            return new MessagingModuleConfiguration();
        }

    }

    /**
     * Each configuration reader which handles user alerts have to implement this
     */
    interface UserAuthenticationConfiguration {

        /**
         * true if we should check about external authentication enabled
         * @return boolean
         */
        boolean shouldCheckExtAuthEnabled();
    }

    abstract class AccountDisabledConfigurationReader implements UserAuthenticationConfiguration {
        protected abstract AlertType getAlertType();

        protected abstract String getModuleName();

        protected abstract String getAlertActivePropertyName();

        protected abstract String getAlertLevelPropertyName();

        protected abstract String getAlertMomentPropertyName();

        protected abstract String getAlertEmailSubjectPropertyName();

        protected AccountDisabledModuleConfiguration readConfiguration() {
            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            try {
                if (shouldCheckExtAuthEnabled()) {
                    //ECAS or other provider
                    LOG.debug("domain:[{}] [{}] module is inactive for the following reason: external authentication provider is enabled", currentDomain, getModuleName());
                    return new AccountDisabledModuleConfiguration(getAlertType());
                }

                final Boolean alertActive = isAlertModuleEnabled();
                final Boolean accountDisabledActive = domibusPropertyProvider.getBooleanProperty(getAlertActivePropertyName());
                if (!alertActive || !accountDisabledActive) {
                    LOG.debug("domain:[{}] [{}] module is inactive for the following reason: global alert module active:[{}], account disabled module active:[{}]"
                            , currentDomain, getModuleName(), alertActive, accountDisabledActive);
                    return new AccountDisabledModuleConfiguration(getAlertType());
                }

                final AlertLevel level = AlertLevel.valueOf(domibusPropertyProvider.getProperty(getAlertLevelPropertyName()));
                final AccountDisabledMoment moment = AccountDisabledMoment.valueOf(domibusPropertyProvider.getProperty(getAlertMomentPropertyName()));
                final String mailSubject = domibusPropertyProvider.getProperty(getAlertEmailSubjectPropertyName());

                LOG.info("[{}] module activated for domain:[{}]", getModuleName(), currentDomain);
                return new AccountDisabledModuleConfiguration(getAlertType(), level, moment, mailSubject);

            } catch (Exception e) {
                LOG.warn("An error occurred while reading [{}] module configuration for domain:[{}], ", getModuleName(), currentDomain, e);
                return new AccountDisabledModuleConfiguration(getAlertType());
            }
        }
    }

    abstract class AccountEnabledConfigurationReader implements UserAuthenticationConfiguration {
        protected abstract AlertType getAlertType();

        protected abstract String getModuleName();

        protected abstract String getAlertActivePropertyName();

        protected abstract String getAlertLevelPropertyName();

        protected abstract String getAlertMomentPropertyName();

        protected abstract String getAlertEmailSubjectPropertyName();

        protected AccountEnabledModuleConfiguration readConfiguration() {
            //todo
            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            try {

                final Boolean alertActive = isAlertModuleEnabled();
                final Boolean accountEnabledActive = domibusPropertyProvider.getBooleanProperty(getAlertActivePropertyName());
                if (!alertActive || !accountEnabledActive) {
                    LOG.debug("domain:[{}] [{}] module is inactive for the following reason: global alert module active:[{}], account disabled module active:[{}]"
                            , currentDomain, getModuleName(), alertActive, accountEnabledActive);
                    return new AccountEnabledModuleConfiguration(getAlertType());
                }

                final AlertLevel level = AlertLevel.valueOf(domibusPropertyProvider.getProperty(getAlertLevelPropertyName()));
                final AccountDisabledMoment moment = AccountDisabledMoment.valueOf(domibusPropertyProvider.getProperty(getAlertMomentPropertyName()));
                final String mailSubject = domibusPropertyProvider.getProperty(getAlertEmailSubjectPropertyName());

                LOG.info("[{}] module activated for domain:[{}]", getModuleName(), currentDomain);
                return new AccountEnabledModuleConfiguration(getAlertType(), level, moment, mailSubject);

            } catch (Exception e) {
                LOG.warn("An error occurred while reading [{}] module configuration for domain:[{}], ", getModuleName(), currentDomain, e);
                return new AccountEnabledModuleConfiguration(getAlertType());
            }
        }
    }

    class ConsoleAccountDisabledConfigurationReader extends AccountDisabledConfigurationReader {

        @Override
        protected AlertType getAlertType() {
            return AlertType.USER_ACCOUNT_DISABLED;
        }

        @Override
        protected String getModuleName() {
            return "Alert account disabled";
        }

        @Override
        protected String getAlertActivePropertyName() {
            return DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE;
        }

        @Override
        protected String getAlertLevelPropertyName() {
            return DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL;
        }

        @Override
        protected String getAlertMomentPropertyName() {
            return DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT;
        }

        @Override
        protected String getAlertEmailSubjectPropertyName() {
            return DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT;
        }

        @Override
        public boolean shouldCheckExtAuthEnabled() {
            return domibusConfigurationService.isExtAuthProviderEnabled();
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
        protected String getAlertMomentPropertyName() {
            return DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_MOMENT;
        }

        @Override
        protected String getAlertEmailSubjectPropertyName() {
            return DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_SUBJECT;
        }

        @Override
        public boolean shouldCheckExtAuthEnabled() {
            return domibusConfigurationService.isExtAuthProviderEnabled();
        }
    }

    class PluginAccountDisabledConfigurationReader extends AccountDisabledConfigurationReader {

        @Override
        protected AlertType getAlertType() {
            return AlertType.PLUGIN_USER_ACCOUNT_DISABLED;
        }

        @Override
        protected String getModuleName() {
            return "Alert plugin account disabled";
        }

        @Override
        protected String getAlertActivePropertyName() {
            return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_ACTIVE;
        }

        @Override
        protected String getAlertLevelPropertyName() {
            return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_LEVEL;
        }

        @Override
        protected String getAlertMomentPropertyName() {
            return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_MOMENT;
        }

        @Override
        protected String getAlertEmailSubjectPropertyName() {
            return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_SUBJECT;
        }

        @Override
        public boolean shouldCheckExtAuthEnabled() {
            return false;
        }
    }
    class PluginAccountEnabledConfigurationReader extends AccountEnabledConfigurationReader {

        @Override
        protected AlertType getAlertType() {
            return AlertType.PLUGIN_USER_ACCOUNT_ENABLED;
        }

        @Override
        protected String getModuleName() {
            return "Alert plugin account disabled";
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
        protected String getAlertMomentPropertyName() {
            return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_MOMENT;
        }

        @Override
        protected String getAlertEmailSubjectPropertyName() {
            return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_SUBJECT;
        }

        @Override
        public boolean shouldCheckExtAuthEnabled() {
            return false;
        }
    }

    class ConsoleLoginFailConfigurationReader extends LoginFailConfigurationReader {
        @Override
        protected AlertType getAlertType() {
            return AlertType.USER_LOGIN_FAILURE;
        }

        @Override
        protected String getModuleName() {
            return "Alert Login failure";
        }

        @Override
        protected String getAlertActivePropertyName() {
            return DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE;
        }

        @Override
        protected String getAlertLevelPropertyName() {
            return DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL;
        }

        @Override
        protected String getAlertEmailSubjectPropertyName() {
            return DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT;
        }

        @Override
        public boolean shouldCheckExtAuthEnabled() {

            return domibusConfigurationService.isExtAuthProviderEnabled();
        }
    }

    protected ImminentExpirationCertificateModuleConfiguration readImminentExpirationCertificateConfiguration() {
        Domain domain = domainContextProvider.getCurrentDomainSafely();
        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean imminentExpirationActive = domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
            if (!alertActive || !imminentExpirationActive) {
                LOG.debug("domain:[{}] Alert certificate imminent expiration module is inactive for the following reason:global alert module active[{}], certificate imminent expiration module active[{}]",
                        domain, alertActive, imminentExpirationActive);
                return new ImminentExpirationCertificateModuleConfiguration();
            }
            final Integer imminentExpirationDelay = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS);
            final Integer imminentExpirationFrequency = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS);
            final AlertLevel imminentExpirationAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL));
            final String imminentExpirationMailSubject = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT);

            LOG.info("Alert certificate imminent expiration module activated for domain:[{}]", domain);
            return new ImminentExpirationCertificateModuleConfiguration(
                    imminentExpirationDelay,
                    imminentExpirationFrequency,
                    imminentExpirationAlertLevel,
                    imminentExpirationMailSubject);

        } catch (Exception e) {
            LOG.warn("An error occurred while reading certificate scanner alert module configuration for domain:[{}], ", domain, e);
            return new ImminentExpirationCertificateModuleConfiguration();
        }

    }

    protected ExpiredCertificateModuleConfiguration readExpiredCertificateConfiguration() {
        Domain domain = domainContextProvider.getCurrentDomainSafely();
        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean expiredActive = domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
            if (!alertActive || !expiredActive) {
                LOG.debug("domain:[{}] Alert certificate expired module is inactive for the following reason:global alert module active[{}], certificate expired module active[{}]",
                        domain, alertActive, expiredActive);
                return new ExpiredCertificateModuleConfiguration();
            }
            final Integer revokedFrequency = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS);
            final Integer revokedDuration = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS);
            final AlertLevel revocationLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_LEVEL));
            final String expiredMailSubject = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT);

            LOG.info("Alert certificate expired activated for domain:[{}]", domain);
            return new ExpiredCertificateModuleConfiguration(
                    revokedFrequency,
                    revokedDuration,
                    revocationLevel,
                    expiredMailSubject);

        } catch (Exception e) {
            LOG.error("An error occurred while reading certificate scanner alert module configuration for domain:[{}], ", domain, e);
            return new ExpiredCertificateModuleConfiguration();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RepetitiveAlertModuleConfiguration getRepetitiveAlertConfiguration(AlertType alertType) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginFailureModuleConfiguration getPluginLoginFailureConfiguration() {
        return pluginLoginFailureConfigurationLoader.getConfiguration(new PluginLoginFailConfigurationReader()::readConfiguration);
    }

    @Override
    public AccountDisabledModuleConfiguration getPluginAccountDisabledConfiguration() {
        return pluginAccountDisabledConfigurationLoader.getConfiguration(new PluginAccountDisabledConfigurationReader()::readConfiguration);
    }

    @Override
    public AccountEnabledModuleConfiguration getPluginAccountEnabledConfiguration() {
        return pluginAccountEnabledConfigurationLoader.getConfiguration(new PluginAccountEnabledConfigurationReader()::readConfiguration);
    }

    abstract class LoginFailConfigurationReader implements UserAuthenticationConfiguration {
        protected abstract AlertType getAlertType();

        protected abstract String getModuleName();

        protected abstract String getAlertActivePropertyName();

        protected abstract String getAlertLevelPropertyName();

        protected abstract String getAlertEmailSubjectPropertyName();

        protected LoginFailureModuleConfiguration readConfiguration() {
            Domain domain = domainContextProvider.getCurrentDomainSafely();
            try {
                if (shouldCheckExtAuthEnabled()) {
                    //ECAS or other provider
                    LOG.debug("[{}] module is inactive for the following reason: external authentication provider is enabled", getModuleName());
                    return new LoginFailureModuleConfiguration(getAlertType());
                }

                final Boolean alertActive = isAlertModuleEnabled();
                final Boolean loginFailureActive = domibusPropertyProvider.getBooleanProperty(getAlertActivePropertyName());

                if (!alertActive || !loginFailureActive) {
                    LOG.debug("{} module is inactive for the following reason:global alert module active[{}], login failure module active[{}]", getModuleName(), alertActive, loginFailureActive);
                    return new LoginFailureModuleConfiguration(getAlertType());
                }

                final AlertLevel loginFailureAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(getAlertLevelPropertyName()));

                final String loginFailureMailSubject = domibusPropertyProvider.getProperty(getAlertEmailSubjectPropertyName());

                LOG.info("{} module activated for domain:[{}]", getModuleName(), domain);
                return new LoginFailureModuleConfiguration(getAlertType(), loginFailureAlertLevel, loginFailureMailSubject);

            } catch (Exception e) {
                LOG.warn("An error occurred while reading {} module configuration for domain:[{}], ", getModuleName(), domain, e);
                return new LoginFailureModuleConfiguration(getAlertType());
            }
        }
    }

    class PluginLoginFailConfigurationReader extends LoginFailConfigurationReader {
        @Override
        protected AlertType getAlertType() {
            return AlertType.PLUGIN_USER_LOGIN_FAILURE;
        }

        @Override
        protected String getModuleName() {
            return "Alert Plugin Login failure";
        }

        @Override
        protected String getAlertActivePropertyName() {
            return DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_ACTIVE;
        }

        @Override
        protected String getAlertLevelPropertyName() {
            return DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_LEVEL;
        }

        @Override
        protected String getAlertEmailSubjectPropertyName() {
            return DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_MAIL_SUBJECT;
        }

        @Override
        public boolean shouldCheckExtAuthEnabled() {
            return false;
        }
    }

}
