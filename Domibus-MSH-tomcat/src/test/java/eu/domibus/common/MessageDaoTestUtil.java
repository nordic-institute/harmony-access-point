package eu.domibus.common;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.*;
import eu.domibus.core.message.*;
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
    UserMessageLogDao userMessageLogDao;

    @Autowired
    SignalMessageLogDao signalMessageLogDao;

    @Autowired
    UserMessageDao userMessageDao;

    @Autowired
    SignalMessageDao signalMessageDao;

    @Autowired
    MessagePropertyDao propertyDao;

    @Autowired
    ActionDao actionDao;

    @Autowired
    ServiceDao serviceDao;

    @Autowired
    MshRoleDao mshRoleDao;

    @Autowired
    MessageStatusDao messageStatusDao;

    @Autowired
    NotificationStatusDao notificationStatusDao;

    @Autowired
    PartyRoleDao partyRoleDao;

    @Autowired
    PartyIdDao partyIdDao;


    final static String PARTY_ID_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    final static String INITIATOR_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";
    final static String RESPONDER_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";


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

    public UserMessageLog createUserMessageLog(String msgId, Date received, MSHRole mshRole, MessageStatus messageStatus, boolean isTestMessage) {
        UserMessage userMessage = new UserMessage();
        userMessage.setMessageId(msgId);
        userMessage.setConversationId("conversation-" + msgId);

        MessageProperty messageProperty1 = propertyDao.findOrCreateProperty("originalSender", "originalSender1", "");
        MessageProperty messageProperty2 = propertyDao.findOrCreateProperty("finalRecipient", "finalRecipient2", "");
        userMessage.setMessageProperties(new HashSet<>(Arrays.asList(messageProperty1, messageProperty2)));

        PartyInfo partyInfo = new PartyInfo();
        partyInfo.setFrom(createFrom(INITIATOR_ROLE, "domibus-blue"));
        partyInfo.setTo(createTo(RESPONDER_ROLE, "domibus-red"));
        userMessage.setPartyInfo(partyInfo);

        final String serviceValue = isTestMessage ? "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/service": "bdx:noprocess";
        final String serviceType = isTestMessage ? null : "tc1";
        final String actionValue = isTestMessage ? "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/test" : "TC1Leg1";
        userMessage.setService(serviceDao.findOrCreateService(serviceValue, serviceType));
        userMessage.setAction(actionDao.findOrCreateAction(actionValue));

        userMessage.setTestMessage(isTestMessage);

        userMessageDao.create(userMessage);

        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setReceived(received);
        userMessageLog.setMshRole(mshRoleDao.findOrCreate(mshRole));
        userMessageLog.setMessageStatus(messageStatusDao.findOrCreate(messageStatus));
        userMessageLog.setNotificationStatus(notificationStatusDao.findOrCreate(NotificationStatus.NOTIFIED));

        userMessageLog.setUserMessage(userMessage);
        userMessageLogDao.create(userMessageLog);

        return userMessageLog;
    }

    public UserMessageLog createUserMessageLog(String msgId, Date received, MSHRole mshRole, MessageStatus messageStatus) {
        return createUserMessageLog(msgId, received, mshRole, messageStatus, false);
    }

    public UserMessageLog createUserMessageLog(String msgId, Date received) {
        return createUserMessageLog(msgId, received, MSHRole.RECEIVING, MessageStatus.RECEIVED, false);
    }

    public UserMessageLog createTestMessage(String msgId) {
        UserMessageLog userMessageLog = createUserMessageLog(msgId, new Date(), MSHRole.SENDING, MessageStatus.ACKNOWLEDGED, true);

        SignalMessage signal = new SignalMessage();
        signal.setUserMessage(userMessageLog.getUserMessage());
        signal.setSignalMessageId("signal-" + msgId);
        signalMessageDao.create(signal);

        SignalMessageLog signalMessageLog = new SignalMessageLog();
        signalMessageLog.setReceived(new Date());
        signalMessageLog.setMshRole(mshRoleDao.findOrCreate(MSHRole.RECEIVING));
        signalMessageLog.setMessageStatus(messageStatusDao.findOrCreate(MessageStatus.RECEIVED));

        signalMessageLog.setSignalMessage(signal);
        signalMessageLogDao.create(signalMessageLog);

        return userMessageLog;
    }

    private To createTo(String role, String partyId) {
        To to = new To();
        to.setRole(partyRoleDao.findOrCreateRole(role));
        to.setPartyId(partyIdDao.findOrCreateParty(partyId, PARTY_ID_TYPE));
        return to;
    }

    private From createFrom(String role, String partyId) {
        From from = new From();
        from.setRole(partyRoleDao.findOrCreateRole(role));
        from.setPartyId(partyIdDao.findOrCreateParty(partyId, PARTY_ID_TYPE));
        return from;
    }

}