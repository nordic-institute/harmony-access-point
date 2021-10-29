package eu.domibus.core.earchive.listener;

import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.EArchiveBatchStatus;
import eu.domibus.core.earchive.EArchivingDefaultService;
import eu.domibus.core.util.JmsUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * @author François Gautier
 * @since 5.0
 */
@Component
public class EArchiveNotificationListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveNotificationListener.class);

    private final DatabaseUtil databaseUtil;

    private final EArchivingDefaultService eArchiveService;

    private final JmsUtil jmsUtil;

    public EArchiveNotificationListener(
            DatabaseUtil databaseUtil,
            EArchivingDefaultService eArchiveService,
            JmsUtil jmsUtil) {
        this.databaseUtil = databaseUtil;
        this.eArchiveService = eArchiveService;
        this.jmsUtil = jmsUtil;
    }

    @Override
    public void onMessage(Message message) {
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());

        String batchId = jmsUtil.getStringPropertySafely(message, MessageConstants.BATCH_ID);

        Long entityId = jmsUtil.getLongPropertySafely(message, MessageConstants.BATCH_ENTITY_ID);
        if (StringUtils.isBlank(batchId) || entityId == null) {
            LOG.error("Could not get the batchId [{}] and/or entityId [{}]", batchId, entityId);
            return;
        }
        jmsUtil.setDomain(message);

        EArchiveBatchStatus notificationType = EArchiveBatchStatus.valueOf(jmsUtil.getStringPropertySafely(message, MessageConstants.NOTIFICATION_TYPE));

        EArchiveBatchEntity eArchiveBatchByBatchId = eArchiveService.getEArchiveBatch(entityId);

        if (notificationType == EArchiveBatchStatus.FAILED) {
            LOG.info("Notification to the earchive client for batch FAILED [{}] ", eArchiveBatchByBatchId);
            // TODO: François Gautier 28-10-21 notification failed
        }

        if (notificationType == EArchiveBatchStatus.EXPORTED) {
            LOG.info("Notification to the earchive client for batch EXPORTED [{}] ", eArchiveBatchByBatchId);
            // TODO: François Gautier 28-10-21 notification exported
        }
    }

}
