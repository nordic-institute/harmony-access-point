package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.manager.*;
import eu.domibus.core.alerts.configuration.model.*;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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

    @Autowired
    private ConsolePasswordExpiredAlertConfigurationManager consolePasswordExpiredAlertConfigurationManager;

    @Autowired
    private ConsolePasswordImminentExpirationAlertConfigurationManager consolePasswordImminentExpirationAlertConfigurationManager;

    @Autowired
    private PluginPasswordExpiredAlertConfigurationManager pluginPasswordExpiredAlertConfigurationManager;

    @Autowired
    private PluginPasswordImminentExpirationAlertConfigurationManager pluginPasswordImminentExpirationAlertConfigurationManager;


    @Override
    public MessagingModuleConfiguration getMessageCommunicationConfiguration() {
        return messagingConfigurationManager.getConfiguration();
    }

    @Override
    public AccountDisabledModuleConfiguration getConsoleAccountDisabledConfiguration() {
        return consoleAccountDisabledConfigurationManager.getConfiguration();
    }

    @Override
    public AlertModuleConfigurationBase getConsoleAccountEnabledConfiguration() {
        return consoleAccountEnabledConfigurationManager.getConfiguration();
    }

    @Override
    public LoginFailureModuleConfiguration getConsoleLoginFailureConfiguration() {
        return consoleLoginFailConfigurationManager.getConfiguration();
    }

    @Override
    public RepetitiveAlertModuleConfiguration getConsolePasswordExpiredAlertConfigurationManager() {
        return consolePasswordExpiredAlertConfigurationManager.getConfiguration();
    }

    @Override
    public RepetitiveAlertModuleConfiguration getConsolePasswordImminentExpirationAlertConfigurationManager() {
        return consolePasswordImminentExpirationAlertConfigurationManager.getConfiguration();
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

    @Override
    public RepetitiveAlertModuleConfiguration getPluginPasswordExpiredAlertConfigurationManager() {
        return pluginPasswordExpiredAlertConfigurationManager.getConfiguration();
    }

    @Override
    public RepetitiveAlertModuleConfiguration getPluginPasswordImminentExpirationAlertConfigurationManager() {
        return pluginPasswordImminentExpirationAlertConfigurationManager.getConfiguration();
    }

    @Override
    public void clearAllConfigurations() {
        clearCommonConfiguration();
        Arrays.asList(AlertType.values()).forEach(alertType -> getModuleConfigurationManager(alertType).reset());
    }

    @Override
    public void clearCommonConfiguration() {
        commonConfigurationManager.reset();
    }

    @Override
    public void clearConsoleLoginFailureConfiguration() {
        consoleLoginFailConfigurationManager.reset();
    }

    @Override
    public void clearPasswordExpirationAlertConfiguration(AlertType alertType) {
        getModuleConfigurationManager(alertType).reset();
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
    public void clearConsoleAccountDisabledConfiguration() {
        consoleAccountDisabledConfigurationManager.reset();
    }

    @Override
    public void clearPluginAccountEnabledConfiguration() {
        pluginAccountEnabledConfigurationManager.reset();
    }

    @Override
    public void clearConsoleAccountEnabledConfiguration() {
        consoleAccountEnabledConfigurationManager.reset();
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

    protected AlertModuleConfiguration getModuleConfiguration(AlertType alertType) {
        return getModuleConfigurationManager(alertType).getConfiguration();
    }

    protected AlertConfigurationManager getModuleConfigurationManager(AlertType alertType) {
        Optional<AlertConfigurationManager> res = alertConfigurationManagers.stream().filter(el -> el.getAlertType() == alertType).findFirst();
        if (!res.isPresent()) {
            throw new IllegalArgumentException("Invalid alert type");
        }
        return res.get();
    }

    protected <T extends AlertModuleConfiguration> T getAlertConfiguration(AlertType alertType, Class<T> configurationType) {
        AlertModuleConfiguration configuration = getModuleConfiguration(alertType);
        if (!configurationType.isInstance(configuration)) {
            throw new IllegalArgumentException("Invalid configuration type " + configurationType + " for alert type " + alertType);
        }
        return configurationType.cast(configuration);
    }
}
