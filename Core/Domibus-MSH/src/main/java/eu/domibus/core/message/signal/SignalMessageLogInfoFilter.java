package eu.domibus.core.message.signal;

import eu.domibus.core.message.MessageLogInfoFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tiago Miguel
 * @author Perpegel Ion
 * @since 3.3
 */
@Service(value = "signalMessageLogInfoFilter")
public class SignalMessageLogInfoFilter extends MessageLogInfoFilter {

    private static final String EMPTY_CONVERSATION_ID = "'',";

    @Override
    protected String getHQLKey(String originalColumn) {
        switch (originalColumn) {
            case "conversationId":
            case "notificationStatus":
            case "failed":
            case "restored":
            case "sendAttempts":
            case "sendAttemptsMax":
            case "nextAttempt":
                return "";
            case "messageId":
                return "signal.signalMessageId";
            case "refToMessageId":
                return "signal.refToMessageId";
            default:
                return super.getHQLKey(originalColumn);
        }
    }

    @Override
    protected StringBuilder filterQuery(String query, String column, boolean asc, Map<String, Object> filters) {
        // remove filters not applicable to signal messages:
        filters.keySet().forEach(key -> {
            if (StringUtils.isBlank(getHQLKey(key))) {
                filters.put(key, null);
            }
        });

        return super.filterQuery(query, column, asc, filters);
    }

    @Override
    public String getFilterMessageLogQuery(String column, boolean asc, Map<String, Object> filters, List<String> fields) {
        String query = "select new eu.domibus.core.message.MessageLogInfo(" +
                "signal.signalMessageId," +
                LOG_MESSAGE_STATUS + ".entityId," +
                LOG_MSH_ROLE + ".entityId," +
                "log.deleted," +
                "log.received," +
                EMPTY_CONVERSATION_ID +
                MESSAGE_PARTY_INFO_FROM_FROM_PARTY_ID + ".entityId," +
                MESSAGE_PARTY_INFO_TO_TO_PARTY_ID + ".entityId," +
                (fields.contains(ORIGINAL_SENDER) ? PROPS_FROM_VALUE + "," : "'',") +
                (fields.contains(FINAL_RECIPIENT) ? PROPS_TO_VALUE + "," : "'',") +
                "signal.refToMessageId," +
                "message.testMessage" +
                ")" +
                getQueryBody(filters, fields);
        StringBuilder result = filterQuery(query, column, asc, filters);
        return result.toString();
    }

    protected String getBaseQueryBody() {
        return " from SignalMessageLog log " +
                "join log.signalMessage signal " +
                "join signal.userMessage message ";
    }

    @Override
    protected String getMainTable() {
        return "SignalMessageLog log ";
    }

    @Override
    protected Map<String, List<String>> createFromMappings() {
        Map<String, List<String>> mappings = super.createFromMappings();

        mappings.put("messaging", Arrays.asList(getMessageTable()));
        mappings.put("signal", Arrays.asList(getMessageTable()));

        return mappings;
    }

    protected String getMessageTable() {
        return " join log.signalMessage signal join signal.userMessage message ";
    }

    @Override
    protected Map<String, List<String>> createWhereMappings() {
        Map<String, List<String>> mappings = super.createWhereMappings();

        mappings.put("signal", Arrays.asList(" 1=1 "));

        return mappings;
    }
}
