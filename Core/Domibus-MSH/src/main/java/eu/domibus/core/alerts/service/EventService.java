package eu.domibus.core.alerts.service;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.user.UserEntityBase;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.model.service.EventProperties;

import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface EventService {

    void enqueueEvent(EventType eventType, String eventIdentifier, EventProperties eventProperties);

    /**
     * Will create a message status change event with the given parameter and enqueue it to the alert/event monitoring queue.
     *
     * @param messageId the id of the monitored message.
     * @param oldStatus the old status of the message.
     * @param newStatus the new status of the message.
     * @param role      the role of the access point.
     */
    void enqueueMessageStatusChangedEvent(String messageId, MessageStatus oldStatus, MessageStatus newStatus, MSHRole role);

    /**
     * Will create a certificate imminent expiration event and enqueue it to the alert/event monitoring queue.
     *
     * @param alias          the alias of the certificate.
     * @param expirationDate the expiration date.
     */
    void enqueueImminentCertificateExpirationEvent(String alias, Date expirationDate);

    /**
     * Will create a certificate expired event and enqueue it to the alert/event monitoring queue.
     *
     * @param alias          the alias of the certificate.
     * @param expirationDate the expiration date.
     */
    void enqueueCertificateExpiredEvent(String alias, Date expirationDate);

    /**
     * Save an event.
     *
     * @param event the event to save.
     */
    eu.domibus.core.alerts.model.persist.Event persistEvent(Event event);

    /**
     * Will create an expiration event and enqueue it to the alert/event monitoring queue.
     *
     * @param eventType            the specific type of expiration event: expired and imminent expiration for console or plugin users
     * @param user                 the user for which the event is triggered
     * @param maxPasswordAgeInDays the number of days the password is not expired
     */
    void enqueuePasswordExpirationEvent(EventType eventType, UserEntityBase user, Integer maxPasswordAgeInDays);


    /**
     * Verifies if an alert should be created based on the last alert and frequency
     *
     * @param event     the event for which the alert should be or not created
     * @param frequency the period in days to send another alert
     * @return true if the previous alert is older than the specified frequency
     */
    boolean shouldCreateAlert(eu.domibus.core.alerts.model.persist.Event event, int frequency);

    eu.domibus.core.alerts.model.persist.Event getOrCreatePersistedEvent(Event event);

    void enqueueMonitoringEvent(String messageId, MSHRole role, MessageStatus messageStatus, MessageStatus newStatus, String fromParty, String toParty);
}
