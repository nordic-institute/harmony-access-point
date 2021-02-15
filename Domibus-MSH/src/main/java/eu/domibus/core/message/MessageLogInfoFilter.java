package eu.domibus.core.message;

import com.google.common.collect.Maps;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.TypedQuery;
import java.util.*;

/**
 * @author Tiago Miguel
 * @author Ion Perpegel
 * @since 3.3
 */
public abstract class MessageLogInfoFilter {

    private static final String LOG_MESSAGE_ID = "log.messageId";
    private static final String LOG_MSH_ROLE = "log.mshRole";
    private static final String LOG_MESSAGE_TYPE = "log.messageType";
    private static final String LOG_MESSAGE_STATUS = "log.messageStatus";
    private static final String LOG_NOTIFICATION_STATUS = "log.notificationStatus";
    private static final String LOG_DELETED = "log.deleted";
    private static final String LOG_RECEIVED = "log.received";
    private static final String LOG_SEND_ATTEMPTS = "log.sendAttempts";
    private static final String LOG_SEND_ATTEMPTS_MAX = "log.sendAttemptsMax";
    private static final String LOG_NEXT_ATTEMPT = "log.nextAttempt";
    private static final String PARTY_FROM_VALUE = "partyFrom.value";
    private static final String PARTY_TO_VALUE = "partyTo.value";
    private static final String INFO_REF_TO_MESSAGE_ID = "info.refToMessageId";
    private static final String PROPS_FROM_VALUE = "propsFrom.value";
    private static final String PROPS_TO_VALUE = "propsTo.value";
    private static final String MESSAGE_COLLABORATION_INFO_CONVERSATION_ID = "message.collaborationInfo.conversationId";
    private static final String LOG_FAILED = "log.failed";
    private static final String LOG_RESTORED = "log.restored";
    private static final String LOG_MESSAGE_SUBTYPE = "log.messageSubtype";

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    protected String getHQLKey(String originalColumn) {
        switch (originalColumn) {
            case "messageId":
                return LOG_MESSAGE_ID;
            case "mshRole":
                return LOG_MSH_ROLE;
            case "messageType":
                return LOG_MESSAGE_TYPE;
            case "messageStatus":
                return LOG_MESSAGE_STATUS;
            case "notificationStatus":
                return LOG_NOTIFICATION_STATUS;
            case "deleted":
                return LOG_DELETED;
            case "received":
                return LOG_RECEIVED;
            case "sendAttempts":
                return LOG_SEND_ATTEMPTS;
            case "sendAttemptsMax":
                return LOG_SEND_ATTEMPTS_MAX;
            case "nextAttempt":
                return LOG_NEXT_ATTEMPT;
            case "fromPartyId":
                return PARTY_FROM_VALUE;
            case "toPartyId":
                return PARTY_TO_VALUE;
            case "refToMessageId":
                return INFO_REF_TO_MESSAGE_ID;
            case "originalSender":
                return PROPS_FROM_VALUE;
            case "finalRecipient":
                return PROPS_TO_VALUE;
            case "conversationId":
                return MESSAGE_COLLABORATION_INFO_CONVERSATION_ID;
            case "failed":
                return LOG_FAILED;
            case "restored":
                return LOG_RESTORED;
            case "messageSubtype":
                return LOG_MESSAGE_SUBTYPE;
            default:
                return "";
        }
    }

    protected StringBuilder filterQuery(String query, String column, boolean asc, Map<String, Object> filters) {
        StringBuilder result = new StringBuilder(query);
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            handleFilter(result, query, filter);
        }

        if (column != null) {
            String usedColumn = getHQLKey(column);
            if (asc) {
                result.append(" order by ").append(usedColumn).append(" asc");
            } else {
                result.append(" order by ").append(usedColumn).append(" desc");
            }
        }

        return result;
    }

    private void handleFilter(StringBuilder result, String query, Map.Entry<String, Object> filter) {
        if (filter.getValue() != null) {
            setSeparator(query, result);
            if (!(filter.getValue() instanceof Date)) {
                if (!(filter.getValue().toString().isEmpty())) {
                    String tableName = getHQLKey(filter.getKey());
                    result.append(tableName).append(" = :").append(filter.getKey());
                }
            } else {
                if (!(filter.getValue().toString().isEmpty())) {
                    String s = filter.getKey();
                    if (s.equals("receivedFrom")) {
                        result.append(LOG_RECEIVED).append(" >= :").append(filter.getKey());
                    } else if (s.equals("receivedTo")) {
                        result.append(LOG_RECEIVED).append(" <= :").append(filter.getKey());
                    }
                }
            }
        } else {
            if (filter.getKey().equals("messageSubtype")) {
                setSeparator(query, result);
                String tableName = getHQLKey(filter.getKey());
                result.append(tableName).append(" is null");
            }
        }
    }

    private void setSeparator(String query, StringBuilder result) {
        if (query.contains("where") || result.toString().contains("where")) {
            result.append(" and ");
        } else {
            result.append(" where ");
        }
    }

    public <E> TypedQuery<E> applyParameters(TypedQuery<E> query, Map<String, Object> filters) {
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                query.setParameter(filter.getKey(), filter.getValue());
            }
        }
        return query;
    }

    /**
     * in four corner model finalRecipient and originalSender exists as default properties
     *
     * @return true by default
     */
    public boolean isFourCornerModel() {
        return domibusPropertyProvider.getBooleanProperty(DomibusConfigurationService.FOURCORNERMODEL_ENABLED_KEY);
    }

    public String filterMessageLogQuery(String column, boolean asc, Map<String, Object> filters) {
        return null;
    }

    public abstract String getQueryBody(Map<String, Object> filters);

    public String getCountMessageLogQuery(Map<String, Object> filters) {
        String expression = "select count(log.id)";
        return getQuery(filters, expression);
    }

    public String getMessageLogIdQuery(Map<String, Object> filters) {
        String expression = "select log.id ";
        return getQuery(filters, expression);
    }

    protected String getQuery(Map<String, Object> filters, String expression) {
        String query = expression + getCountQueryBody(filters);
        StringBuilder result = filterQuery(query, null, false, filters);
        return result.toString();
    }

    public String getCountQueryBody(Map<String, Object> allFilters) {
        final Map<String, Object> filters = Maps.filterEntries(allFilters, input -> input.getValue() != null);

        StringBuilder fromQuery = createFromClause(filters);

        StringBuilder whereQuery = createWhereQuery(fromQuery);

        if (StringUtils.isBlank(whereQuery.toString())) {
            return fromQuery.toString();
        }
        return fromQuery.append(" where ").append(whereQuery).toString();
    }

    protected StringBuilder createFromClause(Map<String, Object> filters) {
        Map<String, List<String>> fromMappings = createFromMappings();
        StringBuilder query = new StringBuilder(" from " + getMainTable());
        Set<String> added = new HashSet<>();

        filters.keySet().stream().forEach(filterParam -> {
            String hqlKey = getHQLKey(filterParam);
            if (StringUtils.isEmpty(hqlKey)) {
                return;
            }
            String table = hqlKey.substring(0, hqlKey.indexOf("."));

            if (added.add(table)) {
                if (fromMappings.containsKey(table)) {
                    fromMappings.get(table).forEach(mapping -> {
                        if (query.indexOf(mapping) < 0) {
                            query.append(mapping);
                        }
                    });
                }
            }
        });

        return query;
    }

    protected StringBuilder createWhereQuery(StringBuilder fromQuery) {
        Map<String, List<String>> whereMappings = createWhereMappings();
        StringBuilder query = new StringBuilder();
        Set<String> added = new HashSet<>();
        whereMappings.keySet().stream().forEach(table -> {
            if (added.add(table)) {
                if (fromQuery.indexOf(table) >= 0) {
                    whereMappings.get(table).forEach(mapping -> {
                        if (query.indexOf(mapping) < 0) {
                            query.append(mapping);
                        }
                    });
                }
            }
        });
        return query;
    }

    protected abstract String getMainTable();

    protected abstract Map<String, List<String>> createFromMappings();

    protected abstract Map<String, List<String>> createWhereMappings();
}
