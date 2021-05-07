package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.core.message.signal.SignalMessageLogBuilder;
import mockit.integration.junit4.JMockit;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author Federico Martini
 * @since 3.2
 */
@RunWith(JMockit.class)
public class MessageLogBuilderTest {

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testSignalMessageLogResolver() {

        String messageId = "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu";

        // Builds the signal message log
        MessageStatusEntity messageStatus = new MessageStatusEntity();
        messageStatus.setMessageStatus(MessageStatus.ACKNOWLEDGED);
        MSHRoleEntity mshRole = new MSHRoleEntity();
        mshRole.setRole(MSHRole.RECEIVING);
        SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
//                .setMessageId(messageId)
                .setMessageStatus(messageStatus)
                .setMshRole(mshRole)
//                .setNotificationStatus(NotificationStatus.NOT_REQUIRED)
                ;

        SignalMessageLog signalMessageLog = smlBuilder.build();

//        assertEquals(MessageType.SIGNAL_MESSAGE, signalMessageLog.getMessageType());
//        assertEquals(messageId, signalMessageLog.getMessageId());
        assertEquals(MessageStatus.ACKNOWLEDGED, signalMessageLog.getMessageStatus().getMessageStatus());
        assertEquals(MSHRole.RECEIVING, signalMessageLog.getMshRole().getRole());
//        assertEquals(NotificationStatus.NOT_REQUIRED, signalMessageLog.getNotificationStatus());
//        assertNull(signalMessageLog.getNextAttempt());
//        assertEquals(0, signalMessageLog.getSendAttempts());

    }
}
