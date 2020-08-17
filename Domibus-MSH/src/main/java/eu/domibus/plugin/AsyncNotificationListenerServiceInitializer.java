package eu.domibus.plugin;

import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.notification.AsyncNotificationListener;
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

    protected List<AsyncNotificationListener> asyncNotificationListeners;
    protected JmsListenerContainerFactory jmsListenerContainerFactory;
    protected ObjectProvider<AsyncNotificationListenerService> asyncNotificationListenerProvider;


    public AsyncNotificationListenerServiceInitializer(@Qualifier("internalJmsListenerContainerFactory") JmsListenerContainerFactory jmsListenerContainerFactory,
                                                       ObjectProvider<AsyncNotificationListenerService> asyncNotificationListenerProvider,
                                                       @Autowired(required = false) List<AsyncNotificationListener> asyncNotificationListeners) {
        this.jmsListenerContainerFactory = jmsListenerContainerFactory;
        this.asyncNotificationListeners = asyncNotificationListeners;
        this.asyncNotificationListenerProvider = asyncNotificationListenerProvider;
    }

    @Override
    public void configureJmsListeners(final JmsListenerEndpointRegistrar registrar) {
        LOG.info("Initializing services of type AsyncNotificationListenerService");

        for (AsyncNotificationListener notificationListenerService : asyncNotificationListeners) {
            initializeAsyncNotificationListerService(registrar, notificationListenerService);
        }
    }

    protected void initializeAsyncNotificationListerService(JmsListenerEndpointRegistrar registrar,
                                                            AsyncNotificationListener asyncNotificationListener) {
        BackendConnector backendConnector = asyncNotificationListener.getBackendConnector();
        if(backendConnector == null) {
            LOG.error("No connector configured for async notification listener");
            return;
        }

        if (backendConnector.getMode() == BackendConnector.Mode.PULL) {
            LOG.info("No async notification listener is created for plugin [{}]: plugin type is PULL", backendConnector.getName());
            return;
        }
        if (asyncNotificationListener.getBackendNotificationQueue() == null) {
            LOG.info("No notification queue configured for plugin [{}]. No async notification listener is created", backendConnector.getName());
            return;
        }

        SimpleJmsListenerEndpoint endpoint = createJMSListener(asyncNotificationListener);
        registrar.registerEndpoint(endpoint, jmsListenerContainerFactory);
        LOG.info("Instantiated AsyncNotificationListenerService for backend [{}]", backendConnector.getName());
    }

    protected SimpleJmsListenerEndpoint createJMSListener(AsyncNotificationListener asyncNotificationListener) {
        BackendConnector backendConnector = asyncNotificationListener.getBackendConnector();
        LOG.debug("Configuring JmsListener for plugin [{}] for mode [{}]", backendConnector.getName(), backendConnector.getMode());

        final SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setId(backendConnector.getName());
        final Queue pushQueue = asyncNotificationListener.getBackendNotificationQueue();
        if (pushQueue == null) {
            throw new ConfigurationException("No notification queue found for " + backendConnector.getName());
        }
        try {
            endpoint.setDestination(asyncNotificationListener.getQueueName());
        } catch (final JMSException e) {
            LOG.error("Problem with predefined queue.", e);
        }
        AsyncNotificationListenerService asyncNotificationListenerService = asyncNotificationListenerProvider.getObject(asyncNotificationListener);
        endpoint.setMessageListener(asyncNotificationListenerService);

        return endpoint;
    }
}
