package eu.domibus.core.earchive.listener;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.alerts.configuration.AlertModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.EventProperties;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.earchive.EArchiveBatchEntity;
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

//    private final ArchivingNotificationFailedConfigurationManager archivingNotificationFailedConfigurationManager;

    private final EventService eventService;

    private final AlertConfigurationService alertConfigurationService;

    public EArchiveNotificationDlqListener(
            DatabaseUtil databaseUtil,
            EArchivingDefaultService eArchiveService,
            JmsUtil jmsUtil,
//            ArchivingNotificationFailedConfigurationManager archivingNotificationFailedConfigurationManager,
            EventService eventService, AlertConfigurationService alertConfigurationService) {
        this.databaseUtil = databaseUtil;
        this.eArchiveService = eArchiveService;
        this.jmsUtil = jmsUtil;
//        this.archivingNotificationFailedConfigurationManager = archivingNotificationFailedConfigurationManager;
        this.eventService = eventService;
        this.alertConfigurationService = alertConfigurationService;
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

        LOG.info("Notification failed for batchId [{}] and entityId [{}]", batchId, entityId);

//        ArchivingNotificationFailedModuleConfiguration alertConfiguration = archivingNotificationFailedConfigurationManager.getConfiguration();
        AlertModuleConfiguration alertConfiguration = alertConfigurationService.getConfiguration(AlertType.ARCHIVING_NOTIFICATION_FAILED);
        if (!alertConfiguration.isActive()) {
            LOG.debug("E-Archiving notification failed alerts module is not enabled, no alert will be created");
            return;
        }
        EArchiveBatchStatus notificationType = EArchiveBatchStatus.valueOf(jmsUtil.getStringPropertySafely(message, MessageConstants.NOTIFICATION_TYPE));
        EArchiveBatchEntity eArchiveBatchByBatchId = eArchiveService.getEArchiveBatch(entityId, false);

        LOG.debug("Creating Alert for batch [{}] [{}]", notificationType, eArchiveBatchByBatchId);
        eventService.enqueueEvent(EventType.ARCHIVING_NOTIFICATION_FAILED, batchId, new EventProperties(batchId, notificationType.name()));
    }

}
