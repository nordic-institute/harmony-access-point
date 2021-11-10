package eu.domibus.core.alerts.service;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import eu.domibus.core.user.UserEntityBase;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Event;

import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface EventService {

    /**
     * Will create a message status change event with the given parameter and enqueue it to the alert/event monitoring queue.
     *
     * @param messageId the id of the monitored message.
     * @param oldStatus the old status of the message.
     * @param newStatus the new status of the message.
     * @param role      the role of the access point.
     */
    void enqueueMessageEvent(String messageId, MessageStatus oldStatus, MessageStatus newStatus, MSHRole role);

    /**
     * Will create login failure event and enqueue it to the alert/event monitoring queue.
     *
     * @param userName        the user name that had a failure login
     * @param loginTime       the login failure time.
     * @param accountDisabled whether the account has been disable or not.
     */
    void enqueueLoginFailureEvent(UserEntityBase.Type type, String userName, Date loginTime, boolean accountDisabled);

    /**
     * Will create partition expiration event and enqueue it to the alert/event monitoring queue.
     *
     * @param partitionName   the partition name that could not be deleted
     */
    void enqueuePartitionExpirationEvent(String partitionName);

    /**
     * Will create a account disabled event and enqueue it to the alert/event monitoring queue.
     *
     * @param userName            the user name that had a failure login
     * @param accountDisabledTime the account disabled time.
     */
    void enqueueAccountDisabledEvent(UserEntityBase.Type type, String userName, Date accountDisabledTime);

    /**
     * Will create a account enabled event and enqueue it to the alert/event monitoring queue.
     *
     * @param userName            the user name enabled
     * @param accountEnabledTime  the account enabled time.
     */
    void enqueueAccountEnabledEvent(UserEntityBase.Type type, String userName, Date accountEnabledTime);

    /**
     * Will create a certificate imminent expiration event and enqueue it to the alert/event monitoring queue.
     *
     * @param accessPoint    the access point at which the certificate will expire.
     * @param alias          the alias of the certificate.
     * @param expirationDate the expiration date.
     */
    void enqueueImminentCertificateExpirationEvent(String accessPoint, String alias, Date expirationDate);

    /**
     * Will create a certificate expired event and enqueue it to the alert/event monitoring queue.
     *
     * @param accessPoint    the access point at which the certificate will expire.
     * @param alias          the alias of the certificate.
     * @param expirationDate the expiration date.
     */
    void enqueueCertificateExpiredEvent(String accessPoint, String alias, Date expirationDate);

    /**
     * Will create an earchiving notification failed event and enqueue it to the alert/event monitoring queue.
     *
     * @param batchId       the id of the batch that could not be notified to the e-archiving client
     * @param batchStatus   the status of the batch that could not be notified to the e-archiving client
     */
    void enqueueEArchivingEvent(String batchId, EArchiveBatchStatus batchStatus);

    /**
     * Save an event.
     *
     * @param event the event to save.
     */
    eu.domibus.core.alerts.model.persist.Event persistEvent(Event event);

    /**
     * Will enrich a message status change event with potential EBMS error details.
     *
     * @param event the even to enrich.
     */
    void enrichMessageEvent(Event event);

    /**
     * Will create an expiration event and enqueue it to the alert/event monitoring queue.
     *
     * @param eventType            the specific type of expiration event: expired and imminent expiration for console or plugin users
     * @param user                 the user for which the event is triggered
     * @param maxPasswordAgeInDays the number of days the password is not expired
     */
    void enqueuePasswordExpirationEvent(EventType eventType, UserEntityBase user, Integer maxPasswordAgeInDays, PasswordExpirationAlertModuleConfiguration alertConfiguration);


    /**
     * Verifies if an alert should be created based on the last alert and frequency
     *
     * @param event the event for which the alert should be or not created
     * @param frequency the period in days to send another alert
     * @return true if the previous alert is older than the specified frequency
     */
    boolean shouldCreateAlert(eu.domibus.core.alerts.model.persist.Event event, int frequency);

}
