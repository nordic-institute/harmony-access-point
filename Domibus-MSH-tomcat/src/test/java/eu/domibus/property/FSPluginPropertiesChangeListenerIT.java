package eu.domibus.property;

import eu.domibus.AbstractIT;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.core.scheduler.DomibusQuartzStarter;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.DomibusSchedulerExtService;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.fs.property.listeners.DomainPropertiesChangeListener;
import eu.domibus.plugin.fs.property.listeners.OutQueueConcurrencyChangeListener;
import eu.domibus.plugin.fs.property.listeners.TriggerChangeListener;
import eu.domibus.plugin.fs.queue.FSSendMessageListenerContainer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.*;

/**
 * @author Ion Perpegel
 * @author Catalin Enache
 * @Since 4.2
 */
public class FSPluginPropertiesChangeListenerIT extends AbstractIT {

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

    @Autowired
    private DomibusScheduler domibusScheduler;

    @Autowired
    private DomibusQuartzStarter domibusQuartzStarter;

    @Configuration
    static class ContextConfiguration {

        @Primary
        @Bean
        public DomibusSchedulerExtService domibusSchedulerExt() {
            return Mockito.mock(DomibusSchedulerExtService.class);
        }

//        @Primary
//        @Bean
//        public FSSendMessageListenerContainer messageListenerContainer() {
//            return Mockito.mock(FSSendMessageListenerContainer.class);
//        }

    }


//    @Test
//    public void testDomainPropertiesChangeListener() {
//        boolean handlesOrder = domainPropertiesChangeListener.handlesProperty("fsplugin.order");
//        Assert.assertTrue(handlesOrder);
//        boolean handlesPattern = domainPropertiesChangeListener.handlesProperty("fsplugin.messages.expression");
//        Assert.assertTrue(handlesPattern);
//        boolean notHandled = domainPropertiesChangeListener.handlesProperty("fsplugin.messages.expression.not.handled");
//        Assert.assertFalse(notHandled);
//
//        final List<String> oldDomains = fSPluginProperties.getDomainsOrdered();
//
//        domainPropertiesChangeListener.propertyValueChanged("default", "fsplugin.order", "10");
//        domainPropertiesChangeListener.propertyValueChanged("default", "fsplugin.messages.expression", "bdx:noprocess#TC1Leg1");
//
//        final List<String> newDomains = fSPluginProperties.getDomainsOrdered();
//        Assert.assertNotSame(oldDomains.get(1), newDomains.get(1));
//
//    }

    @Test
    public void testTriggerChangeListener() throws Exception {
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
    @Ignore
    public void testOutQueueConcurrencyChangeListener() {

        boolean handlesProperty = outQueueConcurrencyChangeListener.handlesProperty(OUT_QUEUE_CONCURRENCY);
        Assert.assertTrue(handlesProperty);

        outQueueConcurrencyChangeListener.propertyValueChanged("default", OUT_QUEUE_CONCURRENCY, "1-2");
        Mockito.verify(messageListenerContainer, Mockito.times(1)).updateMessageListenerContainerConcurrency(null, "1-2");
    }
}