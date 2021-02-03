package eu.domibus.plugin.ws.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


/**
 * @author Catalin Enache
 * @since 5.0
 */
@Service
public class WSMessageLogServiceImpl implements WSMessageLogService {

    @Autowired
    WSMessageLogDao wsMessageLogDao;


    @Override
    public void create(WSMessageLogEntity wsMessageLogEntity) {
        wsMessageLogDao.create(wsMessageLogEntity);
    }

    @Override
    public WSMessageLogEntity findByMessageId(String messageId) {
        return wsMessageLogDao.findByMessageId(messageId);
    }

    @Override
    public void delete(WSMessageLogEntity wsMessageLogEntity) {
        wsMessageLogDao.delete(wsMessageLogEntity);
    }

    @Override
    public void deleteByMessageId(String messageId) {
        wsMessageLogDao.deleteByMessageId(messageId);
    }

    @Override
    public void deleteByMessageIds(List<String> messageIds) {
        wsMessageLogDao.deleteByMessageIds(messageIds);
    }

    @Override
    public List<WSMessageLogEntity> findAllWithFilter(String messageId, String fromPartyId, String conversationId, String referenceMessageId, String originalSender, String finalRecipient, LocalDateTime receivedFrom, LocalDateTime receivedTo, int maxPendingMessagesRetrieveCount) {
        return wsMessageLogDao.findAllWithFilter(messageId, fromPartyId, conversationId, referenceMessageId, originalSender,
                finalRecipient, receivedFrom, receivedTo, maxPendingMessagesRetrieveCount);
    }

}
