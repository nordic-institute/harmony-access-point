package eu.domibus.web.rest;

import eu.domibus.api.util.DateUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.MessageLogFilterRequestRO;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;

import static eu.domibus.core.message.MessageLogInfoFilter.*;

@Component
public class RequestFilterUtils {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RequestFilterUtils.class);

    public static final int DEFAULT_MESSAGES_SEARCH_INTERVAL_IN_MINUTES = 60;
    private static final String PROPERTY_CONVERSATION_ID = "conversationId";
    private static final String PROPERTY_FINAL_RECIPIENT = "finalRecipient";
    private static final String PROPERTY_FROM_PARTY_ID = "fromPartyId";
    private static final String PROPERTY_MESSAGE_ID = "messageId";
    private static final String PROPERTY_MESSAGE_STATUS = "messageStatus";
    private static final String PROPERTY_TEST_MESSAGE = "testMessage";
    private static final String PROPERTY_MSH_ROLE = "mshRole";
    private static final String PROPERTY_NOTIFICATION_STATUS = "notificationStatus";
    private static final String PROPERTY_ORIGINAL_SENDER = "originalSender";
    private static final String PROPERTY_RECEIVED_FROM = "receivedFrom";
    private static final String PROPERTY_RECEIVED_TO = "receivedTo";
    private static final String PROPERTY_MIN_ENTITY_ID = "minEntityId";
    private static final String PROPERTY_MAX_ENTITY_ID = "maxEntityId";
    private static final String PROPERTY_REF_TO_MESSAGE_ID = "refToMessageId";
    private static final String PROPERTY_TO_PARTY_ID = "toPartyId";

    private final DateUtil dateUtil;

    public RequestFilterUtils(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }

    protected HashMap<String, Object> createFilterMap(MessageLogFilterRequestRO request) {
        HashMap<String, Object> filters = new HashMap<>();
        filters.put(PROPERTY_MESSAGE_ID, request.getMessageId());
        filters.put(PROPERTY_CONVERSATION_ID, request.getConversationId());
        filters.put(PROPERTY_MSH_ROLE, request.getMshRole());
        filters.put(PROPERTY_MESSAGE_STATUS, request.getMessageStatus());
        filters.put(PROPERTY_NOTIFICATION_STATUS, request.getNotificationStatus());
        filters.put(PROPERTY_FROM_PARTY_ID, request.getFromPartyId());
        filters.put(PROPERTY_TO_PARTY_ID, request.getToPartyId());
        filters.put(PROPERTY_REF_TO_MESSAGE_ID, request.getRefToMessageId());
        filters.put(PROPERTY_ORIGINAL_SENDER, request.getOriginalSender());
        filters.put(PROPERTY_FINAL_RECIPIENT, request.getFinalRecipient());
        filters.put(PROPERTY_TEST_MESSAGE, request.getTestMessage());
        filters.put(MESSAGE_ACTION, request.getAction());
        filters.put(MESSAGE_SERVICE_TYPE, request.getServiceType());
        filters.put(MESSAGE_SERVICE_VALUE, request.getServiceValue());
        return filters;
    }

    protected void setDefaultFilters(MessageLogFilterRequestRO request, HashMap<String, Object> filters) {
        //we just set default values for received column
        // in order to improve pagination on large amount of data
        Date from = dateUtil.fromString(request.getReceivedFrom());
        if (from == null) {
            from = Date.from(java.time.ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(DEFAULT_MESSAGES_SEARCH_INTERVAL_IN_MINUTES).toInstant());
        }
        Date to = dateUtil.fromString(request.getReceivedTo());
        if (to == null) {
            to = Date.from(java.time.ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        }
        filters.put(PROPERTY_RECEIVED_FROM, from);
        filters.put(PROPERTY_RECEIVED_TO, to);

        filters.put(PROPERTY_MIN_ENTITY_ID, from);
        filters.put(PROPERTY_MAX_ENTITY_ID, to);

        LOG.debug("using filters [{}]", filters);
    }

}
