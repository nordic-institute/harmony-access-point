package eu.domibus.core.alerts.service;

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

    static final String DOMIBUS_ALERT_SUPER_INSTANCE_NAME_SUBJECT = "domibus.instance.name";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private ConfigurationLoader<MessagingModuleConfiguration> messagingConfigurationLoader;

    @Autowired
    private ConfigurationLoader<AccountDisabledModuleConfiguration> accountDisabledConfigurationLoader;

    @Autowired
    private ConfigurationLoader<AccountDisabledModuleConfiguration> pluginAccountDisabledConfigurationLoader;

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
            case PLUGIN_USER_ACCOUNT_DISABLED:
                return getPluginAccountDisabledConfiguration();
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
        String propertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_ACTIVE, DOMIBUS_ALERT_SUPER_ACTIVE);
        return domibusPropertyProvider.getBooleanDomainProperty(propertyName);
    }

    @Override
    public String getSendEmailActivePropertyName() {
        return getDomainOrSuperProperty(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE, DOMIBUS_ALERT_SUPER_MAIL_SENDING_ACTIVE);
    }

    @Override
    public String getAlertRetryMaxAttemptPropertyName() {
        return getDomainOrSuperProperty(DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS, DOMIBUS_ALERT_SUPER_RETRY_MAX_ATTEMPTS);
    }

    @Override
    public String getAlertRetryTimePropertyName() {
        return getDomainOrSuperProperty(DOMIBUS_ALERT_RETRY_TIME, DOMIBUS_ALERT_SUPER_RETRY_TIME);
    }

    @Override
    public String getAlertSuperServerNameSubjectPropertyName() {
        return DOMIBUS_ALERT_SUPER_INSTANCE_NAME_SUBJECT;
    }


    private String getDomainOrSuperProperty(final String domainPropertyName, final String superPropertyName) {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        if (currentDomain == null) {
            return superPropertyName;
        }
        return domainPropertyName;
    }

    protected CommonConfiguration readCommonConfiguration(Domain domain) {
        //TODO: revisit this: I am receiving the domain as a parameter but I am reading the bellow properties for the current domain!! Is it OK?
        final boolean emailActive = domibusPropertyProvider.getBooleanDomainProperty(getSendEmailActivePropertyName());
        final String alertCleanerLifeTimePropertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME, DOMIBUS_ALERT_SUPER_CLEANER_ALERT_LIFETIME);
        final Integer alertLifeTimeInDays = domibusPropertyProvider.getIntegerDomainProperty(alertCleanerLifeTimePropertyName);

        if (!emailActive) {
            return new CommonConfiguration(alertLifeTimeInDays);
        }

        return readDomainEmailConfiguration(domain, alertLifeTimeInDays);
    }

    private CommonConfiguration readDomainEmailConfiguration(Domain domain, Integer alertLifeTimeInDays) {
        final String alertSenderPropertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_SENDER_EMAIL, DOMIBUS_ALERT_SUPER_SENDER_EMAIL);
        final String alertEmailSender = domibusPropertyProvider.getProperty(domain, alertSenderPropertyName);
        final String alertReceiverPropertyName = getDomainOrSuperProperty(DOMIBUS_ALERT_RECEIVER_EMAIL, DOMIBUS_ALERT_SUPER_RECEIVER_EMAIL);
        final String alertEmailReceiver = domibusPropertyProvider.getProperty(domain, alertReceiverPropertyName);

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
            LOG.error("Alert module can not send email, mail sender property name:[{}]/value[{}] and receiver property name:[{}]/value[{}] are mandatory in domain:[{}]", alertSenderPropertyName, alertEmailSender, alertReceiverPropertyName, alertEmailReceiver, domain);
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

    protected MessagingModuleConfiguration readMessageConfiguration(Domain domain) {
        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean messageAlertActive = domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            if (!alertActive || !messageAlertActive) {
                LOG.debug("domain:[{}] Alert message status change module is inactive for the following reason:global alert module active[{}], message status change module active[{}]", domain, alertActive, messageAlertActive);
                return new MessagingModuleConfiguration();
            }
            final String messageCommunicationStates = domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            final String messageCommunicationLevels = domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            final String mailSubject = domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT);

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
            LOG.info("Alert message status change module activated for domain:[{}]", domain);
            return messagingConfiguration;
        } catch (Exception ex) {
            LOG.warn("Error while configuring message communication alerts for domain:[{}], message alert module will be discarded.", domain, ex);
            return new MessagingModuleConfiguration();
        }

    }

    abstract class AccountDisabledConfigurationReader {
        protected abstract AlertType getAlertType();

        protected abstract String getModuleName();

        protected abstract String getAlertActivePropertyName();

        protected abstract String getAlertLevelPropertyName();

        protected abstract String getAlertMomentPropertyName();

        protected abstract String getAlertEmailSubjectPropertyName();

        protected AccountDisabledModuleConfiguration readConfiguration(Domain domain) {
            try {
                final Boolean alertActive = isAlertModuleEnabled();
                final Boolean accountDisabledActive = domibusPropertyProvider.getBooleanDomainProperty(domain, getAlertActivePropertyName());
                if (!alertActive || !accountDisabledActive) {
                    LOG.debug("domain:[{}] [{}] module is inactive for the following reason:global alert module active[{}], account disabled module active[{}]"
                            , domain, getModuleName(), alertActive, accountDisabledActive);
                    return new AccountDisabledModuleConfiguration(getAlertType());
                }

                final AlertLevel level = AlertLevel.valueOf(domibusPropertyProvider.getDomainProperty(domain, getAlertLevelPropertyName()));
                final AccountDisabledMoment moment = AccountDisabledMoment.valueOf(domibusPropertyProvider.getDomainProperty(domain, getAlertMomentPropertyName()));
                final String mailSubject = domibusPropertyProvider.getDomainProperty(domain, getAlertEmailSubjectPropertyName());

                LOG.info("[{}] module activated for domain:[{}]", getModuleName(), domain);
                return new AccountDisabledModuleConfiguration(getAlertType(), level, moment, mailSubject);

            } catch (Exception e) {
                LOG.warn("An error occurred while reading [{}] module configuration for domain:[{}], ", getModuleName(), domain, e);
                return new AccountDisabledModuleConfiguration(getAlertType());
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
            return getDomainOrSuperProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE, DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_ACTIVE);
        }

        @Override
        protected String getAlertLevelPropertyName() {
            return getDomainOrSuperProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL, DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_LEVEL);
        }

        @Override
        protected String getAlertMomentPropertyName() {
            return getDomainOrSuperProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT, DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_MOMENT);
        }

        @Override
        protected String getAlertEmailSubjectPropertyName() {
            return getDomainOrSuperProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT, DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_SUBJECT);
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
            return getDomainOrSuperProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE, DOMIBUS_ALERT_SUPER_USER_LOGIN_FAILURE_ACTIVE);
        }

        @Override
        protected String getAlertLevelPropertyName() {
            return getDomainOrSuperProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL, DOMIBUS_ALERT_SUPER_USER_LOGIN_FAILURE_LEVEL);
        }

        @Override
        protected String getAlertEmailSubjectPropertyName() {
            return getDomainOrSuperProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT, DOMIBUS_ALERT_SUPER_USER_LOGIN_FAILURE_MAIL_SUBJECT);
        }
    }

    protected ImminentExpirationCertificateModuleConfiguration readImminentExpirationCertificateConfiguration(Domain domain) {

        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean imminentExpirationActive = domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
            if (!alertActive || !imminentExpirationActive) {
                LOG.debug("domain:[{}] Alert certificate imminent expiration module is inactive for the following reason:global alert module active[{}], certificate imminent expiration module active[{}]", domain, alertActive, imminentExpirationActive);
                return new ImminentExpirationCertificateModuleConfiguration();
            }
            final Integer imminentExpirationDelay = domibusPropertyProvider.getIntegerDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS);
            final Integer imminentExpirationFrequency = domibusPropertyProvider.getIntegerDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS);
            final AlertLevel imminentExpirationAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL));
            final String imminentExpirationMailSubject = domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT);

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

    protected ExpiredCertificateModuleConfiguration readExpiredCertificateConfiguration(Domain domain) {

        try {
            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean expiredActive = domibusPropertyProvider.getBooleanDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
            if (!alertActive || !expiredActive) {
                LOG.debug("domain:[{}] Alert certificate expired module is inactive for the following reason:global alert module active[{}], certificate expired module active[{}]", domain, alertActive, expiredActive);
                return new ExpiredCertificateModuleConfiguration();
            }
            final Integer revokedFrequency = domibusPropertyProvider.getIntegerDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS);
            final Integer revokedDuration = domibusPropertyProvider.getIntegerDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS);
            final AlertLevel revocationLevel = AlertLevel.valueOf(domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_LEVEL));
            final String expiredMailSubject = domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT);

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
        return configurationLoader.getConfiguration(new RepetitiveAlertConfigurationReader(alertType)::readConfiguration);
    }

    class RepetitiveAlertConfigurationReader {
        AlertType alertType;
        String property, moduleName;

        public RepetitiveAlertConfigurationReader(AlertType alertType) {
            this.alertType = alertType;
            this.property = alertType.getConfigurationProperty();
            this.moduleName = alertType.getTitle();
        }

        public RepetitiveAlertModuleConfiguration readConfiguration(Domain domain) {
            try {
                final Boolean alertModuleActive = isAlertModuleEnabled();
                final Boolean eventActive = Boolean.valueOf(domibusPropertyProvider.getDomainProperty(domain, property + ".active"));
                if (!alertModuleActive || !eventActive) {
                    LOG.debug("domain:[{}] Alert {} module is inactive for the following reason: global alert module active[{}], event active[{}]", domain, moduleName, alertModuleActive, eventActive);
                    return new RepetitiveAlertModuleConfiguration(alertType);
                }

                final Integer delay = Integer.valueOf(domibusPropertyProvider.getDomainProperty(domain, property + ".delay_days"));
                final Integer frequency = Integer.valueOf(domibusPropertyProvider.getDomainProperty(domain, property + ".frequency_days"));
                final AlertLevel alertLevel = AlertLevel.valueOf(domibusPropertyProvider.getDomainProperty(domain, property + ".level"));
                final String mailSubject = domibusPropertyProvider.getDomainProperty(domain, property + ".mail.subject");

                LOG.info("Alert {} module activated for domain:[{}]", moduleName, domain);
                return new RepetitiveAlertModuleConfiguration(alertType, delay, frequency, alertLevel, mailSubject);
            } catch (Exception e) {
                LOG.warn("An error occurred while reading {} alert module configuration for domain:[{}], ", moduleName, domain, e);
                return new RepetitiveAlertModuleConfiguration(alertType);
            }
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

    abstract class LoginFailConfigurationReader {
        protected abstract AlertType getAlertType();

        protected abstract String getModuleName();

        protected abstract String getAlertActivePropertyName();

        protected abstract String getAlertLevelPropertyName();

        protected abstract String getAlertEmailSubjectPropertyName();

        protected LoginFailureModuleConfiguration readConfiguration(Domain domain) {

            try {
                final Boolean alertActive = isAlertModuleEnabled();

                final Boolean loginFailureActive = domibusPropertyProvider.getBooleanDomainProperty(domain, getAlertActivePropertyName());

                if (!alertActive || !loginFailureActive) {
                    LOG.debug("{} module is inactive for the following reason:global alert module active[{}], login failure module active[{}]", getModuleName(), alertActive, loginFailureActive);
                    return new LoginFailureModuleConfiguration(getAlertType());
                }

                final AlertLevel loginFailureAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getDomainProperty(domain, getAlertLevelPropertyName()));

                final String loginFailureMailSubject = domibusPropertyProvider.getDomainProperty(domain, getAlertEmailSubjectPropertyName());

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
    }

}
