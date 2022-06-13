package eu.domibus.plugin.ws;

import eu.domibus.api.model.*;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.dictionary.*;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.util.DateUtilImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Component
public class MessageDaoTestUtil {
    public static final String MPC = "mpc";

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Autowired
    SignalMessageLogDao signalMessageLogDao;

    @Autowired
    UserMessageDao userMessageDao;
    @Autowired
    MpcDao mpcDao;

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

    public UserMessageLog createUserMessageLog(String msgId, Date received, MSHRole mshRole, MessageStatus messageStatus, boolean isTestMessage, boolean properties, String mpc, Date archivedAndExported) {
        UserMessage userMessage = new UserMessage();
        userMessage.setMessageId(msgId);
        userMessage.setConversationId("conversation-" + msgId);

        if (properties) {
            MessageProperty messageProperty1 = propertyDao.findOrCreateProperty("originalSender", "originalSender1", "");
            MessageProperty messageProperty2 = propertyDao.findOrCreateProperty("finalRecipient", "finalRecipient2", "");
            userMessage.setMessageProperties(new HashSet<>(Arrays.asList(messageProperty1, messageProperty2)));
        }
        PartyInfo partyInfo = new PartyInfo();
        partyInfo.setFrom(createFrom());
        partyInfo.setTo(createTo());
        userMessage.setPartyInfo(partyInfo);

        final String serviceValue = isTestMessage ? "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/service" : "bdx:noprocess";
        final String serviceType = isTestMessage ? null : "tc1";
        final String actionValue = isTestMessage ? "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/test" : "TC1Leg1";
        userMessage.setService(serviceDao.findOrCreateService(serviceValue, serviceType));
        userMessage.setAction(actionDao.findOrCreateAction(actionValue));

        userMessage.setSourceMessage(false);
        userMessage.setTestMessage(isTestMessage);
        userMessage.setMpc(mpcDao.findOrCreateMpc(mpc));
        userMessageDao.create(userMessage);

        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setReceived(received);
        userMessageLog.setMshRole(mshRoleDao.findOrCreate(mshRole));
        userMessageLog.setMessageStatus(messageStatusDao.findOrCreate(messageStatus));
        switch (messageStatus) {
            case DELETED:
                userMessageLog.setDeleted(received);
                break;
            case RECEIVED:
                userMessageLog.setReceived(received);
                break;
            case DOWNLOADED:
                userMessageLog.setDownloaded(received);
                break;
            case WAITING_FOR_RETRY:
                userMessageLog.setNextAttempt(new DateUtilImpl().fromString("2019-01-01T12:00:00Z"));
                userMessageLog.setSendAttempts(1);
                userMessageLog.setSendAttemptsMax(5);
                userMessageLog.setScheduled(false);
                break;
        }
        userMessageLog.setExported(archivedAndExported);
        userMessageLog.setArchived(archivedAndExported);
        userMessageLog.setNotificationStatus(notificationStatusDao.findOrCreate(NotificationStatus.NOTIFIED));

        userMessageLog.setUserMessage(userMessage);
        userMessageLogDao.create(userMessageLog);

        return userMessageLog;
    }

    @Transactional
    public UserMessageLog createTestMessage(String msgId) {
        UserMessageLog userMessageLog = createUserMessageLog(msgId, new Date(), MSHRole.SENDING, MessageStatus.ACKNOWLEDGED, true, true, MPC, new Date());

        SignalMessage signal = new SignalMessage();
        signal.setUserMessage(userMessageLog.getUserMessage());
        signal.setSignalMessageId("signal-" + msgId);
        signal.setRefToMessageId(msgId);
        signalMessageDao.create(signal);

        SignalMessageLog signalMessageLog = new SignalMessageLog();
        signalMessageLog.setReceived(new Date());
        signalMessageLog.setMshRole(mshRoleDao.findOrCreate(MSHRole.RECEIVING));
        signalMessageLog.setMessageStatus(messageStatusDao.findOrCreate(MessageStatus.RECEIVED));

        signalMessageLog.setSignalMessage(signal);
        signalMessageLogDao.create(signalMessageLog);

        return userMessageLog;
    }

    private To createTo() {
        To to = new To();
        to.setToRole(partyRoleDao.findOrCreateRole(MessageDaoTestUtil.RESPONDER_ROLE));
        to.setToPartyId(partyIdDao.findOrCreateParty("domibus-red", PARTY_ID_TYPE));
        return to;
    }

    private From createFrom() {
        From from = new From();
        from.setFromRole(partyRoleDao.findOrCreateRole(MessageDaoTestUtil.INITIATOR_ROLE));
        from.setFromPartyId(partyIdDao.findOrCreateParty("domibus-blue", PARTY_ID_TYPE));
        return from;
    }

}
