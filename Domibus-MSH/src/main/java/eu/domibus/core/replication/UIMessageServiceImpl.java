package eu.domibus.core.replication;

import eu.domibus.core.converter.MessageCoreMapper;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.MessagesLogServiceHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation for {@link UIMessageService}
 *
 * @author  Catalin Enache
 * @since 4.0
 */
@Service
public class UIMessageServiceImpl implements UIMessageService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIMessageServiceImpl.class);

    @Autowired
    private UIMessageDao uiMessageDao;

    @Autowired
    private MessageCoreMapper messageCoreConverter;

    @Autowired
    MessagesLogServiceHelper messagesLogServiceHelper;

    @Override
    @Transactional(readOnly = true)
    public long countMessages(Map<String, Object> filters) {
        return uiMessageDao.countEntries(filters);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageLogInfo> findPaged(int from, int max, String column, boolean asc, Map<String, Object> filters) {
        return uiMessageDao.findPaged(from, max, column, asc, filters)
                .stream()
                .map(messageCoreConverter::uiMessageEntityToMessageLogInfo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MessageLogResultRO countAndFindPaged(int from, int max, String column, boolean asc, Map<String, Object> filters) {
        MessageLogResultRO result = new MessageLogResultRO();
        List<UIMessageEntity> uiMessageEntityList = new ArrayList<>();

        //make the count
        long numberOfMessages = uiMessageDao.countEntries(filters);

        if (numberOfMessages != 0) {
            //query for the page results
            uiMessageEntityList = uiMessageDao.findPaged(from, max, column, asc, filters);
        }

        result.setCount(numberOfMessages);
        result.setMessageLogEntries(uiMessageEntityList
                .stream()
                .map(messageCoreConverter::uiMessageEntityToMessageLogRO)
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    public void saveOrUpdate(UIMessageEntity uiMessageEntity) {
        try {
            uiMessageDao.saveOrUpdate(uiMessageEntity);
        } catch (Exception e) {
            //we log here just in case an exception is thrown
            LOG.error("Failed to insert/update into TB_MESSAGE_UI having messageId=[{}] ", uiMessageEntity.getMessageId(), e);
        }
    }

}
