package eu.domibus.core.plugin.notification;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.functions.ApplicationAuthenticatedProcedure;
import eu.domibus.common.NotificationType;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
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
public class PluginAsyncNotificationListenerTest {

    @Tested
    PluginAsyncNotificationListener pluginAsyncNotificationListener;

    @Injectable
    protected JmsListenerContainerFactory internalJmsListenerContainerFactory;

    @Injectable
    protected AuthUtils authUtils;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected AsyncNotificationConfiguration notificationListenerService;

    @Injectable
    protected PluginEventNotifierProvider pluginEventNotifierProvider;


    @Test
    public void onMessage(@Injectable Message message,
                          @Injectable PluginEventNotifier pluginEventNotifier,
                          @Injectable Map<String, String> messageProperties) throws JMSException {
        String messageId = "123";
        NotificationType notificationType = NotificationType.MESSAGE_FRAGMENT_RECEIVED;

        new Expectations(pluginAsyncNotificationListener) {{
            message.getStringProperty(MessageConstants.MESSAGE_ID);
            result = messageId;

            message.getStringProperty(MessageConstants.NOTIFICATION_TYPE);
            result = notificationType.toString();

            pluginEventNotifierProvider.getPluginEventNotifier(notificationType);
            result = pluginEventNotifier;

            pluginAsyncNotificationListener.getMessageProperties(message);
            result = messageProperties;
        }};

        pluginAsyncNotificationListener.doOnMessage(message);

        new Verifications() {{
            pluginEventNotifier.notifyPlugin(notificationListenerService.getBackendConnector(), messageId, messageProperties);
            times = 1;
        }};
    }

    @Test
    public void onMessage_addsAuthentication(@Injectable Message message,
                                             @Injectable PluginEventNotifier pluginEventNotifier,
                                             @Injectable Map<String, String> messageProperties)  throws JMSException {
        // Given
        new Expectations() {{
            authUtils.runMethodWithSecurityContext((ApplicationAuthenticatedProcedure)any, anyString, anyString, (AuthRole)any);
        }};

        // When
        pluginAsyncNotificationListener.onMessage(message);

        // Then
        new FullVerifications() {{
            ApplicationAuthenticatedProcedure function;
            String username;
            String password;
            AuthRole role;
            authUtils.runMethodWithSecurityContext(function = withCapture(),
                    username=withCapture(), password=withCapture(), role=withCapture());
            Assert.assertNotNull(function);
            Assert.assertEquals("notif",username);
            Assert.assertEquals("notif",password);
            Assert.assertEquals(AuthRole.ROLE_ADMIN,role);

        }};
    }
}
