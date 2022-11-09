package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.payload.encryption.PayloadEncryptionService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.core.alerts.MailSender;
import eu.domibus.core.alerts.configuration.account.disabled.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.configuration.global.CommonConfigurationManager;
import eu.domibus.core.alerts.configuration.messaging.MessagingConfigurationManager;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.certificate.crl.CRLService;
import eu.domibus.core.jms.MessageListenerContainerInitializer;
import eu.domibus.core.logging.cxf.DomibusLoggingEventSender;
import eu.domibus.core.message.pull.PullFrequencyHelper;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorage;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.property.CorePropertyMetadataManagerImpl;
import eu.domibus.core.property.GatewayConfigurationValidator;
import eu.domibus.api.proxy.DomibusProxyService;
import eu.domibus.core.rest.validators.BlacklistValidator;
import eu.domibus.core.scheduler.DomibusQuartzStarter;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.junit.Ignore;
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
    AlertCommonConfigurationChangeListener alertCommonConfigurationChangeListener;

    @Tested
    AlertMailChangeListener alertMailChangeListener;

    @Tested
    CRLChangeListener crlChangeListener;

    @Tested
    DomibusLoggingApacheCXFChangeListener loggingApacheCXFChangeListener;

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
    PayloadFileStorage mockStorage = new PayloadFileStorage(null);

    @Injectable
    private MailSender mailSender;

    @Injectable
    private AlertConfigurationService alertConfigurationService;

    @Injectable
    private ConfigurationLoader<AccountDisabledModuleConfiguration> pluginAccountDisabledConfigurationLoader;

    @Injectable
    MessagingConfigurationManager messagingConfigurationManager;

    @Injectable
    CRLService crlService;

    @Injectable
    LoggingFeature loggingFeature;

    @Injectable
    DomibusLoggingEventSender loggingSender;

    @Injectable
    CommonConfigurationManager configurationManager;

    @Test
    @Ignore
    public void testPropertyChangeListeners() {
        DomibusPropertyChangeListener[] domibusPropertyChangeListeners = new DomibusPropertyChangeListener[]{
                blacklistChangeListener,
                concurrencyChangeListener,
                cronExpressionChangeListener,
                dispatchClientChangeListener,
                domainTitleChangeListener,
                dynamicDiscoveryEndpointChangeListener,
                dynamicDiscoveryClientChangeListener,
                payloadEncryptionChangeListener,
                proxyChangeListener,
                pullConfigurationChangeListener,
                storageChangeListener,
                crlChangeListener,
                loggingApacheCXFChangeListener,

                alertActiveChangeListener,
                alertCommonConfigurationChangeListener,
                alertMailChangeListener,
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

        new FullVerifications() {{
            pullFrequencyHelper.reset();
            payloadEncryptionService.createPayloadEncryptionKeyIfNotExists((Domain) any);
            domibusProxyService.resetProxy();
            messageListenerContainerInitializer.setConcurrency((Domain) any, anyString, anyString);
            domibusScheduler.rescheduleJob((Domain) any, anyString, anyString);
            domibusCacheService.clearCache(anyString);
            pModeProvider.refresh();
            blacklistValidators.forEach((Consumer) any);

            mailSender.reset();
            alertConfigurationService.resetAll();

            mailSender.reset();
            messagingConfigurationManager.reset();
            crlService.resetCacheCrlProtocols();

            loggingFeature.setLimit(anyInt);
            loggingSender.setPrintMetadata(anyBoolean);
            loggingSender.setPrintPayload(anyBoolean);
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
