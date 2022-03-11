package eu.domibus.core.message;

import eu.domibus.api.model.MessageType;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.core.converter.MessageCoreMapper;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MESSAGE_DOWNLOAD_MAX_SIZE;

/**
 * @author Federico Martini
 * @since 3.2
 */
@Service
public class MessagesLogServiceImpl implements MessagesLogService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagesLogServiceImpl.class);

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private MessageCoreMapper messageCoreConverter;

    @Autowired
    private MessagesLogServiceHelper messagesLogServiceHelper;

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public long countMessages(MessageType messageType, Map<String, Object> filters) {
        MessageLogDao dao = getMessageLogDao(messageType);
        return dao.countEntries(filters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageLogResultRO countAndFindPaged(MessageType messageType, int from, int max, String column, boolean asc, Map<String, Object> filters) {
        MessageLogResultRO result = new MessageLogResultRO();

        MessageLogDao dao = getMessageLogDao(messageType);
        List<MessageLogInfo> resultList = countAndFilter(dao, from, max, column, asc, filters, result);
        List<MessageLogRO> convertedList = resultList.stream()
                .map(messageLogInfo -> messageCoreConverter.messageLogInfoToMessageLogRO(messageLogInfo))
                .collect(Collectors.toList());
        setCanDownload(convertedList);
        result.setMessageLogEntries(convertedList);

        return result;
    }

    @Override
    public List<MessageLogInfo> findAllInfoCSV(MessageType messageType, int max, String orderByColumn, boolean asc, Map<String, Object> filters) {
        MessageLogDao dao = getMessageLogDao(messageType);
        return dao.findAllInfoPaged(0, max, orderByColumn, asc, filters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageLogRO findUserMessageById(String messageId) {
        HashMap<String, Object> filters = new HashMap<>();
        filters.put("messageId", messageId);
        MessageLogResultRO result = countAndFindPaged(MessageType.USER_MESSAGE, 0, 1, null, true, filters);

        List<MessageLogRO> messages = result.getMessageLogEntries();
        if (messages.size() == 0) {
            LOG.info("Could not find message log entry for id [{}].", messageId);
            return null;
        }
        if (messages.size() > 1) {
            LOG.warn("Found more than one message log entry for id [{}].", messageId);
        }
        return messages.get(0);
    }

    protected List<MessageLogInfo> countAndFilter(MessageLogDao dao, int from, int max, String column, boolean asc, Map<String, Object> filters, MessageLogResultRO result) {
        List<MessageLogInfo> resultList = new ArrayList<>();
        long number = messagesLogServiceHelper.calculateNumberOfMessages(dao, filters, result);
        if (number > 0) {
            resultList = dao.findAllInfoPaged(from, max, column, asc, filters);
        }

        return resultList;
    }

    protected List<MessageLogRO> setCanDownload(List<MessageLogRO> resultList) {
        LOG.debug("Check whether the message's can download or not.");
        int maxDownLoadSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGE_DOWNLOAD_MAX_SIZE);
        for (MessageLogRO messageLogRO : resultList) {
            messageLogRO.setCanDownload(true);
            long content = messageLogRO.getPartLength();
            LOG.debug("The message size [{}]: ", content);
            if (content > maxDownLoadSize) {
                LOG.debug("Couldn't download the message. The message size exceeds maximum download size limit [{}].: ", maxDownLoadSize);
                messageLogRO.setCanDownload(false);
            }
        }
        return resultList;
    }

    protected MessageLogDao getMessageLogDao(MessageType messageType) {
        return (messageType == MessageType.SIGNAL_MESSAGE) ? signalMessageLogDao : userMessageLogDao;
    }
}
