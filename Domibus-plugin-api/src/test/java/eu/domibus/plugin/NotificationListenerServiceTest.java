package eu.domibus.plugin;

import eu.domibus.common.NotificationType;
import eu.domibus.ext.services.JMSExtService;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

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


    @Test
    public void testConstructor(@Injectable Queue queue, @Injectable final BackendConnector.Mode mode) {
        NotificationListenerService nls;
        List<NotificationType> rn = new ArrayList<>();
        rn.add(NotificationType.MESSAGE_RECEIVED);
        rn.add(NotificationType.MESSAGE_SEND_FAILURE);
        nls = new NotificationListenerService(null, BackendConnector.Mode.PUSH, rn);
        Assert.assertTrue(nls.getRequiredNotificationTypeList().size() == 2);
        Assert.assertTrue(nls.getRequiredNotificationTypeList().contains(NotificationType.MESSAGE_RECEIVED));
        Assert.assertTrue(nls.getRequiredNotificationTypeList().contains(NotificationType.MESSAGE_SEND_FAILURE));
        Assert.assertTrue(!nls.getRequiredNotificationTypeList().contains(NotificationType.MESSAGE_SEND_SUCCESS));
    }
}