package eu.domibus.common;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.*;
import eu.domibus.core.message.MessagePropertyDao;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.MshRoleDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Component
public class MessageDaoTestUtil {

    @Autowired
    MessagePropertyDao propertyDao;

    @Autowired
    SignalMessageDao signalMessageDao;

    @Autowired
    MshRoleDao mshRoleDao;

    @Autowired
    MessageStatusDao messageStatusDao;

    @Autowired
    SignalMessageLogDao signalMessageLogDao;

    public void createSignalMessageLog(String msgId, Date received) {
        createSignalMessageLog(msgId, received, MSHRole.RECEIVING, MessageStatus.RECEIVED);
    }

    public void createSignalMessageLog(String msgId, Date received, MSHRole mshRole, MessageStatus messageStatus) {
        UserMessage userMessage = new UserMessage();
        userMessage.setMessageId(msgId);
        userMessage.setConversationId("conversation-" + msgId);

        MessageProperty messageProperty1 = propertyDao.findOrCreateProperty("originalSender", "originalSender1", "");
        MessageProperty messageProperty2 = propertyDao.findOrCreateProperty("finalRecipient", "finalRecipient2", "");
        userMessage.setMessageProperties(new HashSet<>(Arrays.asList(messageProperty1, messageProperty2)));

        SignalMessage signal = new SignalMessage();
        signal.setUserMessage(userMessage);
        signal.setSignalMessageId("signal-" + msgId);
        signalMessageDao.create(signal);

        SignalMessageLog signalMessageLog = new SignalMessageLog();
        signalMessageLog.setReceived(received);
        signalMessageLog.setMshRole(mshRoleDao.findOrCreate(mshRole));
        signalMessageLog.setMessageStatus(messageStatusDao.findOrCreate(messageStatus));

        signalMessageLog.setSignalMessage(signal);
        signalMessageLogDao.create(signalMessageLog);
    }
}
