package eu.domibus.core.plugin.notification;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.plugin.notification.PluginAsyncNotificationJMSConfigurer;
import eu.domibus.core.plugin.notification.PluginAsyncNotificationListener;
import eu.domibus.core.plugin.notification.PluginEventNotifierProvider;
import eu.domibus.plugin.NotificationListenerService;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since
 */
@RunWith(JMockit.class)
public class PluginAsyncNotificationJMSConfigurerTest {

    @Tested
    PluginAsyncNotificationJMSConfigurer pluginAsyncNotificationJMSConfigurer;

    @Injectable
    ObjectProvider<PluginAsyncNotificationListener> asyncNotificationListenerProvider;

    @Injectable
    protected JmsListenerContainerFactory internalJmsListenerContainerFactory;

    @Injectable
    protected AuthUtils authUtils;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected PluginEventNotifierProvider pluginEventNotifierProvider;

    @Injectable
    List<AsyncNotificationConfiguration> notificationListenerServices;

    @Test
    public void configureJmsListeners(@Injectable JmsListenerEndpointRegistrar registrar,
                                      @Injectable Queue queue,
                                      @Injectable AsyncNotificationConfiguration notificationListenerService1) throws JMSException {
        String backendName = "mybackend";
        String queueName = "myQueue";

        List<AsyncNotificationConfiguration> notificationListenerServices = new ArrayList<>();
        notificationListenerServices.add(notificationListenerService1);
        pluginAsyncNotificationJMSConfigurer.asyncNotificationConfigurations = notificationListenerServices;

        new Expectations(pluginAsyncNotificationJMSConfigurer) {{
            pluginAsyncNotificationJMSConfigurer.initializeAsyncNotificationLister(registrar, (NotificationListenerService) any);
        }};

        pluginAsyncNotificationJMSConfigurer.configureJmsListeners(registrar);

        new Verifications() {{
            pluginAsyncNotificationJMSConfigurer.initializeAsyncNotificationLister(registrar, notificationListenerService1);
        }};
    }

    /*@Test
    public void configureJmsListeners(@Injectable JmsListenerEndpointRegistrar registrar,
                                      @Injectable Queue queue) throws JMSException {
        String backendName = "mybackend";
        String queueName = "myQueue";

        new Expectations() {{
            notificationListenerService.getBackendName();
            result = backendName;

            notificationListenerService.getBackendNotificationQueue();
            result = queue;

            queue.getQueueName();
            result = queueName;
        }};

        asyncNotificationListenerServiceInitializer.configureJmsListeners(registrar);

        new Verifications() {{
            SimpleJmsListenerEndpoint endpoint = null;
            registrar.registerEndpoint(endpoint = withCapture(), internalJmsListenerContainerFactory);

            assertEquals(backendName, endpoint.getId());
            assertTrue(asyncNotificationListenerServiceInitializer == endpoint.getMessageListener());
        }};
    }*/
}