package eu.domibus.core.earchive;

import eu.domibus.api.earchive.DomibusEArchiveService;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.jms.spi.InternalJMSConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.MIN;
import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.dtf;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchivingDefaultService implements DomibusEArchiveService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingDefaultService.class);

    public static final int CONTINUOUS_ID = 1;

    public static final int SANITY_ID = 2;

    private final EArchiveBatchStartDao eArchiveBatchStartDao;

    private final EArchiveBatchDao eArchiveBatchDao;

    private final JMSManager jmsManager;

    private final Queue eArchiveNotificationQueue;

    public EArchivingDefaultService(EArchiveBatchStartDao eArchiveBatchStartDao, EArchiveBatchDao eArchiveBatchDao,
                                    JMSManager jmsManager,
                                    @Qualifier(InternalJMSConstants.EARCHIVE_NOTIFICATION_QUEUE) Queue eArchiveNotificationQueue) {
        this.eArchiveBatchStartDao = eArchiveBatchStartDao;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.jmsManager = jmsManager;
        this.eArchiveNotificationQueue = eArchiveNotificationQueue;
    }

    @Override
    public void updateStartDateContinuousArchive(Date startDate) {
        updateEArchiveBatchStart(CONTINUOUS_ID, startDate);
    }

    @Override
    public void updateStartDateSanityArchive(Date startDate) {
        updateEArchiveBatchStart(SANITY_ID, startDate);
    }

    @Override
    public Date getStartDateContinuousArchive() {
        LocalDate parse = LocalDate.parse(StringUtils.substring(StringUtils.leftPad(eArchiveBatchStartDao.findByReference(CONTINUOUS_ID).getLastPkUserMessage() + "", 18, "0"), 0, 8), dtf);
        return Date.from(parse.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    @Override
    public Date getStartDateSanityArchive() {
        LocalDate parse = LocalDate.parse(StringUtils.substring(StringUtils.leftPad(eArchiveBatchStartDao.findByReference(SANITY_ID).getLastPkUserMessage() + "", 18, "0"), 0, 8), dtf);
        return Date.from(parse.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    private void updateEArchiveBatchStart(int sanityId, Date startDate) {
        EArchiveBatchStart byReference = eArchiveBatchStartDao.findByReference(sanityId);
        long lastPkUserMessage = Long.parseLong(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC).format(dtf) + MIN);
        if (LOG.isDebugEnabled()) {
            LOG.debug("New start date archive [{}] batch lastPkUserMessage : [{}]", byReference.getDescription(), lastPkUserMessage);
        }
        byReference.setLastPkUserMessage(lastPkUserMessage);
        eArchiveBatchStartDao.update(byReference);
    }

    public EArchiveBatchEntity getEArchiveBatch(long entityId) {
        EArchiveBatchEntity eArchiveBatchByBatchId = eArchiveBatchDao.findEArchiveBatchByBatchId(entityId);

        if (eArchiveBatchByBatchId == null) {
            throw new DomibusEArchiveException("EArchive batch not found for batchId: [" + entityId + "]");
        }
        return eArchiveBatchByBatchId;
    }

    public void setStatus(EArchiveBatchEntity eArchiveBatchByBatchId, EArchiveBatchStatus status) {
        eArchiveBatchDao.setStatus(eArchiveBatchByBatchId, status, null);
    }

    public void setStatus(EArchiveBatchEntity eArchiveBatchByBatchId, EArchiveBatchStatus status, String error) {
        eArchiveBatchDao.setStatus(eArchiveBatchByBatchId, status, error);
    }

    public void sendToNotificationQueue(EArchiveBatchEntity eArchiveBatchByBatchId, EArchiveBatchStatus type) {
        jmsManager.sendMessageToQueue(JMSMessageBuilder
                .create()
                .property(MessageConstants.BATCH_ID, eArchiveBatchByBatchId.getBatchId())
                .property(MessageConstants.BATCH_ENTITY_ID, "" + eArchiveBatchByBatchId.getEntityId())
                .property(MessageConstants.NOTIFICATION_TYPE, type.name())
                .build(), eArchiveNotificationQueue);
    }
}
