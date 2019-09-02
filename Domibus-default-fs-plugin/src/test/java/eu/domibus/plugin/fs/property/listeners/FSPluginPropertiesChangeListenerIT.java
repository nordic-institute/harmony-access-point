package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.ext.services.*;
import eu.domibus.messaging.PluginMessageListenerContainer;
import eu.domibus.plugin.fs.property.FSPluginProperties;
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
import java.util.Properties;

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
        public DomibusSchedulerExtService domibusSchedulerExtService() {
            return Mockito.mock(DomibusSchedulerExtService.class);
        }

        @Bean
        public DomibusConfigurationExtService domibusConfigurationExtService() {
            return Mockito.mock(DomibusConfigurationExtService.class);
        }

        @Bean
        public PluginMessageListenerContainer pluginMessageListenerContainer() {
            return Mockito.mock(PluginMessageListenerContainer.class);
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
        public PluginMessageListenerConcurrencyChangeListener pluginMessageListenerConcurrencyChangeListener() {
            return new PluginMessageListenerConcurrencyChangeListener();
        }

        @Bean
        public DomainPropertiesChangeListener domainPropertiesChangeListener() {
            return new DomainPropertiesChangeListener();
        }
    }

    @Autowired
    private FSPluginProperties fSPluginProperties;

    @Autowired
    private DomibusConfigurationExtService domibusConfigurationExtService;

    @Autowired
    private DomainPropertiesChangeListener domainPropertiesChangeListener;

    @Autowired
    private PluginMessageListenerConcurrencyChangeListener pluginMessageListenerConcurrencyChangeListener;

    @Autowired
    private TriggerChangeListener triggerChangeListener;

    @Test
    public void testDomainPropertiesChangeListener() throws Exception {
        boolean handlesOrder = domainPropertiesChangeListener.handlesProperty("order");
        Assert.assertEquals(true, handlesOrder);
        boolean handlesPattern = domainPropertiesChangeListener.handlesProperty("messages.expression");
        Assert.assertEquals(true, handlesPattern);
        boolean notHandled = domainPropertiesChangeListener.handlesProperty("messages.expression.not.handled");
        Assert.assertEquals(false, notHandled);

        domainPropertiesChangeListener.propertyValueChanged("default", "order", "10");
        domainPropertiesChangeListener.propertyValueChanged("default", "messages.expression", "bdx:noprocess#TC1Leg1");

    }
}