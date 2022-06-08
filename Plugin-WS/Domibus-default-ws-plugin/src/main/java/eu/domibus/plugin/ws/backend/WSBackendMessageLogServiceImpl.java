package eu.domibus.plugin.ws.backend;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.ws.generated.body.FaultDetail;
import eu.domibus.plugin.ws.webservice.WebServiceExceptionFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author Catalin Enache
 * @since 5.0
 */
@Service
public class WSBackendMessageLogServiceImpl implements WSBackendMessageLogService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSBackendMessageLogServiceImpl.class);

    private final WSBackendMessageLogDao wsBackendMessageLogDao;

    private final WebServiceExceptionFactory createFaultMessageIdNotFound;

    public WSBackendMessageLogServiceImpl(WSBackendMessageLogDao wsBackendMessageLogDao,
                                          WebServiceExceptionFactory createFaultMessageIdNotFound) {
        this.wsBackendMessageLogDao = wsBackendMessageLogDao;
        this.createFaultMessageIdNotFound = createFaultMessageIdNotFound;
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
    public FaultDetail updateForRetry(List<String> messageIDs) {
        List<String> messageIdsNotFound = new ArrayList<>();
        for (String messageId : messageIDs) {
            WSBackendMessageLogEntity byMessageId = wsBackendMessageLogDao.findByMessageId(messageId);
            if (byMessageId == null) {
                messageIdsNotFound.add(messageId);
                LOG.warn("WSBackendMessageLogEntity with id [{}] not found", messageId);
            } else {
                byMessageId.setSendAttempts(0);
                byMessageId.setNextAttempt(new Date());
                byMessageId.setFailed(null);
                byMessageId.setBackendMessageStatus(WSBackendMessageStatus.WAITING_FOR_RETRY);
                LOG.debug("Update WSBackendMessageLogEntity [{}] [{}]", messageIDs, byMessageId);
            }
        }

        if (CollectionUtils.isNotEmpty(messageIdsNotFound)) {
            return createFaultMessageIdNotFound.createFaultMessageIdNotFound(String.join(",", messageIdsNotFound));
        }

        return null;
    }

}
