package eu.domibus.core.message;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.ebms3.common.model.MessageType;
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
    private DomainCoreConverter domainConverter;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public long countMessages(MessageType messageType, Map<String, Object> filters) {
        long numberOfMessageLogs = 0;
        if (messageType == MessageType.SIGNAL_MESSAGE) {
            numberOfMessageLogs = signalMessageLogDao.countAllInfo(filters);
        } else if (messageType == MessageType.USER_MESSAGE) {
            numberOfMessageLogs = userMessageLogDao.countAllInfo(filters);
        }
        return numberOfMessageLogs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageLogResultRO countAndFindPaged(MessageType messageType, int from, int max, String column, boolean asc, Map<String, Object> filters) {
        MessageLogResultRO result = new MessageLogResultRO();

        List<MessageLogInfo> resultList = new ArrayList<>();
        if (messageType == MessageType.SIGNAL_MESSAGE) {
            long number = getNumberOfMessages(signalMessageLogDao, filters, result);
            if (number > 0) {
                resultList = signalMessageLogDao.findAllInfoPaged(from, max, column, asc, filters);
            }

        } else if (messageType == MessageType.USER_MESSAGE) {
            long number = getNumberOfMessages(userMessageLogDao, filters, result);
            if (number > 0) {
                resultList = userMessageLogDao.findAllInfoPaged(from, max, column, asc, filters);
            }
        }
        result.setMessageLogEntries(resultList
                .stream()
                .map(messageLogInfo -> convertMessageLogInfo(messageLogInfo))
                .collect(Collectors.toList()));
        return result;
    }

    protected long getNumberOfMessages(MessageLogDao dao, Map<String, Object> filters, MessageLogResultRO result) {
        long count;
        boolean isEstimated;
        Integer limit = domibusPropertyProvider.getIntegerProperty("domibus.console.messageLogs.countLimit");
        if (limit > 0 && dao.isElementAtPosition(filters, limit + 1)) {
            count = limit;
            isEstimated = true;
        } else {
            count = dao.countAllInfo(filters);
            isEstimated = false;
        }
        LOG.debug("count User Messages Logs [{}]; is estimated [{}]", count, isEstimated);
        result.setEstimatedCount(isEstimated);
        result.setCount(count);
        return count;
    }

    @Override
    public List<MessageLogInfo> findAllInfoCSV(MessageType messageType, int max, String orderByColumn, boolean asc, Map<String, Object> filters) {

        return (messageType == MessageType.SIGNAL_MESSAGE ?
                signalMessageLogDao.findAllInfoPaged(0, max, orderByColumn, asc, filters) :
                userMessageLogDao.findAllInfoPaged(0, max, orderByColumn, asc, filters));
    }

    /**
     * @param messageLogInfo
     * @return
     */
    MessageLogRO convertMessageLogInfo(MessageLogInfo messageLogInfo) {
        if (messageLogInfo == null) {
            return null;
        }

        return domainConverter.convert(messageLogInfo, MessageLogRO.class);
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
}
