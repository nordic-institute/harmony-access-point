package eu.domibus.plugin.ws.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


/**
 * @author Catalin Enache
 * @since 5.0
 */
@Service
public class WSBackendMessageLogServiceImpl implements WSBackendMessageLogService {

    @Autowired
    WSBackendMessageLogDao wsBackendMessageLogDao;


    @Override
    public void create(WSBackendMessageLogEntity wsBackendMessageLogEntity) {
        wsBackendMessageLogDao.create(wsBackendMessageLogEntity);
    }

    @Override
    public WSBackendMessageLogEntity findByMessageId(String messageId) {
        return wsBackendMessageLogDao.findByMessageId(messageId);
    }

    @Override
    public void delete(WSBackendMessageLogEntity wsBackendMessageLogEntity) {
        wsBackendMessageLogDao.delete(wsBackendMessageLogEntity);
    }

    @Override
    public List<WSBackendMessageLogEntity> findAllWithFilter(String messageId, String fromPartyId, String originalSender, String finalRecipient, LocalDateTime receivedFrom, LocalDateTime receivedTo, int maxPendingMessagesRetrieveCount) {
        return wsBackendMessageLogDao.findAllFailedWithFilter(messageId, fromPartyId, originalSender,
                finalRecipient, receivedFrom, receivedTo, maxPendingMessagesRetrieveCount);
    }

}
