package eu.domibus.messaging;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
public class MessageConstants {

    private MessageConstants() {}

    public static final String MESSAGE_ID = "MESSAGE_ID";
    public static final String DOMAIN = "DOMAIN";
    public static final String ENDPOINT = "ENDPOINT";
    public static final String DELAY = "DELAY";
    public static final String NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    public static final String ORIGINAL_SENDER = "originalSender";
    public static final String FINAL_RECIPIENT = "finalRecipient";
    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_DETAIL = "errorDetail";
    public static final String RETRY_COUNT = "RETRY_COUNT";
    public static final String FILE_NAME = "FILE_NAME";
    public static final String STATUS_FROM = "fromStatus";
    public static final String STATUS_TO = "toStatus";
    public static final String CHANGE_TIMESTAMP = "changeTimestamp";
    public static final String SERVICE = "service";
    public static final String SERVICE_TYPE = "serviceType";
    public static final String ACTION = "action";


    /**
     * we used this attribute name and not FileName to avoid name collision with Domibus core class SubmissionAS4Transformer
     */
    public static final String PAYLOAD_PROPERTY_FILE_NAME = "PayloadName";
}
