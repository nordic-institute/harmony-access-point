package eu.domibus.plugin;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.NotificationType;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.notification.AsyncNotificationListener;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Map;

/**
 * @author Arun Venugopal
 * @author Federico Martini
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class AsyncNotificationListenerServiceTest {

    @Tested
    AsyncNotificationListenerService asyncNotificationListenerService;

    @Injectable
    protected JmsListenerContainerFactory internalJmsListenerContainerFactory;

    @Injectable
    protected AuthUtils authUtils;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected AsyncNotificationListener notificationListenerService;

    @Injectable
    protected PluginEventNotifierProvider pluginEventNotifierProvider;


    @Test
    public void onMessage(@Injectable Message message,
                          @Injectable PluginEventNotifier pluginEventNotifier,
                          @Injectable Map<String, Object> messageProperties) throws JMSException {
        String messageId = "123";
        NotificationType notificationType = NotificationType.MESSAGE_FRAGMENT_RECEIVED;

        new Expectations(asyncNotificationListenerService) {{
            message.getStringProperty(MessageConstants.MESSAGE_ID);
            result = messageId;

            message.getStringProperty(MessageConstants.NOTIFICATION_TYPE);
            result = notificationType.toString();

            pluginEventNotifierProvider.getPluginEventNotifier(notificationType);
            result = pluginEventNotifier;

            asyncNotificationListenerService.getMessageProperties(message);
            result = messageProperties;
        }};

        asyncNotificationListenerService.onMessage(message);

        new Verifications() {{
            pluginEventNotifier.notifyPlugin(notificationListenerService.getBackendConnector(), messageId, messageProperties);
        }};
    }


}
