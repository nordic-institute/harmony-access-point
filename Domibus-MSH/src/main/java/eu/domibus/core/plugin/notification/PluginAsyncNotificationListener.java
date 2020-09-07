
package eu.domibus.core.plugin.notification;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.NotificationType;
import eu.domibus.core.ebms3.sender.MessageSenderListener;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * JMS listener responsible for sending async notifications to plugins
 *
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
public class PluginAsyncNotificationListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginAsyncNotificationListener.class);

    protected AuthUtils authUtils;
    protected DomainContextProvider domainContextProvider;
    protected AsyncNotificationConfiguration asyncNotificationConfiguration;
    protected PluginEventNotifierProvider pluginEventNotifierProvider;

    public PluginAsyncNotificationListener(DomainContextProvider domainContextProvider,
                                           AsyncNotificationConfiguration asyncNotificationConfiguration,
                                           PluginEventNotifierProvider pluginEventNotifierProvider,
                                           AuthUtils authUtils) {
        this.domainContextProvider = domainContextProvider;
        this.asyncNotificationConfiguration = asyncNotificationConfiguration;
        this.pluginEventNotifierProvider = pluginEventNotifierProvider;
        this.authUtils = authUtils;
    }

    @MDCKey({DomibusLogger.MDC_MESSAGE_ID})
    @Transactional
    @Timer(clazz = PluginAsyncNotificationListener.class,value="onMessage")
    @Counter(clazz = PluginAsyncNotificationListener.class,value="onMessage")
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

            LOG.info("Received message with messageId [{}] and notification type [{}]", messageId, notificationType);

            PluginEventNotifier pluginEventNotifier = pluginEventNotifierProvider.getPluginEventNotifier(notificationType);
            if (pluginEventNotifier == null) {
                LOG.warn("Could not get plugin event notifier for notification type [{}]", notificationType);
                return;
            }
            Map<String, String> messageProperties = getMessageProperties(message);
            pluginEventNotifier.notifyPlugin(asyncNotificationConfiguration.getBackendConnector(), messageId, messageProperties);
        } catch (JMSException jmsEx) {
            LOG.error("Error getting the property from JMS message", jmsEx);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Error getting the property from JMS message", jmsEx.getCause());
        } catch (Exception ex) { //NOSONAR To catch every exceptions thrown by all plugins.
            LOG.error("Error occurred during the plugin notification process of the message", ex);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Error occurred during the plugin notification process of the message", ex.getCause());
        }
    }

    protected Map<String, String> getMessageProperties(Message message) throws JMSException {
        Map<String, String> properties = new HashMap<>();
        Enumeration propertyNames = message.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            properties.put(propertyName, message.getStringProperty(propertyName));
        }
        return properties;
    }


}
