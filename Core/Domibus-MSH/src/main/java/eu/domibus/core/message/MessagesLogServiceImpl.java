package eu.domibus.core.message;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageType;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.MessageCoreMapper;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Federico Martini
 * @author Ion Perpegel
 * @since 3.2
 */
@Service
public class MessagesLogServiceImpl implements MessagesLogService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagesLogServiceImpl.class);

    private final Set<MessageStatus> hasNoEnvelopes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            MessageStatus.SEND_FAILURE,
            MessageStatus.WAITING_FOR_RETRY,
            MessageStatus.SEND_ENQUEUED,
            MessageStatus.READY_TO_PULL
    )));

    private final UserMessageLogDao userMessageLogDao;

    private final SignalMessageLogDao signalMessageLogDao;

    private final MessageCoreMapper messageCoreConverter;

    private final MessagesLogServiceHelper messagesLogServiceHelper;

    private final MessageLogDictionaryDataService messageLogDictionaryDataService;

    public MessagesLogServiceImpl(UserMessageLogDao userMessageLogDao, SignalMessageLogDao signalMessageLogDao,
                                  MessageCoreMapper messageCoreConverter, MessagesLogServiceHelper messagesLogServiceHelper,
                                  MessageLogDictionaryDataService messageLogDictionaryDataService) {
        this.userMessageLogDao = userMessageLogDao;
        this.signalMessageLogDao = signalMessageLogDao;
        this.messageCoreConverter = messageCoreConverter;

        this.messagesLogServiceHelper = messagesLogServiceHelper;
        this.messageLogDictionaryDataService = messageLogDictionaryDataService;
    }

    @Override
    public long countMessages(MessageType messageType, Map<String, Object> filters) {
        MessageLogDao dao = getMessageLogDao(messageType);
        return dao.countEntries(filters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public MessageLogResultRO countAndFindPaged(MessageType messageType, int from, int max, String column, boolean asc, Map<String, Object> filters, List<String> fields) {
        MessageLogResultRO result = new MessageLogResultRO();

        MessageLogDao dao = getMessageLogDao(messageType);
        List<MessageLogInfo> resultList = countAndFilter(dao, from, max, column, asc, filters, fields, result);

        List<MessageLogRO> convertedList = resultList.stream()
                .map(messageLogInfo -> messageCoreConverter.messageLogInfoToMessageLogRO(messageLogInfo))
                .collect(Collectors.toList());

        setCanDownloadMessageAndEnvelope(convertedList);
        result.setMessageLogEntries(convertedList);

        return result;
    }

    @Override
    @Transactional
    public List<MessageLogInfo> findAllInfoCSV(MessageType messageType, int max, String orderByColumn, boolean asc, Map<String, Object> filters, List<String> fields) {
        MessageLogDao dao = getMessageLogDao(messageType);
        List<MessageLogInfo> resultList = dao.findAllInfoPaged(0, max, orderByColumn, asc, filters, fields);
        resultList.forEach(msgInfo -> {
            messageLogDictionaryDataService.addDictionaryData(fields, msgInfo);
        });
        return resultList;
    }

    protected List<MessageLogInfo> countAndFilter(MessageLogDao dao, int from, int max, String column, boolean asc, Map<String, Object> filters,
                                                  List<String> fields, MessageLogResultRO result) {
        List<MessageLogInfo> resultList = new ArrayList<>();
        long number = messagesLogServiceHelper.calculateNumberOfMessages(dao, filters, result);
        if (number > 0) {
            resultList = dao.findAllInfoPaged(from, max, column, asc, filters, fields);

            resultList.forEach(msgInfo -> {
                messageLogDictionaryDataService.addDictionaryData(fields, msgInfo);
            });
        }

        return resultList;
    }

    protected void setCanDownloadMessageAndEnvelope(List<MessageLogRO> resultList) {
        LOG.debug("Check whether the message's can download or not.");
        for (MessageLogRO messageLogRO : resultList) {
            setCanDownloadMessage(messageLogRO);
            setCanDownloadEnvelope(messageLogRO);
        }
    }

    private void setCanDownloadMessage(MessageLogRO messageLogRO) {
        if (messageLogRO.getMessageType() == MessageType.SIGNAL_MESSAGE
                || messageLogRO.isSplitAndJoin()
                || messageLogRO.getDeleted() != null) {
            messageLogRO.setCanDownloadMessage(false);
            return;
        }

        messageLogRO.setCanDownloadMessage(true);
    }

    protected void setCanDownloadEnvelope(MessageLogRO messageLogRO) {
        MessageStatus messageStatus = messageLogRO.getMessageStatus();
        if (hasNoEnvelopes.contains(messageStatus)) {
            LOG.debug("The message [{}] status is [{}]: setting canDownloadEnvelope to false.", messageLogRO.getMessageId(), messageStatus);
            messageLogRO.setCanDownloadEnvelope(false);
            return;
        }

        messageLogRO.setCanDownloadEnvelope(true);
    }

    protected MessageLogDao getMessageLogDao(MessageType messageType) {
        return (messageType == MessageType.SIGNAL_MESSAGE) ? signalMessageLogDao : userMessageLogDao;
    }
}
