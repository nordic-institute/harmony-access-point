package eu.domibus.plugin.webService.backend;

/**
 * @author François Gautier
 * @since 5.0
 */
public enum WSBackendMessageType {

    /**
     * Notify C4 of a domibus message received successfully
     */
    RECEIVE_SUCCESS,

    /**
     * Notify C4 of a domibus message received failure
     */
    RECEIVE_FAIL,

    /**
     * Notify C1 of a domibus message sent successfully
     */
    SEND_SUCCESS,

    /**
     * Notify C1 of a domibus message send failure
     */
    SEND_FAILURE,

    /**
     * Notify C1 and/or C4 of a domibus message status change
     */
    MESSAGE_STATUS_CHANGE,

    /**
     * Submit domibus message to C4
     */
    SUBMIT_MESSAGE,

    /**
     * Notify C1 of a domibus message deleted
     */
    DELETED
}
