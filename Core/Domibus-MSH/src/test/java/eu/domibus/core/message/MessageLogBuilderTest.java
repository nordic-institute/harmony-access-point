package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.core.message.signal.SignalMessageLogBuilder;
import mockit.integration.junit4.JMockit;
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
    public void testSignalMessageLogResolver() {

        String messageId = "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu";

        // Builds the signal message log
        MessageStatusEntity messageStatus = new MessageStatusEntity();
        messageStatus.setMessageStatus(MessageStatus.ACKNOWLEDGED);
        MSHRoleEntity mshRole = new MSHRoleEntity();
        mshRole.setRole(MSHRole.RECEIVING);
        SignalMessage signalMessage = new SignalMessage();
        signalMessage.setSignalMessageId(messageId);
        SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
                .setSignalMessage(signalMessage)
                .setMessageStatus(messageStatus)
                .setMshRole(mshRole);

        SignalMessageLog signalMessageLog = smlBuilder.build();

        assertEquals(messageId, signalMessageLog.getSignalMessage().getSignalMessageId());
        assertEquals(MessageStatus.ACKNOWLEDGED, signalMessageLog.getMessageStatus().getMessageStatus());
        assertEquals(MSHRole.RECEIVING, signalMessageLog.getMshRole().getRole());
    }
}
