 package eu.domibus.core.message;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@Service(value = "userMessageLogInfoFilter")
public class UserMessageLogInfoFilter extends MessageLogInfoFilter {

    @Override
    public String filterMessageLogQuery(String column, boolean asc, Map<String, Object> filters) {
        String query = "select new eu.domibus.core.message.MessageLogInfo(" +
                "message.messageId," +
                "log.messageStatus.messageStatus," +
                "log.notificationStatus.status," +
                "log.mshRole.role," +
                "log.deleted," +
                "log.received," +
                "log.sendAttempts," +
                "log.sendAttemptsMax," +
                "log.nextAttempt," +
                "timezoneOffset.nextAttemptTimezoneId," +
                "timezoneOffset.nextAttemptOffsetSeconds," +
                "message.conversationId," +
                "partyFrom.value," +
                "partyTo.value," +
                (isFourCornerModel() ? "propsFrom.value," : "'',") +
                (isFourCornerModel() ? "propsTo.value," : "'',") +
                "message.refToMessageId," +
                "log.failed," +
                "log.restored," +
                "message.testMessage," +
                "message.messageFragment," +
                "message.sourceMessage," +
                MESSAGE_COLLABORATION_INFO_ACTION + "," +
                MESSAGE_COLLABORATION_INFO_SERVICE_TYPE + "," +
                MESSAGE_COLLABORATION_INFO_SERVICE_VALUE +
                ")" + getQueryBody(filters);
        StringBuilder result = filterQuery(query, column, asc, filters);
        return result.toString();
    }

    /**
     * Constructs the query body based on different conditions
     *
     * @return String query body
     */
    @Override
    public String getQueryBody(Map<String, Object> filters) {
        return
                " from UserMessageLog log " +
                        "join log.userMessage message " +
                        (isFourCornerModel() ?
                                "left join message.messageProperties propsFrom "  +
                                "left join message.messageProperties propsTo " : StringUtils.EMPTY) +
                        "left join log.timezoneOffset timezoneOffset " +
                        "left join message.partyInfo.from.partyId partyFrom " +
                        "left join message.partyInfo.to.partyId partyTo " +
                        (isFourCornerModel() ?
                                "where propsFrom.name = 'originalSender' "  +
                                "and propsTo.name = 'finalRecipient' " : StringUtils.EMPTY);

    }


    @Override
    protected String getMainTable() {
        return "UserMessageLog log ";
    }

    @Override
    protected Map<String, List<String>> createFromMappings() {
        Map<String, List<String>> mappings = new HashMap<>();
        String messageTable = " join log.userMessage message ";

        mappings.put("message", Arrays.asList(messageTable));
       // mappings.put("info", Arrays.asList(messageTable));
        mappings.put("propsFrom", Arrays.asList(messageTable, "left join message.messageProperties.property propsFrom "));
        mappings.put("propsTo", Arrays.asList(messageTable, "left join message.messageProperties.property propsTo "));
        mappings.put("partyFrom", Arrays.asList(messageTable, "left join message.partyInfo.from.partyId partyFrom "));
        mappings.put("partyTo", Arrays.asList(messageTable, "left join message.partyInfo.to.partyId partyTo "));
        return mappings;
    }

    @Override
    protected Map<String, List<String>> createWhereMappings() {
        Map<String, List<String>> mappings = new HashMap<>();
        String messageCriteria = "1=1" ; // "message.messageInfo = info ";
        mappings.put("message", Arrays.asList(messageCriteria));
        mappings.put("propsFrom", Arrays.asList(messageCriteria, "and propsFrom.name = 'originalSender' "));
        mappings.put("propsTo", Arrays.asList(messageCriteria, "and propsTo.name = 'finalRecipient' "));
        return mappings;
    }

}
