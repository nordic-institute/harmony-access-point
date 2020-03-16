package eu.domibus.common.services.impl;

import eu.domibus.core.pull.SignalMessageLogDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.common.services.MessagesLogService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageLogResultRO countAndFindPaged(MessageType messageType, int from, int max, String column, boolean asc, Map<String, Object> filters) {
        MessageLogResultRO result = new MessageLogResultRO();

        List<MessageLogInfo> resultList = new ArrayList<>();
        if (messageType == MessageType.SIGNAL_MESSAGE) {
            long numberOfSignalMessageLogs = signalMessageLogDao.countAllInfo(asc, filters);
            LOG.debug("count Signal Messages Logs [{}]", numberOfSignalMessageLogs);
            result.setCount(numberOfSignalMessageLogs);
            if (numberOfSignalMessageLogs > 0) {
                resultList = signalMessageLogDao.findAllInfoPaged(from, max, column, asc, filters);
            }

        } else if (messageType == MessageType.USER_MESSAGE) {
            long numberOfUserMessageLogs = userMessageLogDao.countAllInfo(asc, filters);
            LOG.debug("count User Messages Logs [{}]", numberOfUserMessageLogs);
            result.setCount(numberOfUserMessageLogs);
            if (numberOfUserMessageLogs > 0) {
                resultList = userMessageLogDao.findAllInfoPaged(from, max, column, asc, filters);
            }
        }
        result.setMessageLogEntries(resultList
                .stream()
                .map(messageLogInfo -> convertMessageLogInfo(messageLogInfo))
                .collect(Collectors.toList()));
        return result;
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

}
