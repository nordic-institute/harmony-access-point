package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
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
// todo: call clear loaders on enabled property changed
    private static final Logger LOG = DomibusLoggerFactory.getLogger(MultiDomainAlertConfigurationServiceImpl.class);

    static final String DOMIBUS_ALERT_SUPER_INSTANCE_NAME_SUBJECT = DOMIBUS_INSTANCE_NAME;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    List<AlertConfigurationManager> alertConfigurationManagers;

    @Autowired
    private MessagingConfigurationManager messagingConfigurationManager;

    @Autowired
    ConsoleAccountDisabledConfigurationManager consoleAccountDisabledConfigurationManager;

    @Autowired
    private PluginAccountDisabledConfigurationManager pluginAccountDisabledConfigurationManager;

    @Autowired
    private ConsoleAccountEnabledConfigurationManager consoleAccountEnabledConfigurationManager;

    @Autowired
    private PluginAccountEnabledConfigurationManager pluginAccountEnabledConfigurationManager;

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

//    @Autowired
//    private ConsolePasswordExpiredAlertConfigurationManager consolePasswordExpiredAlertConfigurationManager;
//
//    @Autowired
//    private ConsolePasswordImminentExpirationAlertConfigurationManager consolePasswordImminentExpirationAlertConfigurationManager;
//
//    @Autowired
//    private PluginPasswordExpiredAlertConfigurationManager pluginPasswordExpiredAlertConfigurationManager;
//
//    @Autowired
//    private PluginPasswordImminentExpirationAlertConfigurationManager pluginPasswordImminentExpirationAlertConfigurationManager;


//    @Autowired
//    private RepetitiveAlertConfigurationHolder passwordExpirationAlertsConfigurationHolder;


    @Override
    public MessagingModuleConfiguration getMessageCommunicationConfiguration() {
        return messagingConfigurationManager.getConfiguration();
    }

    @Override
    public AccountDisabledModuleConfiguration getAccountDisabledConfiguration() {
        return consoleAccountDisabledConfigurationManager.getConfiguration();
    }

    @Override
    public AlertModuleConfigurationBase getAccountEnabledConfiguration() {
        return consoleAccountEnabledConfigurationManager.getConfiguration();
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
        getModuleConfigurationManager(alertType).reset();
//        this.passwordExpirationAlertsConfigurationHolder.clearConfiguration(alertType);
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
        consoleAccountDisabledConfigurationManager.reset();
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
        clearCommonConfiguration();
        clearLoginFailureConfiguration();
        clearAccountDisabledConfiguration();
        clearPluginLoginFailureConfiguration();
        clearPluginAccountDisabledConfiguration();
        clearMessageCommunicationConfiguration();
        clearExpiredCertificateConfiguration();
        clearImminentExpirationCertificateConfiguration();
        // todo: do not forget clear enabled alert
        // todo: reset all by scanning alert types
//        passwordExpirationAlertsConfigurationHolder.clearConfiguration();
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

    @Override
    public RepetitiveAlertModuleConfiguration getRepetitiveAlertConfiguration(AlertType alertType) {
        // todo: try to get rid of cast or even of get by AlertType
        return (RepetitiveAlertModuleConfiguration) getModuleConfiguration(alertType);
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
        return pluginAccountEnabledConfigurationManager.getConfiguration();
    }

    private AlertModuleConfiguration getModuleConfiguration(AlertType alertType) {
        return getModuleConfigurationManager(alertType).getConfiguration();
    }

    private AlertConfigurationManager getModuleConfigurationManager(AlertType alertType) {
        Optional<AlertConfigurationManager> res = alertConfigurationManagers.stream().filter(el -> el.getAlertType() == alertType).findFirst();
        if (!res.isPresent()) {
            LOG.error("Invalid alert type[{}]", alertType);
            throw new IllegalArgumentException("Invalid alert type");
        }
        return res.get();
    }
}
