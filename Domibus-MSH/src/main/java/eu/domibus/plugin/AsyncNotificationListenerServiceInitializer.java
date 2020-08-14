package eu.domibus.plugin;

import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class AsyncNotificationListenerServiceInitializer implements JmsListenerConfigurer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AsyncNotificationListenerServiceInitializer.class);

    protected List<NotificationListenerService> notificationListenerServices;
    protected JmsListenerContainerFactory jmsListenerContainerFactory;
    protected ObjectProvider<AsyncNotificationListenerService> asyncNotificationListenerProvider;


    public AsyncNotificationListenerServiceInitializer(@Qualifier("internalJmsListenerContainerFactory") JmsListenerContainerFactory jmsListenerContainerFactory,
                                                       ObjectProvider<AsyncNotificationListenerService> asyncNotificationListenerProvider,
                                                       @Autowired(required = false) List<NotificationListenerService> notificationListenerServices) {
        this.jmsListenerContainerFactory = jmsListenerContainerFactory;
        this.notificationListenerServices = notificationListenerServices;
        this.asyncNotificationListenerProvider = asyncNotificationListenerProvider;
    }

    @Override
    public void configureJmsListeners(final JmsListenerEndpointRegistrar registrar) {
        LOG.info("Initializing services of type AsyncNotificationListenerService");

        for (NotificationListenerService notificationListenerService : notificationListenerServices) {
            initializeAsyncNotificationListerService(registrar, notificationListenerService);
        }
    }

    protected void initializeAsyncNotificationListerService(JmsListenerEndpointRegistrar registrar,
                                                            NotificationListenerService notificationListenerService) {
        if (notificationListenerService.getMode() == BackendConnector.Mode.PULL) {
            LOG.info("No async notification listener is created for plugin [{}]: plugin type is PULL", notificationListenerService.getBackendName());
            return;
        }
        if (notificationListenerService.getBackendNotificationQueue() == null) {
            LOG.info("No notification queue configured for plugin [{}]. No async notification listener is created", notificationListenerService.getBackendName());
            return;
        }

        SimpleJmsListenerEndpoint endpoint = createJMSListener(notificationListenerService);
        registrar.registerEndpoint(endpoint, jmsListenerContainerFactory);
        LOG.info("Instantiated AsyncNotificationListenerService for backend [{}]", notificationListenerService.getBackendName());


    }

    protected SimpleJmsListenerEndpoint createJMSListener(NotificationListenerService notificationListenerService) {
        LOG.debug("Configuring JmsListener for plugin [{}] for mode [{}]", notificationListenerService.getBackendName(), notificationListenerService.getMode());

        final SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setId(notificationListenerService.getBackendName());
        final Queue pushQueue = notificationListenerService.getBackendNotificationQueue();
        if (pushQueue == null) {
            throw new ConfigurationException("No notification queue found for " + notificationListenerService.getBackendName());
        }
        try {
            endpoint.setDestination(getQueueName(pushQueue));
        } catch (final JMSException e) {
            LOG.error("Problem with predefined queue.", e);
        }
        AsyncNotificationListenerService asyncNotificationListenerService = asyncNotificationListenerProvider.getObject(notificationListenerService);
        endpoint.setMessageListener(asyncNotificationListenerService);

        return endpoint;
    }

    protected String getQueueName(Queue queue) throws JMSException {
        return queue.getQueueName();
    }
}
