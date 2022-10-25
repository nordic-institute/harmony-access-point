package eu.domibus.plugin.fs.property;

import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.ext.domain.DomainDTO;
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
public class EnableChangeListenerTestIT extends AbstractIT {

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
