package eu.domibus.core.message;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Tiago Miguel
 * @author Perpegel Ion
 * @since 3.3
 */
@Service(value = "userMessageLogInfoFilter")
public class UserMessageLogInfoFilter extends MessageLogInfoFilter {

    @Override
    public String getFilterMessageLogQuery(String column, boolean asc, Map<String, Object> filters, List<String> fields) {
        String query = "select new eu.domibus.core.message.MessageLogInfo(" +
                "message.messageId," +
                LOG_MESSAGE_STATUS + ".entityId," +
                LOG_NOTIFICATION_STATUS + ".entityId," +
                LOG_MSH_ROLE + ".entityId," +
                "log.deleted," +
                "log.received," +
                "log.sendAttempts," +
                "log.sendAttemptsMax," +
                "log.nextAttempt," +
                "log.timezoneOffset.entityId," +
                "message.conversationId," +
                MESSAGE_PARTY_INFO_FROM_FROM_PARTY_ID + ".entityId," +
                MESSAGE_PARTY_INFO_TO_TO_PARTY_ID + ".entityId," +
                (fields.contains(ORIGINAL_SENDER) ? PROPS_FROM_VALUE + "," : "'',") +
                (fields.contains(FINAL_RECIPIENT) ? PROPS_TO_VALUE + "," : "'',") +
                "message.refToMessageId," +
                "log.failed," +
                "log.restored," +
                "message.testMessage," +
                "message.messageFragment," +
                "message.sourceMessage," +
                (fields.contains(MESSAGE_ACTION) ? MESSAGE_COLLABORATION_INFO_ACTION + ".entityId," : "0L,") +
                (fields.contains(MESSAGE_SERVICE_TYPE) || fields.contains(MESSAGE_SERVICE_VALUE)
                        ? MESSAGE_COLLABORATION_INFO_SERVICE + ".entityId," : "0L,") +
                "log.backend," +
                "0L," +
                "log.archived" +
                ")" +
                getQueryBody(filters, fields);
        StringBuilder result = filterQuery(query, column, asc, filters);
        return result.toString();
    }

    protected String getBaseQueryBody() {
        return " from UserMessageLog log " +
                "join log.userMessage message ";
    }


    @Override
    protected String getMainTable() {
        return "UserMessageLog log ";
    }

    protected String getMessageTable() {
        return " join log.userMessage message ";
    }

}
