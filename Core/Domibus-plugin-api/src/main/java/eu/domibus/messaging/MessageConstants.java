package eu.domibus.messaging;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
public final class MessageConstants {

    private MessageConstants() {}

    public static final String MESSAGE_ID = "MESSAGE_ID";
    public static final String MESSAGE_ENTITY_ID = "MESSAGE_ENTITY_ID";
    public static final String BATCH_ID = "BATCH_ID";
    public static final String BATCH_ENTITY_ID = "BATCH_ENTITY_ID";
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
    public static final String CONVERSATION_ID = "conversationId";
    public static final String REF_TO_MESSAGE_ID = "refToMessageId";
    public static final String FROM_PARTY_ID = "fromPartyId";
    public static final String FROM_PARTY_TYPE = "fromPartyType";
    public static final String FROM_PARTY_ROLE = "fromPartyRole";
    public static final String TO_PARTY_ID = "toPartyId";
    public static final String TO_PARTY_TYPE = "toPartyType";
    public static final String TO_PARTY_ROLE = "toPartyRole";

    public static final String COMPRESSION_PROPERTY_KEY = "CompressionType";
    public static final String COMPRESSION_PROPERTY_VALUE = "application/gzip";


    /**
     * we used this attribute name and not FileName to avoid name collision with Domibus core class SubmissionAS4Transformer
     */
    public static final String PAYLOAD_PROPERTY_FILE_NAME = "PayloadName";

    public static final String PAYLOAD_PROPERTY_FILEPATH = "FilePath";
}
