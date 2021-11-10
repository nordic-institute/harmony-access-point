package eu.domibus.core.earchive.alerts;

import eu.domibus.api.model.MessageStatus;
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

    private final ArchivingMessagesNonFinalStatusConfigurationManager archivingMessagesNonFinalStatusConfigurationManager;
    private final EventService eventService;

    public EArchivingEventService(ArchivingMessagesNonFinalStatusConfigurationManager archivingMessagesNonFinalStatusConfigurationManager,
                                  EventService eventService) {
        this.archivingMessagesNonFinalStatusConfigurationManager = archivingMessagesNonFinalStatusConfigurationManager;
        this.eventService = eventService;
    }

    public void sendEvent(String messageId, MessageStatus messageStatus) {
        ArchivingMessagesNonFinalModuleConfiguration alertConfiguration = archivingMessagesNonFinalStatusConfigurationManager.getConfiguration();
        if (!alertConfiguration.isActive()) {
            LOG.debug("E-Archiving messages not final alerts module is not enabled, no alert will be created");
            return;
        }

        LOG.debug("Creating Alert for message [{}] status [{}]", messageId, messageStatus);
        eventService.enqueueEArchivingMessageNonFinalEvent(messageId, messageStatus);
    }
}
