package eu.domibus.core.earchive.alerts;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.EventProperties;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchivingEventService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(EArchivingEventService.class);

    //    private final ArchivingMessagesNonFinalStatusConfigurationManager archivingMessagesNonFinalStatusConfigurationManager;
//    private final ArchivingStartDateStoppedConfigurationManager archivingStartDateStoppedModuleConfiguration;
    private final EventService eventService;

    public EArchivingEventService(
//            ArchivingMessagesNonFinalStatusConfigurationManager archivingMessagesNonFinalStatusConfigurationManager,
//                                  ArchivingStartDateStoppedConfigurationManager archivingStartDateStoppedConfigurationManager,
            EventService eventService) {
//        this.archivingMessagesNonFinalStatusConfigurationManager = archivingMessagesNonFinalStatusConfigurationManager;
//        this.archivingStartDateStoppedModuleConfiguration = archivingStartDateStoppedConfigurationManager;
        this.eventService = eventService;
    }

    public void sendEventMessageNotFinal(String messageId, MessageStatus messageStatus) {
//        ArchivingMessagesNonFinalModuleConfiguration alertConfiguration = archivingMessagesNonFinalStatusConfigurationManager.getConfiguration();
        DefaultAlertConfiguration alertConfiguration = (DefaultAlertConfiguration) AlertType.ARCHIVING_MESSAGES_NON_FINAL.getConfiguration();
        if (!alertConfiguration.isActive()) {
            LOG.debug("E-Archiving messages not final alerts module is not enabled, no alert will be created");
            return;
        }

        LOG.debug("Creating Alert for message [{}] status [{}]", messageId, messageStatus);
        eventService.enqueueEvent(EventType.ARCHIVING_MESSAGES_NON_FINAL, new EventProperties(messageId, messageStatus.name()));
    }

    public void sendEventStartDateStopped() {
//        ArchivingStartDateStoppedModuleConfiguration alertConfiguration = archivingStartDateStoppedModuleConfiguration.getConfiguration();
        DefaultAlertConfiguration alertConfiguration = (DefaultAlertConfiguration) AlertType.ARCHIVING_START_DATE_STOPPED.getConfiguration();
        if (!alertConfiguration.isActive()) {
            LOG.debug("E-Archiving messages not final alerts module is not enabled, no alert will be created");
            return;
        }

        LOG.debug("Creating Alert for continuous job start date stopped");
        eventService.enqueueEvent(EventType.ARCHIVING_START_DATE_STOPPED, new EventProperties());
    }
}
