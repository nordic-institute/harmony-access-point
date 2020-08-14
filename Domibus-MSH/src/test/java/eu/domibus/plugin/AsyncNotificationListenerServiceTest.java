package eu.domibus.plugin;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.NotificationType;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Arun Venugopal
 * @author Federico Martini
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class AsyncNotificationListenerServiceTest {

    @Tested
    AsyncNotificationListenerService objNotificationListenerService;

    @Injectable
    protected JmsListenerContainerFactory internalJmsListenerContainerFactory;

    @Injectable
    protected AuthUtils authUtils;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected NotificationListenerService notificationListenerService;

    @Injectable
    protected PluginEventNotifierProvider pluginEventNotifierProvider;


    @Test
    public void onMessage(@Injectable Message message,
                          @Injectable PluginEventNotifier pluginEventNotifier,
                          @Injectable Map<String, Object> messageProperties) throws JMSException {
        String messageId = "123";
        NotificationType notificationType = NotificationType.MESSAGE_FRAGMENT_RECEIVED;

        new Expectations(objNotificationListenerService) {{
            message.getStringProperty(MessageConstants.MESSAGE_ID);
            result = messageId;

            message.getStringProperty(MessageConstants.NOTIFICATION_TYPE);
            result = notificationType.toString();

            pluginEventNotifierProvider.getPluginEventNotifier(notificationType);
            result = pluginEventNotifier;

            objNotificationListenerService.getMessageProperties(message);
            result = messageProperties;
        }};

        objNotificationListenerService.onMessage(message);

        new Verifications() {{
            pluginEventNotifier.notifyPlugin(notificationListenerService.getBackendConnector(), messageId, messageProperties);
        }};
    }


}
