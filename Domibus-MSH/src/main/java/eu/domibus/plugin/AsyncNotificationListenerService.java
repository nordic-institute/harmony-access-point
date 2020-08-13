
package eu.domibus.plugin;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.NotificationType;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
public class AsyncNotificationListenerService implements MessageListener, JmsListenerConfigurer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AsyncNotificationListenerService.class);

    @Qualifier("internalJmsListenerContainerFactory")
    protected JmsListenerContainerFactory jmsListenerContainerFactory;
    protected AuthUtils authUtils;
    protected DomainContextProvider domainContextProvider;
    protected NotificationListenerService notificationListenerService;
    protected PluginEventNotifierProvider pluginEventNotifierProvider;

    public AsyncNotificationListenerService(JmsListenerContainerFactory jmsListenerContainerFactory,
                                            AuthUtils authUtils,
                                            DomainContextProvider domainContextProvider,
                                            NotificationListenerService notificationListenerService,
                                            PluginEventNotifierProvider pluginEventNotifierProvider) {
        this.jmsListenerContainerFactory = jmsListenerContainerFactory;
        this.authUtils = authUtils;
        this.domainContextProvider = domainContextProvider;
        this.notificationListenerService = notificationListenerService;
        this.pluginEventNotifierProvider = pluginEventNotifierProvider;
    }

    @MDCKey({DomibusLogger.MDC_MESSAGE_ID})
    @Transactional
    public void onMessage(final Message message) {
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("notif", "notif", AuthRole.ROLE_ADMIN);
        }

        try {
            final String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);

            final String domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            LOG.debug("Processing message ID [{}] for domain [{}]", messageId, domainCode);
            domainContextProvider.setCurrentDomain(domainCode);

            final NotificationType notificationType = NotificationType.valueOf(message.getStringProperty(MessageConstants.NOTIFICATION_TYPE));

            LOG.info("Received message with messageId [" + messageId + "] and notification type [" + notificationType + "]");

            PluginEventNotifier pluginEventNotifier = pluginEventNotifierProvider.getPluginEventNotifier(notificationType);
            if (pluginEventNotifier == null) {
                LOG.warn("Could not get plugin event notifier for notification type [{}]", notificationType);
                return;
            }
            Map<String, Object> messageProperties = getMessageProperties(message);
            pluginEventNotifier.notifyPlugin(notificationListenerService.getBackendConnector(), messageId, messageProperties);
        } catch (JMSException jmsEx) {
            LOG.error("Error getting the property from JMS message", jmsEx);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Error getting the property from JMS message", jmsEx.getCause());
        } catch (Exception ex) { //NOSONAR To catch every exceptions thrown by all plugins.
            LOG.error("Error occurred during the plugin notification process of the message", ex);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Error occurred during the plugin notification process of the message", ex.getCause());
        }
    }

    protected Map<String, Object> getMessageProperties(Message message) throws JMSException {
        Map<String, Object> properties = new HashMap<>();
        Enumeration propertyNames = message.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            properties.put(propertyName, message.getObjectProperty(propertyName));
        }
        return properties;
    }

    @Override
    public void configureJmsListeners(final JmsListenerEndpointRegistrar registrar) {
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

        endpoint.setMessageListener(this);
        registrar.registerEndpoint(endpoint, jmsListenerContainerFactory);
    }


    protected String getQueueName(Queue queue) throws JMSException {
        return queue.getQueueName();
    }
}
