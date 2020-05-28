package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.ext.services.*;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl;
import eu.domibus.plugin.fs.queue.FSSendMessageListenerContainer;
import eu.domibus.plugin.fs.worker.FSDomainService;
import eu.domibus.plugin.property.PluginPropertyChangeNotifier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.*;

/**
 * @author Ion Perpegel
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class FSPluginPropertiesChangeListenerIT {

    @Configuration
    static class ContextConfiguration {

        @Bean
        public FSPluginProperties pluginProperties() {
            FSPluginProperties fsPluginProperties = new FSPluginProperties();
            return fsPluginProperties;
        }

        @Bean
        public Properties fsPluginProperties() throws IOException {
            Properties properties = new Properties();
            properties.load(this.getClass().getResourceAsStream("../FSPluginPropertiesTest_fs-plugin.properties"));
            return properties;
        }

        @Bean
        public PasswordEncryptionExtService passwordEncryptionExtService() {
            return Mockito.mock(PasswordEncryptionExtService.class);
        }

        @Bean
        public FSDomainService fsDomainService() {
            return Mockito.mock(FSDomainService.class);
        }

        @Bean
        public DomainContextExtService domainContextExtService() {
            return Mockito.mock(DomainContextExtService.class);
        }

        @Bean
        public DomainExtService domainExtService() {
            return Mockito.mock(DomainExtService.class);
        }

        @Bean
        public DomibusSchedulerExtService domibusSchedulerExt() {
            return Mockito.mock(DomibusSchedulerExtService.class);
        }

        @Bean
        public DomibusConfigurationExtService domibusConfigurationExtService() {
            return Mockito.mock(DomibusConfigurationExtService.class);
        }

        @Bean
        public FSSendMessageListenerContainer messageListenerContainer() {
            return Mockito.mock(FSSendMessageListenerContainer.class);
        }

        @Bean
        public PluginPropertyChangeNotifier propertyChangeNotifier() {
            return Mockito.mock(PluginPropertyChangeNotifier.class);
        }

        @Bean
        public TriggerChangeListener triggerChangeListener() {
            return new TriggerChangeListener();
        }

        @Bean
        public OutQueueConcurrencyChangeListener outQueueConcurrencyChangeListener() {
            return new OutQueueConcurrencyChangeListener();
        }

        @Bean
        public DomainPropertiesChangeListener domainPropertiesChangeListener() {
            return new DomainPropertiesChangeListener();
        }

        @Bean
        public FSPluginPropertiesMetadataManagerImpl fsPluginPropertiesMetadataManager() {
            return new FSPluginPropertiesMetadataManagerImpl();
        }
    }

    @Autowired
    private FSPluginProperties fSPluginProperties;

    @Autowired
    private DomibusConfigurationExtService domibusConfigurationExtService;

    @Autowired
    private DomibusSchedulerExtService domibusSchedulerExt;

    @Autowired
    private FSSendMessageListenerContainer messageListenerContainer;

    @Autowired
    private DomainPropertiesChangeListener domainPropertiesChangeListener;

    @Autowired
    private OutQueueConcurrencyChangeListener outQueueConcurrencyChangeListener;

    @Autowired
    private TriggerChangeListener triggerChangeListener;

    @Test
    public void testDomainPropertiesChangeListener() throws Exception {
        boolean handlesOrder = domainPropertiesChangeListener.handlesProperty("fsplugin.order");
        Assert.assertEquals(true, handlesOrder);
        boolean handlesPattern = domainPropertiesChangeListener.handlesProperty("fsplugin.messages.expression");
        Assert.assertEquals(true, handlesPattern);
        boolean notHandled = domainPropertiesChangeListener.handlesProperty("fsplugin.messages.expression.not.handled");
        Assert.assertEquals(false, notHandled);

        final List<String> oldDomains = fSPluginProperties.getDomains();

        domainPropertiesChangeListener.propertyValueChanged("default", "fsplugin.order", "10");
        domainPropertiesChangeListener.propertyValueChanged("default", "fsplugin.messages.expression", "bdx:noprocess#TC1Leg1");

        final List<String> newDomains = fSPluginProperties.getDomains();
        Assert.assertTrue(newDomains != oldDomains);
    }

    @Test
    public void testTriggerChangeListener() throws Exception {
        boolean handlesWorkerInterval = triggerChangeListener.handlesProperty(PROPERTY_PREFIX + SEND_WORKER_INTERVAL);
        Assert.assertTrue(handlesWorkerInterval);

        try {
            triggerChangeListener.propertyValueChanged("default", PROPERTY_PREFIX + SEND_WORKER_INTERVAL, "wrong-value");
            Assert.fail("Expected exception not raised");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid"));
        }

        triggerChangeListener.propertyValueChanged("default", PROPERTY_PREFIX + SEND_WORKER_INTERVAL, "3000");
        triggerChangeListener.propertyValueChanged("default", PROPERTY_PREFIX + SENT_PURGE_WORKER_CRONEXPRESSION, "0 0/15 * * * ?");

        Mockito.verify(domibusSchedulerExt, Mockito.times(1)).rescheduleJob("default", "fsPluginSendMessagesWorkerJob", 3000);
        Mockito.verify(domibusSchedulerExt, Mockito.times(1)).rescheduleJob("default", "fsPluginPurgeSentWorkerJob", "0 0/15 * * * ?");
    }

    @Test
    public void testOutQueueConcurrencyChangeListener() throws Exception {
        boolean handlesProperty = outQueueConcurrencyChangeListener.handlesProperty(OUT_QUEUE_CONCURRENCY);
        Assert.assertTrue(handlesProperty);

        outQueueConcurrencyChangeListener.propertyValueChanged("default", OUT_QUEUE_CONCURRENCY, "1-2");
        Mockito.verify(messageListenerContainer, Mockito.times(1)).updateMessageListenerContainerConcurrency(null, "1-2");
    }
}