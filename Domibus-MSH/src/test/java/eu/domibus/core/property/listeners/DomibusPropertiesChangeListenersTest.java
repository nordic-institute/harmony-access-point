package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.common.validators.GatewayConfigurationValidator;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.payload.encryption.PayloadEncryptionService;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorage;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.core.property.DomibusPropertyMetadataManagerImpl;
import eu.domibus.ebms3.puller.PullFrequencyHelper;
import eu.domibus.messaging.MessageListenerContainerInitializer;
import eu.domibus.proxy.DomibusProxyService;
import eu.domibus.quartz.DomibusQuartzStarter;
import eu.domibus.web.rest.validators.BaseBlacklistValidator;
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
    @Injectable
    DomibusPropertyMetadataManagerImpl domibusPropertyMetadataManager;

    @Injectable
    List<BaseBlacklistValidator> blacklistValidators;
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


    @Mocked
    PayloadFileStorage mockStorage = new PayloadFileStorage();

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
        };

        new Expectations() {{
            payloadFileStorageProvider.forDomain((Domain) any);
            result = mockStorage;
        }};

        Map<String, DomibusPropertyMetadata> properties = domibusPropertyMetadataManager.getKnownProperties();

        for (String propertyName : properties.keySet()) {
            if (domibusPropertyMetadataManager.hasKnownProperty(propertyName)) {
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
            cryptoService.refresh();
            gatewayConfigurationValidator.validateConfiguration();
            domibusProxyService.resetProxy();
            messageListenerContainerInitializer.createSendMessageListenerContainer((Domain) any);
            domibusQuartzStarter.rescheduleJob((Domain) any, anyString, anyString);
            domibusCacheService.clearCache(anyString);
            pModeProvider.refresh();
            blacklistValidators.forEach((Consumer) any);
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