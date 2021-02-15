package eu.domibus.core.message;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@Service(value = "userMessageLogInfoFilter")
public class UserMessageLogInfoFilter extends MessageLogInfoFilter {

    @Override
    public String filterMessageLogQuery(String column, boolean asc, Map<String, Object> filters) {
        String query = "select new eu.domibus.core.message.MessageLogInfo(" +
                "log.messageId," +
                "log.messageStatus," +
                "log.notificationStatus," +
                "log.mshRole," +
                "log.messageType," +
                "log.deleted," +
                "log.received," +
                "log.sendAttempts," +
                "log.sendAttemptsMax," +
                "log.nextAttempt," +
                "message.collaborationInfo.conversationId," +
                "partyFrom.value," +
                "partyTo.value," +
                (isFourCornerModel() ? "propsFrom.value," : "'',") +
                (isFourCornerModel() ? "propsTo.value," : "'',") +
                "info.refToMessageId," +
                "log.failed," +
                "log.restored," +
                "log.messageSubtype," +
                "log.messageFragment," +
                "log.sourceMessage" +
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
    public String getQueryBody(Map<String, Object> allFilters) {
        return " from UserMessageLog log, " +
                "UserMessage message " +
                "left join log.messageInfo info " +
                (isFourCornerModel() ?
                        "left join message.messageProperties.property propsFrom " +
                                "left join message.messageProperties.property propsTo "
                        : StringUtils.EMPTY) +
                "left join message.partyInfo.from.partyId partyFrom " +
                "left join message.partyInfo.to.partyId partyTo " +

                "where message.messageInfo = info " +
                (isFourCornerModel() ?
                        "and propsFrom.name = 'originalSender' " +
                                "and propsTo.name = 'finalRecipient' "
                        : StringUtils.EMPTY);
    }

    public String getCountQueryBody(Map<String, Object> allFilters) {
        final Map<String, Object> filters = Maps.filterEntries(allFilters, input -> input.getValue() != null);

        Map<String, List<String>> fromMappings = createFromMappings();
        StringBuilder query = new StringBuilder(" from UserMessageLog log ");
        Set<String> added = new HashSet<>();

        filters.keySet().stream().forEach(param -> {
            String hqlKey = getHQLKey(param);
            if (StringUtils.isEmpty(hqlKey)) {
                return;
            }
            String table = hqlKey.substring(0, hqlKey.indexOf("."));

            if (added.add(table)) {
                if (fromMappings.containsKey(table)) {
                    fromMappings.get(table).forEach(el -> {
                        if (query.indexOf(el) < 0) {
                            query.append(el);
                        }
                    });
                }
            }
        });

        Map<String, List<String>> whereMappings = createWhereMappings();
        StringBuilder query2 = new StringBuilder();
        Set<String> added2 = new HashSet<>();
        whereMappings.keySet().stream().forEach(table -> {
            if (added2.add(table)) {
                if (query.indexOf(table) >= 0) {
                    whereMappings.get(table).forEach(el -> {
                        if (query2.indexOf(el) < 0) {
                            query2.append(el);
                        }
                    });
                }
            }
        });

        if (StringUtils.isBlank(query2.toString())) {
            return query.toString();
        } else {
            return query.append(" where ").append(query2).toString();
        }
    }

    private Map<String, List<String>> createFromMappings() {
        Map<String, List<String>> mappings = new HashMap<>();
        String messageTable = ", UserMessage message left join log.messageInfo info ";
        mappings.put("message", Arrays.asList(messageTable));
        mappings.put("info", Arrays.asList(messageTable));
        mappings.put("propsFrom", Arrays.asList(messageTable, "left join message.messageProperties.property propsFrom "));
        mappings.put("propsTo", Arrays.asList(messageTable, "left join message.messageProperties.property propsTo "));
        mappings.put("partyFrom", Arrays.asList(messageTable, "left join message.partyInfo.from.partyId partyFrom "));
        mappings.put("partyTo", Arrays.asList(messageTable, "left join message.partyInfo.to.partyId partyTo "));
        return mappings;
    }

    private Map<String, List<String>> createWhereMappings() {
        Map<String, List<String>> mappings = new HashMap<>();
        String messageCriteria = "message.messageInfo = info ";
        mappings.put("message", Arrays.asList(messageCriteria));
        mappings.put("propsFrom", Arrays.asList(messageCriteria, "and propsFrom.name = 'originalSender' "));
        mappings.put("propsTo", Arrays.asList(messageCriteria, "and propsTo.name = 'finalRecipient' "));
        return mappings;
    }
}
