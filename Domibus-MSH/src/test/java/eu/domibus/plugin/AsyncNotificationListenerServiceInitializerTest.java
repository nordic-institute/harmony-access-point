package eu.domibus.plugin;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthUtils;
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
public class AsyncNotificationListenerServiceInitializerTest {

    @Tested
    AsyncNotificationListenerServiceInitializer asyncNotificationListenerServiceInitializer;

    @Injectable
    ObjectProvider<AsyncNotificationListenerService> asyncNotificationListenerProvider;

    @Injectable
    protected JmsListenerContainerFactory internalJmsListenerContainerFactory;

    @Injectable
    protected AuthUtils authUtils;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected PluginEventNotifierProvider pluginEventNotifierProvider;

    @Injectable
    List<NotificationListenerService> notificationListenerServices;

    @Test
    public void configureJmsListeners(@Injectable JmsListenerEndpointRegistrar registrar,
                                      @Injectable Queue queue,
                                      @Injectable NotificationListenerService notificationListenerService1) throws JMSException {
        String backendName = "mybackend";
        String queueName = "myQueue";

        List<NotificationListenerService> notificationListenerServices = new ArrayList<>();
        notificationListenerServices.add(notificationListenerService1);
        asyncNotificationListenerServiceInitializer.notificationListenerServices = notificationListenerServices;

        new Expectations(asyncNotificationListenerServiceInitializer) {{
            asyncNotificationListenerServiceInitializer.initializeAsyncNotificationListerService(registrar, (NotificationListenerService) any);
        }};

        asyncNotificationListenerServiceInitializer.configureJmsListeners(registrar);

        new Verifications() {{
            asyncNotificationListenerServiceInitializer.initializeAsyncNotificationListerService(registrar, notificationListenerService1);
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