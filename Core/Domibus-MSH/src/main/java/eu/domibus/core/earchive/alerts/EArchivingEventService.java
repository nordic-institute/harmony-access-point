package eu.domibus.core.earchive.alerts;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.alerts.configuration.common.AlertModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.EventProperties;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

/**
 * @author François Gautier
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class EArchivingEventService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(EArchivingEventService.class);

    private final EventService eventService;

    public EArchivingEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void sendEventMessageNotFinal(String messageId, MessageStatus messageStatus) {
        eventService.enqueueEvent(EventType.ARCHIVING_MESSAGES_NON_FINAL, messageId, new EventProperties(messageId, messageStatus.name()));
        LOG.debug("Creating Alert for message [{}] status [{}]", messageId, messageStatus);
    }

    public void sendEventStartDateStopped() {
        EventType eventType = EventType.ARCHIVING_START_DATE_STOPPED;
        eventService.enqueueEvent(eventType, eventType.name(), new EventProperties());
        LOG.debug("Creating Alert for continuous job start date stopped");
    }

    public void sendEventExportFailed(String batchId, Long entityId, String message) {
        eventService.enqueueEvent(EventType.ARCHIVING_MESSAGE_EXPORT_FAILED, entityId.toString(), new EventProperties(batchId, entityId, message));
        LOG.debug("Creating Alert for message export failed.");
    }
}
