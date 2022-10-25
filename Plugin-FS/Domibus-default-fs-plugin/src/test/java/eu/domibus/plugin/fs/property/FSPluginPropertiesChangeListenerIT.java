package eu.domibus.plugin.fs.property;

import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.exceptions.DomibusServiceExtException;
import eu.domibus.ext.services.BackendConnectorProviderExtService;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusSchedulerExtService;
import eu.domibus.plugin.fs.property.listeners.FSPluginEnabledChangeListener;
import eu.domibus.plugin.fs.property.listeners.OutQueueConcurrencyChangeListener;
import eu.domibus.plugin.fs.property.listeners.TriggerChangeListener;
import eu.domibus.plugin.fs.queue.FSSendMessageListenerContainer;
import eu.domibus.test.AbstractIT;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static eu.domibus.plugin.fs.FSPluginImpl.PLUGIN_NAME;
import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.*;

/**
 * @author Ion Perpegel
 * @author Catalin Enache
 * @since 4.2
 */
public class FSPluginPropertiesChangeListenerIT extends AbstractIT {

    @Autowired
    private DomibusSchedulerExtService domibusSchedulerExt;

    @Autowired
    private FSSendMessageListenerContainer messageListenerContainer;

    @Autowired
    private OutQueueConcurrencyChangeListener outQueueConcurrencyChangeListener;

    @Autowired
    private TriggerChangeListener triggerChangeListener;

    @Autowired
    private FSPluginEnabledChangeListener enabledChangeListener;

    @Autowired
    private DomainExtService domainExtService;

    @Autowired
    private FSPluginProperties fsPluginProperties;

    @Autowired
    BackendConnectorProviderExtService backendConnectorProviderExtService;

    @Configuration
    static class ContextConfiguration {

        @Primary
        @Bean
        public BackendConnectorProviderExtService backendConnectorProviderExtService() {
            return Mockito.mock(BackendConnectorProviderExtService.class);
        }

        @Primary
        @Bean
        public DomibusSchedulerExtService domibusSchedulerExt() {
            return Mockito.mock(DomibusSchedulerExtService.class);
        }

        @Primary
        @Bean
        public FSSendMessageListenerContainer messageListenerContainer() {
            return Mockito.mock(FSSendMessageListenerContainer.class);
        }

    }

    @Test
    public void testTriggerChangeListener() {
        boolean handlesWorkerInterval = triggerChangeListener.handlesProperty(SEND_WORKER_INTERVAL);
        Assert.assertTrue(handlesWorkerInterval);

        try {
            triggerChangeListener.propertyValueChanged("default", SEND_WORKER_INTERVAL, "wrong-value");
            Assert.fail("Expected exception not raised");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid"));
        }

        triggerChangeListener.propertyValueChanged("default", SEND_WORKER_INTERVAL, "3000");
        triggerChangeListener.propertyValueChanged("default", SENT_PURGE_WORKER_CRONEXPRESSION, "0 0/15 * * * ?");

        Mockito.verify(domibusSchedulerExt, Mockito.times(1)).rescheduleJob("default", "fsPluginSendMessagesWorkerJob", 3000);
        Mockito.verify(domibusSchedulerExt, Mockito.times(1)).rescheduleJob("default", "fsPluginPurgeSentWorkerJob", "0 0/15 * * * ?");
    }

    @Test
    public void testOutQueueConcurrencyChangeListener() {

        boolean handlesProperty = outQueueConcurrencyChangeListener.handlesProperty(OUT_QUEUE_CONCURRENCY);
        Assert.assertTrue(handlesProperty);

        DomainDTO aDefault = domainExtService.getDomain("default");

        outQueueConcurrencyChangeListener.propertyValueChanged("default", OUT_QUEUE_CONCURRENCY, "1-2");
        Mockito.verify(messageListenerContainer, Mockito.times(1)).updateMessageListenerContainerConcurrency(aDefault, "1-2");
    }

    @Test(expected = DomibusPropertyException.class)
    public void testEnabledChangeListener1() {
        boolean handlesProperty = enabledChangeListener.handlesProperty(DOMAIN_ENABLED);
        Assert.assertTrue(handlesProperty);

        fsPluginProperties.setKnownPropertyValue(DOMAIN_ENABLED, "false");
    }

    @Test
    public void testEnabledChangeListener() {
        String domainCode = "default";

        boolean handlesProperty = enabledChangeListener.handlesProperty(DOMAIN_ENABLED);
        Assert.assertTrue(handlesProperty);

        if (!fsPluginProperties.getDomainEnabled(domainCode)) {
            Mockito.verify(backendConnectorProviderExtService, Mockito.times(1)).backendConnectorEnabled(PLUGIN_NAME, domainCode);
            fsPluginProperties.setKnownPropertyValue(DOMAIN_ENABLED, "true");
            Mockito.verify(backendConnectorProviderExtService, Mockito.times(0)).backendConnectorEnabled(PLUGIN_NAME, domainCode);
        } else {
            fsPluginProperties.setKnownPropertyValue(DOMAIN_ENABLED, "true");
            Mockito.verify(backendConnectorProviderExtService, Mockito.times(0)).backendConnectorEnabled(PLUGIN_NAME, domainCode);
        }
    }
}
