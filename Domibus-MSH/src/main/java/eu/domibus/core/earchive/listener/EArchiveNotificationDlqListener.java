package eu.domibus.core.earchive.listener;

import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.alerts.configuration.earchive.ArchivingNotificationFailedConfigurationManager;
import eu.domibus.core.alerts.configuration.earchive.ArchivingNotificationFailedModuleConfiguration;
import eu.domibus.core.alerts.service.EventService;
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
public class EArchiveNotificationDlqListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveNotificationDlqListener.class);

    private final DatabaseUtil databaseUtil;

    private final EArchivingDefaultService eArchiveService;

    private final JmsUtil jmsUtil;

    private final ArchivingNotificationFailedConfigurationManager archivingNotificationFailedConfigurationManager;

    private final EventService eventService;

    public EArchiveNotificationDlqListener(
            DatabaseUtil databaseUtil,
            EArchivingDefaultService eArchiveService,
            JmsUtil jmsUtil, ArchivingNotificationFailedConfigurationManager archivingNotificationFailedConfigurationManager, EventService eventService) {
        this.databaseUtil = databaseUtil;
        this.eArchiveService = eArchiveService;
        this.jmsUtil = jmsUtil;
        this.archivingNotificationFailedConfigurationManager = archivingNotificationFailedConfigurationManager;
        this.eventService = eventService;
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

        ArchivingNotificationFailedModuleConfiguration alertConfiguration = archivingNotificationFailedConfigurationManager.getConfiguration();
        if (!alertConfiguration.isActive()) {
            LOG.debug("E-Archiving notification failed alerts module is not enabled, no alert will be created");
            return;
        }
        EArchiveBatchStatus notificationType = EArchiveBatchStatus.valueOf(jmsUtil.getStringPropertySafely(message, MessageConstants.NOTIFICATION_TYPE));
        EArchiveBatchEntity eArchiveBatchByBatchId = eArchiveService.getEArchiveBatch(entityId);

        LOG.debug("Creating Alert for batch [{}] [{}]", notificationType, eArchiveBatchByBatchId);
        eventService.enqueueEArchivingEvent(batchId, notificationType);
    }

}
