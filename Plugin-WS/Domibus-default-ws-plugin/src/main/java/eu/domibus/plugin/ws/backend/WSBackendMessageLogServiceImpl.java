package eu.domibus.plugin.ws.backend;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.ws.exception.WSPluginException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


/**
 * @author Catalin Enache
 * @since 5.0
 */
@Service
public class WSBackendMessageLogServiceImpl implements WSBackendMessageLogService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSBackendMessageLogServiceImpl.class);

    private final WSBackendMessageLogDao wsBackendMessageLogDao;

    public WSBackendMessageLogServiceImpl(WSBackendMessageLogDao wsBackendMessageLogDao) {
        this.wsBackendMessageLogDao = wsBackendMessageLogDao;
    }


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
    public List<WSBackendMessageLogEntity> findAllWithFilter(String messageId, String originalSender, String finalRecipient, LocalDateTime receivedFrom, LocalDateTime receivedTo, int maxPendingMessagesRetrieveCount) {
        return wsBackendMessageLogDao.findAllFailedWithFilter(messageId, originalSender,
                finalRecipient, receivedFrom, receivedTo, maxPendingMessagesRetrieveCount);
    }

    @Override
    public void updateForRetry(List<String> messageIDs) throws WSPluginException {
        int countUpdated = wsBackendMessageLogDao.updateForRetry(messageIDs);
        int total = CollectionUtils.size(messageIDs);
        if (countUpdated != total) {
            throw new WSPluginException("Not all messages could be found [" + countUpdated + "/" + total + "]");
        }
        LOG.debug("[{}] messages updated for retry", total);
    }

}
