package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.core.alerts.MailSender;
import eu.domibus.core.alerts.configuration.account.disabled.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.configuration.account.disabled.console.ConsoleAccountDisabledConfigurationManager;
import eu.domibus.core.alerts.configuration.account.disabled.plugin.PluginAccountDisabledConfigurationManager;
import eu.domibus.core.alerts.configuration.certificate.expired.ExpiredCertificateConfigurationManager;
import eu.domibus.core.alerts.configuration.certificate.imminent.ImminentExpirationCertificateConfigurationManager;
import eu.domibus.core.alerts.configuration.common.CommonConfigurationManager;
import eu.domibus.core.alerts.configuration.login.console.ConsoleLoginFailConfigurationManager;
import eu.domibus.core.alerts.configuration.login.plugin.PluginLoginFailConfigurationManager;
import eu.domibus.core.alerts.configuration.messaging.MessagingConfigurationManager;
import eu.domibus.core.alerts.configuration.password.expired.console.ConsolePasswordExpiredAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.password.expired.plugin.PluginPasswordExpiredAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.password.imminent.console.ConsolePasswordImminentExpirationAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.password.imminent.plugin.PluginPasswordImminentExpirationAlertConfigurationManager;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.jms.MessageListenerContainerInitializer;
import eu.domibus.core.message.pull.PullFrequencyHelper;
import eu.domibus.core.payload.encryption.PayloadEncryptionService;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorage;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.property.CorePropertyMetadataManagerImpl;
import eu.domibus.core.property.GatewayConfigurationValidator;
import eu.domibus.core.proxy.DomibusProxyService;
import eu.domibus.core.rest.validators.BlacklistValidator;
import eu.domibus.core.scheduler.DomibusQuartzStarter;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class DomibusPropertiesChangeListenersTest {

    @Tested
    BlacklistChangeListener blacklistChangeListener;

    @Tested
    ConcurrencyChangeListener concurrencyChangeListener;

    @Tested
    CronExpressionChangeListener cronExpressionChangeListener;

    @Tested
    CryptoChangeListener cryptoChangeListener;

    @Tested
    DispatchClientChangeListener dispatchClientChangeListener;

    @Tested
    DomainTitleChangeListener domainTitleChangeListener;

    @Tested
    DynamicDiscoveryEndpointChangeListener dynamicDiscoveryEndpointChangeListener;

    @Tested
    DynamicDiscoveryClientChangeListener dynamicDiscoveryClientChangeListener;

    @Tested
    PayloadEncryptionChangeListener payloadEncryptionChangeListener;

    @Tested
    ProxyChangeListener proxyChangeListener;

    @Tested
    PullConfigurationChangeListener pullConfigurationChangeListener;

    @Tested
    StorageChangeListener storageChangeListener;

    @Tested
    AlertActiveChangeListener alertActiveChangeListener;

    @Tested
    AlertConsoleAccountDisabledConfigurationChangeListener alertConsoleAccountDisabledConfigurationChangeListener;

    @Tested
    AlertCertificateExpiredConfigurationChangeListener alertCertificateExpiredConfigurationChangeListener;

    @Tested
    AlertCertificateImminentExpirationConfigurationChangeListener alertCertificateImminentExpirationConfigurationChangeListener;

    @Tested
    AlertCommonConfigurationChangeListener alertCommonConfigurationChangeListener;

    @Tested
    AlertConsoleLoginFailureConfigurationChangeListener alertConsoleLoginFailureConfigurationChangeListener;

    @Tested
    AlertMailChangeListener alertMailChangeListener;

    @Tested
    AlertMessagingConfigurationChangeListener alertMessagingConfigurationChangeListener;

    @Tested
    AlertPasswordExpiredConfigurationChangeListener alertPasswordExpiredConfigurationChangeListener;

    @Tested
    AlertPasswordImminentExpirationConfigurationChangeListener alertPasswordImminentExpirationConfigurationChangeListener;

    @Tested
    AlertPluginAccountDisabledConfigurationChangeListener alertPluginAccountDisabledConfigurationChangeListener;

    @Tested
    AlertPluginLoginFailureConfigurationChangeListener alertPluginLoginFailureConfigurationChangeListener;

    @Tested
    AlertPluginPasswordExpiredConfigurationChangeListener alertPluginPasswordExpiredConfigurationChangeListener;

    @Tested
    AlertPluginPasswordImminentExpirationConfigurationChangeListener alertPluginPasswordImminentExpirationConfigurationChangeListener;

    @Tested
    CRLChangeListener crlChangeListener;

    @Tested
    @Injectable
    CorePropertyMetadataManagerImpl corePropertyMetadataManager;

    @Injectable
    List<BlacklistValidator> blacklistValidators;

    @Injectable
    DomainService domainService;

    @Injectable
    ApplicationContext applicationContext;

    @Injectable
    MessageListenerContainerInitializer messageListenerContainerInitializer;

    @Injectable
    DomibusQuartzStarter domibusQuartzStarter;

    @Injectable
    protected MultiDomainCryptoService cryptoService;

    @Injectable
    protected GatewayConfigurationValidator gatewayConfigurationValidator;

    @Injectable
    private DomibusCacheService domibusCacheService;

    @Injectable
    protected PModeProvider pModeProvider;

    @Injectable
    protected PayloadEncryptionService payloadEncryptionService;

    @Injectable
    protected DomibusProxyService domibusProxyService;

    @Injectable
    private PullFrequencyHelper pullFrequencyHelper;

    @Injectable
    PayloadFileStorageProvider payloadFileStorageProvider;

    @Injectable
    DomibusScheduler domibusScheduler;

    @Mocked
    PayloadFileStorage mockStorage = new PayloadFileStorage();

    @Injectable
    private MailSender mailSender;

    @Injectable
    private AlertConfigurationService alertConfigurationService;

    @Injectable
    private ConfigurationLoader<AccountDisabledModuleConfiguration> pluginAccountDisabledConfigurationLoader;

    @Injectable
    ConsoleAccountDisabledConfigurationManager clearConsoleAccountDisabledConfiguration;

    @Injectable
    ExpiredCertificateConfigurationManager expiredCertificateConfigurationManager;

    @Injectable
    ImminentExpirationCertificateConfigurationManager imminentExpirationCertificateConfigurationManager;

    @Injectable
    CommonConfigurationManager commonConfigurationManager;

    @Injectable
    ConsoleLoginFailConfigurationManager consoleLoginFailConfigurationManager;

    @Injectable
    MessagingConfigurationManager messagingConfigurationManager;

    @Injectable
    ConsolePasswordExpiredAlertConfigurationManager consolePasswordExpiredAlertConfigurationManager;

    @Injectable
    PluginPasswordExpiredAlertConfigurationManager pluginPasswordExpiredAlertConfigurationManager;

    @Injectable
    ConsolePasswordImminentExpirationAlertConfigurationManager consolePasswordImminentExpirationAlertConfigurationManager;

    @Injectable
    PluginPasswordImminentExpirationAlertConfigurationManager pluginPasswordImminentExpirationAlertConfigurationManager;

    @Injectable
    PluginLoginFailConfigurationManager pluginLoginFailConfigurationManager;

    @Injectable
    PluginAccountDisabledConfigurationManager pluginAccountDisabledConfigurationManager;

    @Test
    public void testPropertyChangeListeners() throws Exception {
        DomibusPropertyChangeListener[] domibusPropertyChangeListeners = new DomibusPropertyChangeListener[]{
                blacklistChangeListener,
                concurrencyChangeListener,
                cronExpressionChangeListener,
                cryptoChangeListener,
                dispatchClientChangeListener,
                domainTitleChangeListener,
                dynamicDiscoveryEndpointChangeListener,
                dynamicDiscoveryClientChangeListener,
                payloadEncryptionChangeListener,
                proxyChangeListener,
                pullConfigurationChangeListener,
                storageChangeListener,
                crlChangeListener,

                alertActiveChangeListener,
                alertConsoleAccountDisabledConfigurationChangeListener,
                alertCertificateExpiredConfigurationChangeListener,
                alertCertificateImminentExpirationConfigurationChangeListener,
                alertCommonConfigurationChangeListener,
                alertConsoleLoginFailureConfigurationChangeListener,
                alertMailChangeListener,
                alertMessagingConfigurationChangeListener,
                alertPasswordExpiredConfigurationChangeListener,
                alertPasswordImminentExpirationConfigurationChangeListener,
                alertPluginAccountDisabledConfigurationChangeListener,
                alertPluginLoginFailureConfigurationChangeListener,
                alertPluginPasswordExpiredConfigurationChangeListener,
                alertPluginPasswordImminentExpirationConfigurationChangeListener
        };

        new Expectations() {{
            payloadFileStorageProvider.forDomain((Domain) any);
            result = mockStorage;
        }};

        Map<String, DomibusPropertyMetadata> properties = corePropertyMetadataManager.getKnownProperties();

        for (String propertyName : properties.keySet()) {
            if (corePropertyMetadataManager.hasKnownProperty(propertyName)) {
                for (DomibusPropertyChangeListener listener : domibusPropertyChangeListeners) {
                    if (listener.handlesProperty(propertyName)) {
                        String testValue = testPropertyValue(propertyName);
                        listener.propertyValueChanged("default", propertyName, testValue);
                    }
                }
            }
        }

        new Verifications() {{
            mockStorage.initFileSystemStorage();
            pullFrequencyHelper.reset();
            payloadEncryptionService.createPayloadEncryptionKeyIfNotExists((Domain) any);
            cryptoService.reset();
            gatewayConfigurationValidator.validateConfiguration();
            domibusProxyService.resetProxy();
            messageListenerContainerInitializer.setConcurrency((Domain) any, anyString, anyString);
            domibusScheduler.rescheduleJob((Domain) any, anyString, anyString);
            domibusCacheService.clearCache(anyString);
            pModeProvider.refresh();
            blacklistValidators.forEach((Consumer) any);

            mailSender.reset();
            alertConfigurationService.resetAll();
            clearConsoleAccountDisabledConfiguration.reset();
            expiredCertificateConfigurationManager.reset();
            imminentExpirationCertificateConfigurationManager.reset();
            commonConfigurationManager.reset();
            consoleLoginFailConfigurationManager.reset();
            mailSender.reset();
            messagingConfigurationManager.reset();
            consolePasswordExpiredAlertConfigurationManager.reset();
            pluginPasswordExpiredAlertConfigurationManager.reset();
            consolePasswordImminentExpirationAlertConfigurationManager.reset();
            pluginPasswordImminentExpirationAlertConfigurationManager.reset();
            domibusCacheService.clearCache(anyString);
        }};
    }

    private String testPropertyValue(String propertyName) {
        if (propertyName.endsWith("active") || propertyName.endsWith("enabled")) {
            return "true";
        } else if (propertyName.contains(".cron")) {
            return "0 0/11 * * * ?";
        } else if (propertyName.contains("concurrency") || propertyName.contains("concurency")) {
            return "10-20";
        } else {
            return "123";
        }
    }
}