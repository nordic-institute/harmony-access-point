package eu.domibus.core.message;

import com.google.common.collect.Maps;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.TypedQuery;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.*;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public abstract class MessageLogInfoFilter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagesLogServiceHelperImpl.class);

    private static final String LOG_MESSAGE_ENTITY_ID = "log.entityId";
    private static final String LOG_MESSAGE_ID = "message.messageId";
    private static final String LOG_MSH_ROLE = "log.mshRole.role";
    private static final String LOG_MESSAGE_STATUS = "log.messageStatus.messageStatus";
    private static final String LOG_NOTIFICATION_STATUS = "log.notificationStatus.status";
    private static final String LOG_DELETED = "log.deleted";
    private static final String LOG_RECEIVED = "log.received";
    private static final String LOG_SEND_ATTEMPTS = "log.sendAttempts";
    private static final String LOG_SEND_ATTEMPTS_MAX = "log.sendAttemptsMax";
    private static final String LOG_NEXT_ATTEMPT = "log.nextAttempt";
    private static final String PARTY_FROM_VALUE = "partyFrom.value";
    private static final String PARTY_TO_VALUE = "partyTo.value";
    private static final String INFO_REF_TO_MESSAGE_ID = "message.refToMessageId";
    private static final String PROPS_FROM_VALUE = "propsFrom.value";
    private static final String PROPS_TO_VALUE = "propsTo.value";
    private static final String MESSAGE_COLLABORATION_INFO_CONVERSATION_ID = "message.conversationId";
    private static final String LOG_FAILED = "log.failed";
    private static final String LOG_RESTORED = "log.restored";
    private static final String MESSAGE_TEST_MESSAGE = "message.testMessage";
    public static final String MESSAGE_ACTION = "action";
    public static final String MESSAGE_SERVICE_TYPE = "serviceType";
    public static final String MESSAGE_SERVICE_VALUE = "serviceValue";
    public static final String MESSAGE_COLLABORATION_INFO_ACTION = "message.action.value";
    public static final String MESSAGE_COLLABORATION_INFO_SERVICE_TYPE = "message.service.type";
    public static final String MESSAGE_COLLABORATION_INFO_SERVICE_VALUE = "message.service.value";

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    protected String getHQLKey(String originalColumn) {
        switch (originalColumn) {
            case "messageId":
                return LOG_MESSAGE_ID;
            case "mshRole":
                return LOG_MSH_ROLE;
            case "messageStatus":
                return LOG_MESSAGE_STATUS;
            case "notificationStatus":
                return LOG_NOTIFICATION_STATUS;
            case "deleted":
                return LOG_DELETED;
            case "received":
            case "receivedFrom":
            case "receivedTo":
                return LOG_RECEIVED;
            case "minEntityId":
            case "maxEntityId":
                return LOG_MESSAGE_ENTITY_ID;
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
            case "testMessage":
                return MESSAGE_TEST_MESSAGE;
            case MESSAGE_ACTION:
                return MESSAGE_COLLABORATION_INFO_ACTION;
            case MESSAGE_SERVICE_TYPE:
                return MESSAGE_COLLABORATION_INFO_SERVICE_TYPE;
            case MESSAGE_SERVICE_VALUE:
                return MESSAGE_COLLABORATION_INFO_SERVICE_VALUE;
            default:
                return "";
        }
    }

    protected StringBuilder filterQuery(String query, String sortColumn, boolean asc, Map<String, Object> filters) {
        StringBuilder result = new StringBuilder(query);
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            handleFilter(result, query, filter);
        }

        if (sortColumn != null) {
            String usedColumn = getHQLKey(sortColumn);
            if (!StringUtils.isBlank(usedColumn)) {
                result.append(" order by ").append(usedColumn).append(asc ? " asc" : " desc");
            }
        }

        return result;
    }

    private void handleFilter(StringBuilder result, String query, Map.Entry<String, Object> filter) {
        if (filter.getValue() != null) {
            String tableName = getHQLKey(filter.getKey());
            if (StringUtils.isBlank(tableName)) {
                return;
            }

            setSeparator(query, result);
            if (!(filter.getValue() instanceof Date)) {
                if (!(filter.getValue().toString().isEmpty())) {
                    result.append(tableName).append(" = :").append(filter.getKey());
                }
            } else {
                if (!(filter.getValue().toString().isEmpty())) {
                    String s = filter.getKey();
                    if (s.equals("receivedFrom") || s.equals("minEntityId")) {
                        result.append(tableName).append(" >= :").append(filter.getKey());
                    } else if (s.equals("receivedTo") || s.equals("maxEntityId")) {
                        result.append(tableName).append(" <= :").append(filter.getKey());
                    }
                }
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
            if (filter.getValue() != null && !filter.getValue().toString().isEmpty()) {
                if (filter.getValue() instanceof Date) {
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(((Date) filter.getValue()).toInstant(), ZoneOffset.UTC);
                    LOG.trace(" zonedDateTime is [{}]", zonedDateTime);
                    switch (filter.getKey()) {
                        case "minEntityId":
                            Long minId = Long.parseLong(zonedDateTime.format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MIN);
                            LOG.debug("Turned [{}] into min entityId [{}]", filter.getValue(), minId);
                            query.setParameter(filter.getKey(), minId);
                            break;
                        case "maxEntityId":
                            Long maxId = Long.parseLong(zonedDateTime.format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX);
                            LOG.debug("Turned [{}] into max entityId [{}]", filter.getValue(), maxId);
                            query.setParameter(filter.getKey(),maxId);
                            break;
                        default:
                            query.setParameter(filter.getKey(), filter.getValue());
                            break;
                    }
                } else {
                    query.setParameter(filter.getKey(), filter.getValue());
                }
            }
        }
        return query;
    }

    /**
     * in four corner model finalRecipient and originalSender exist as default properties
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

    protected String getQuery(Map<String, Object> filters, String selectExpression) {
        String query = selectExpression + getCountQueryBody(filters);
        StringBuilder result = filterQuery(query, null, false, filters);
        return result.toString();
    }

    public String getCountQueryBody(Map<String, Object> allFilters) {
        final Map<String, Object> filters = getNonEmptyParams(allFilters);

        StringBuilder fromQuery = createFromClause(filters);

        StringBuilder whereQuery = createWhereQuery(fromQuery);

        if (StringUtils.isBlank(whereQuery.toString())) {
            return fromQuery.toString();
        }
        return fromQuery.append(" where ").append(whereQuery).toString();
    }

    protected Map<String, Object> getNonEmptyParams(Map<String, Object> allFilters) {
        final Map<String, Object> filters = Maps.filterEntries(allFilters, input -> input.getValue() != null);
        return filters;
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
