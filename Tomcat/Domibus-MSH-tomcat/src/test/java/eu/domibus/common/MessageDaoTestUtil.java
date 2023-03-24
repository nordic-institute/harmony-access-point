package eu.domibus.common;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.*;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.acknowledge.MessageAcknowledgeConverter;
import eu.domibus.core.message.acknowledge.MessageAcknowledgementDao;
import eu.domibus.core.message.dictionary.*;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.util.DateUtilImpl;
import joptsimple.internal.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Component
public class MessageDaoTestUtil {
    public static final String MPC = "mpc";
    public static final String DEFAULT_MPC = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC";

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

    @Autowired
    MessageAcknowledgementDao messageAcknowledgementDao;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;

    @Autowired
    MessageAcknowledgeConverter messageAcknowledgeConverter;

    @Autowired
    UserMessageDefaultService userMessageDefaultService;

    final static String PARTY_ID_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    final static String INITIATOR_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";
    final static String RESPONDER_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";


    public void createSignalMessageLog(String msgId, Date received) {
        createSignalMessageLog(msgId, received, MSHRole.RECEIVING, MessageStatus.RECEIVED);
    }

    public void createSignalMessageLog(String msgId, Date received, MSHRole mshRole, MessageStatus messageStatus) {
        UserMessage userMessage = new UserMessage();
        userMessage.setMessageId(msgId);
        userMessage.setMshRole(mshRoleDao.findOrCreate(mshRole == MSHRole.RECEIVING ? MSHRole.SENDING : MSHRole.RECEIVING));
        userMessage.setConversationId("conversation-" + msgId);

        MessageProperty messageProperty1 = propertyDao.findOrCreateProperty("originalSender", "originalSender1", "");
        MessageProperty messageProperty2 = propertyDao.findOrCreateProperty("finalRecipient", "finalRecipient2", "");
        userMessage.setMessageProperties(new HashSet<>(Arrays.asList(messageProperty1, messageProperty2)));

        SignalMessage signal = new SignalMessage();
        signal.setUserMessage(userMessage);
        signal.setSignalMessageId("signal-" + msgId);
        signal.setMshRole(mshRoleDao.findOrCreate(mshRole));
        signalMessageDao.create(signal);

        SignalMessageLog signalMessageLog = new SignalMessageLog();
        signalMessageLog.setReceived(received);
        signalMessageLog.setMshRole(mshRoleDao.findOrCreate(mshRole));
        signalMessageLog.setMessageStatus(messageStatusDao.findOrCreate(messageStatus));

        signalMessageLog.setSignalMessage(signal);
        signalMessageLogDao.create(signalMessageLog);
    }

    public UserMessageLog createUserMessageLog(String msgId, Date received, MSHRole mshRole, MessageStatus messageStatus, boolean isTestMessage, boolean properties, String mpc, Date archivedAndExported, boolean fragment) {
        String originalSender = null, finalRecipient = null;
        if (properties) {
            originalSender = "originalSender1";
            finalRecipient = "finalRecipient2";
        }
        return createUserMessageLog(msgId, received, mshRole, messageStatus, isTestMessage, mpc, archivedAndExported, originalSender, finalRecipient, fragment);
    }

    public UserMessageLog createUserMessageLog(String msgId, Date received, MSHRole mshRole, MessageStatus messageStatus, boolean isTestMessage, String mpc, Date archivedAndExported,
                                               String originalSender, String finalRecipient, boolean fragment) {
        UserMessage userMessage = new UserMessage();
        userMessage.setMessageId(msgId);
        userMessage.setConversationId("conversation-" + msgId);
        userMessage.setMshRole(mshRoleDao.findOrCreate(mshRole));

        if (!Strings.isNullOrEmpty(originalSender) || !Strings.isNullOrEmpty(finalRecipient)) {
            MessageProperty messageProperty1 = propertyDao.findOrCreateProperty("originalSender", originalSender, "");
            MessageProperty messageProperty2 = propertyDao.findOrCreateProperty("finalRecipient", finalRecipient, "");
            userMessage.setMessageProperties(new HashSet<>(Arrays.asList(messageProperty1, messageProperty2)));
        }

        PartyInfo partyInfo = new PartyInfo();
        partyInfo.setFrom(createFrom(INITIATOR_ROLE, "domibus-blue"));
        partyInfo.setTo(createTo(RESPONDER_ROLE, "domibus-red"));
        userMessage.setPartyInfo(partyInfo);

        final String serviceValue = isTestMessage ? "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/service" : "bdx:noprocess";
        final String serviceType = isTestMessage ? null : "tc1";
        final String actionValue = isTestMessage ? "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/test" : "TC1Leg1";
        userMessage.setService(serviceDao.findOrCreateService(serviceValue, serviceType));
        userMessage.setAction(actionDao.findOrCreateAction(actionValue));

        userMessage.setSourceMessage(false);
        userMessage.setTestMessage(isTestMessage);
        userMessage.setMessageFragment(fragment);
        userMessage.setMpc(mpcDao.findOrCreateMpc(mpc));
        userMessageDao.create(userMessage);

        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMshRole(mshRoleDao.findOrCreate(mshRole));
        userMessageLog.setMessageStatus(messageStatusDao.findOrCreate(messageStatus));
        setUserMessageLogDates(userMessageLog, received, archivedAndExported);
        userMessageLog.setNotificationStatus(notificationStatusDao.findOrCreate(NotificationStatus.NOTIFIED));

        userMessageLog.setUserMessage(userMessage);
        userMessageLogDao.create(userMessageLog);

        return userMessageLog;
    }

    public static void setUserMessageLogDates(UserMessageLog userMessageLog, Date received, Date archivedAndExported) {
        userMessageLog.setExported(archivedAndExported);
        userMessageLog.setArchived(archivedAndExported);
        userMessageLog.setReceived(received);
        switch (userMessageLog.getMessageStatus()) {
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
    }

    @Transactional
    public UserMessageLog createUserMessageLog(String msgId, Date received, MSHRole mshRole, MessageStatus messageStatus, boolean properties, String mpc, Date archived) {
        return createUserMessageLog(msgId, received, mshRole, messageStatus, false, properties, mpc, archived, false);
    }

    @Transactional
    public UserMessageLog createUserMessageLog(String msgId, Date received, MSHRole mshRole, MessageStatus messageStatus) {
        return createUserMessageLog(msgId, received, mshRole, messageStatus, false, true, MPC, new Date(), false);
    }

    @Transactional
    public UserMessageLog createUserMessageLog(String msgId, Date received) {
        return createUserMessageLog(msgId, received, MessageStatus.RECEIVED, MPC, false);
    }
    @Transactional
    public UserMessageLog createUserMessageLogFragment(String msgId, Date received) {
        return createUserMessageLog(msgId, received, MessageStatus.RECEIVED, MPC, true);
    }

    @Transactional
    public UserMessageLog createUserMessageLog(String msgId, Date received, MSHRole mshRole, MessageStatus messageStatus, String originalSender, String finalRecipient) {
        return createUserMessageLog(msgId, received, mshRole, messageStatus, false, MPC, new Date(), originalSender, finalRecipient, false);
    }

    @Transactional
    public UserMessageLog createUserMessageLog(String msgId, Date received, MessageStatus messageStatus, String mpc, boolean fragment) {
        return createUserMessageLog(msgId, received, MSHRole.RECEIVING, messageStatus, false, true, mpc, null, fragment);
    }

    @Transactional
    public UserMessageLog createUserMessageLog(String msgId, MSHRole mshRole, Date received, MessageStatus messageStatus, String mpc) {
        return createUserMessageLog(msgId, received, mshRole, messageStatus, false, true, mpc, null, false);
    }

    @Transactional
    public UserMessageLog createTestMessage(String msgId) {
        UserMessageLog userMessageLog = createUserMessageLog(msgId, new Date(), MSHRole.SENDING, MessageStatus.ACKNOWLEDGED, true, true, MPC, new Date(), false);

        SignalMessage signal = new SignalMessage();
        signal.setUserMessage(userMessageLog.getUserMessage());
        signal.setSignalMessageId("signal-" + msgId);
        signal.setRefToMessageId(msgId);
        signal.setMshRole(mshRoleDao.findOrCreate(MSHRole.RECEIVING));
        signalMessageDao.create(signal);

        SignalMessageLog signalMessageLog = new SignalMessageLog();
        signalMessageLog.setReceived(new Date());
        signalMessageLog.setMshRole(mshRoleDao.findOrCreate(MSHRole.RECEIVING));
        signalMessageLog.setMessageStatus(messageStatusDao.findOrCreate(MessageStatus.RECEIVED));

        signalMessageLog.setSignalMessage(signal);
        signalMessageLogDao.create(signalMessageLog);

        return userMessageLog;
    }

    public UserMessageLog createTestMessageInSend_Failure(String msgId) {
        UserMessageLog userMessageLog = createUserMessageLog(msgId, new Date(), MSHRole.SENDING, MessageStatus.SEND_FAILURE, false, MPC, new Date());

        SignalMessage signal = new SignalMessage();
        signal.setUserMessage(userMessageLog.getUserMessage());
        signal.setSignalMessageId("signal-" + msgId);
        signal.setMshRole(mshRoleDao.findOrCreate(MSHRole.RECEIVING));
        signalMessageDao.create(signal);

        SignalMessageLog signalMessageLog = new SignalMessageLog();
        signalMessageLog.setReceived(new Date());
        signalMessageLog.setMshRole(mshRoleDao.findOrCreate(MSHRole.RECEIVING));
        signalMessageLog.setMessageStatus(messageStatusDao.findOrCreate(MessageStatus.SEND_FAILURE));

        signalMessageLog.setSignalMessage(signal);
        signalMessageLogDao.create(signalMessageLog);

        return userMessageLog;
    }

    private To createTo(String role, String partyId) {
        To to = new To();
        to.setToRole(partyRoleDao.findOrCreateRole(role));
        to.setToPartyId(partyIdDao.findOrCreateParty(partyId, PARTY_ID_TYPE));
        return to;
    }

    private From createFrom(String role, String partyId) {
        From from = new From();
        from.setFromRole(partyRoleDao.findOrCreateRole(role));
        from.setFromPartyId(partyIdDao.findOrCreateParty(partyId, PARTY_ID_TYPE));
        return from;
    }

    @Transactional
    public MessageStatusEntity getMessageStatusEntity(MessageStatus messageStatus) {
        return messageStatusDao.findOrCreate(messageStatus);
    }

    @Transactional
    public MSHRoleEntity getMshRole(MSHRole sending) {
        return mshRoleDao.findOrCreate(sending);
    }

    @Transactional
    public MessageProperty createMessageProperty(String originalSender, String originalSender1, String type) {
        return propertyDao.findOrCreateProperty(originalSender, originalSender1, type);
    }

    @Transactional
    public List<UserMessageLog> getAllUserMessageLogs() {
        return em.createQuery("select uml from UserMessageLog uml", UserMessageLog.class).getResultList();
    }

    @Transactional
    public void clear() {
        final List<UserMessageLogDto> allMessages = userMessageLogDao.getAllMessages();
        userMessageDefaultService.deleteMessages(allMessages);
    }

    @Transactional
    public void deleteMessages(List<Long> ids) {
        userMessageLogDao.deleteMessageLogs(ids);
        signalMessageLogDao.deleteMessageLogs(ids);
        signalMessageDao.deleteMessages(ids);
    }
}
