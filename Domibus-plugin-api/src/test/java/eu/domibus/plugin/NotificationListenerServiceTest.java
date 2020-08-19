package eu.domibus.plugin;

import eu.domibus.common.NotificationType;
import eu.domibus.ext.services.JMSExtService;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;

import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class NotificationListenerServiceTest {

    @Tested
    NotificationListenerService notificationListenerService;

    @Injectable
    protected JMSExtService jmsExtService;

    @Injectable
    protected BackendConnector backendConnector;

    @Injectable
    ObjectProvider<QueueMessageLister> queueMessageListerObjectProvider;

    @Test
    public void testConstructor(@Injectable Queue queue, @Injectable final BackendConnector.Mode mode) {
        List<NotificationType> notificationTypes = new ArrayList<>();
        notificationTypes.add(NotificationType.MESSAGE_RECEIVED);
        notificationTypes.add(NotificationType.MESSAGE_SEND_FAILURE);

        NotificationListenerService nls = new NotificationListenerService(null, BackendConnector.Mode.PUSH, notificationTypes);
        Assert.assertTrue(nls.getRequiredNotificationTypeList().size() == 2);
        Assert.assertTrue(nls.getRequiredNotificationTypeList().contains(NotificationType.MESSAGE_RECEIVED));
        Assert.assertTrue(nls.getRequiredNotificationTypeList().contains(NotificationType.MESSAGE_SEND_FAILURE));
        Assert.assertTrue(!nls.getRequiredNotificationTypeList().contains(NotificationType.MESSAGE_SEND_SUCCESS));
    }
}