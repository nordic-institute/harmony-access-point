package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.api.multitenancy.RestoreMDCContextRunnable;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogBuilder;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserMessagePersistenceService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessagePersistenceService.class);

    protected MessagingService messagingService;
    protected UserMessageLogDefaultService userMessageLogService;
    protected UserMessageRawEnvelopeDao rawEnvelopeLogDao;
    protected SignalMessageDao signalMessageDao;
    protected SignalMessageLogDao signalMessageLogDao;
    protected MessageStatusDao messageStatusDao;
    protected MshRoleDao mshRoleDao;
    protected ReceiptDao receiptDao;

    public UserMessagePersistenceService(MessagingService messagingService, UserMessageLogDefaultService userMessageLogService, UserMessageRawEnvelopeDao rawEnvelopeLogDao, SignalMessageDao signalMessageDao, SignalMessageLogDao signalMessageLogDao, MessageStatusDao messageStatusDao, MshRoleDao mshRoleDao, ReceiptDao receiptDao) {
        this.messagingService = messagingService;
        this.userMessageLogService = userMessageLogService;
        this.rawEnvelopeLogDao = rawEnvelopeLogDao;
        this.signalMessageDao = signalMessageDao;
        this.signalMessageLogDao = signalMessageLogDao;
        this.messageStatusDao = messageStatusDao;
        this.mshRoleDao = mshRoleDao;
        this.receiptDao = receiptDao;
    }

    @Transactional
    public void saveIncomingMessage(UserMessage userMessage, List<PartInfo> partInfoList, NotificationStatus notificationStatus, String backendName, UserMessageRaw userMessageRaw, SignalMessageResult signalMessageResult, Runnable notifyBackend) {
        messagingService.saveUserMessageAndPayloads(userMessage, partInfoList);
        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ENTITY_ID, String.valueOf(userMessage.getEntityId()));

        userMessageLogService.save(
                userMessage,
                MessageStatus.RECEIVED.toString(),
                notificationStatus.toString(),
                MSHRole.RECEIVING.toString(),
                0,
                backendName);
        userMessageRaw.setUserMessage(userMessage);
        rawEnvelopeLogDao.create(userMessageRaw);

        if(signalMessageResult != null) {
            saveSignalMessage(signalMessageResult, userMessage);
        }

        // The code executed when notifying the backend might alter the MDC (eg by submitting a new message)
        // If we use the modified MDC afterwards, it can lead to issues:
        // - The message entity ID is extracted from the MDC in MSHWebservice::setUserMessageEntityIdOnContext
        // and stored in the CXF message exchange context, and then used to save the raw xml envelope.
        // - Log entries may become inconsistent due to the altered MDC.
        // To prevent these issues, we restore the original MDC context after the backend notification,
        // ensuring that subsequent operations use the correct, unaltered MDC.
        new RestoreMDCContextRunnable(notifyBackend).run();

        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PERSISTED);
    }


    private void saveSignalMessage(SignalMessageResult signalMessageResult, UserMessage userMessage) {
        final eu.domibus.api.model.SignalMessage signalMessage = signalMessageResult.getSignalMessage();
        final ReceiptEntity receiptEntity = signalMessageResult.getReceiptEntity();
        receiptEntity.setSignalMessage(signalMessage);
        signalMessage.setUserMessage(userMessage);

        LOG.debug("Save signalMessage with messageId [{}], refToMessageId [{}]", signalMessage.getSignalMessageId(), signalMessage.getRefToMessageId());
        // Stores the signal message
        signalMessageDao.create(signalMessage);
        //stores the receipt
        receiptDao.create(receiptEntity);

        MessageStatusEntity messageStatus = messageStatusDao.findOrCreate(MessageStatus.ACKNOWLEDGED);
        MSHRoleEntity role = mshRoleDao.findOrCreate(MSHRole.SENDING);

        // Builds the signal message log
        SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
                .setSignalMessage(signalMessage)
                .setMessageStatus(messageStatus)
                .setMshRole(role);
        // Saves an entry of the signal message log
        SignalMessageLog signalMessageLog = smlBuilder.build();
        signalMessageLogDao.create(signalMessageLog);
    }
}
