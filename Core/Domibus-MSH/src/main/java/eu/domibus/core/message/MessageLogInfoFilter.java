package eu.domibus.core.message;

import com.google.common.collect.Maps;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.model.ServiceEntity;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.dao.SingleValueDictionaryDao;
import eu.domibus.core.message.dictionary.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.TypedQuery;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.*;
import static eu.domibus.web.rest.MessageLogResource.*;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;

/**
 * @author Tiago Miguel
 * @author Perpegel Ion
 * @since 3.3
 */
public abstract class MessageLogInfoFilter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageLogInfoFilter.class);

    private static final String LOG_MESSAGE_ENTITY_ID = "log.entityId";
    private static final String LOG_MESSAGE_ID = "message.messageId";
    public static final String LOG_MSH_ROLE = "log.mshRole";
    public static final String LOG_MESSAGE_STATUS = "log.messageStatus";
    public static final String LOG_NOTIFICATION_STATUS = "log.notificationStatus";
    private static final String LOG_DELETED = "log.deleted";
    private static final String LOG_RECEIVED = "log.received";
    private static final String LOG_SEND_ATTEMPTS = "log.sendAttempts";
    private static final String LOG_SEND_ATTEMPTS_MAX = "log.sendAttemptsMax";
    private static final String LOG_NEXT_ATTEMPT = "log.nextAttempt";
    public static final String INFO_REF_TO_MESSAGE_ID = "message.refToMessageId";
    public static final String PROPS_FROM_VALUE = "propsFrom.value";
    public static final String ORIGINAL_SENDER = "originalSender";
    public static final String PROPS_TO_VALUE = "propsTo.value";
    public static final String FINAL_RECIPIENT = "finalRecipient";
    private static final String MESSAGE_COLLABORATION_INFO_CONVERSATION_ID = "message.conversationId";
    private static final String LOG_FAILED = "log.failed";
    private static final String LOG_RESTORED = "log.restored";
    private static final String MESSAGE_TEST_MESSAGE = "message.testMessage";
    public static final String MESSAGE_ACTION = "action";
    public static final String MESSAGE_SERVICE_TYPE = "serviceType";
    public static final String MESSAGE_SERVICE_VALUE = "serviceValue";
    public static final String MESSAGE_COLLABORATION_INFO_ACTION = "message.action";
    public static final String MESSAGE_COLLABORATION_INFO_SERVICE = "message.service";
    public static final String MESSAGE_PARTY_INFO_FROM_FROM_PARTY_ID = "message.partyInfo.from.fromPartyId";
    public static final String MESSAGE_PARTY_INFO_TO_TO_PARTY_ID = "message.partyInfo.to.toPartyId";
    public static final String MIN_ENTITY_ID = "minEntityId";
    public static final String MAX_ENTITY_ID = "maxEntityId";
    public static final String RECEIVED_FROM = "receivedFrom";
    public static final String RECEIVED_TO = "receivedTo";

    Map<String, FilterParameterExtractor> parameterExtractors = new HashMap<>();

    @Autowired
    ServiceDao serviceDao;

    @Autowired
    PartyIdDao partyIdDao;

    @Autowired
    private MessageStatusDao messageStatusDao;

    @Autowired
    private MshRoleDao mshRoleDao;

    @Autowired
    private NotificationStatusDao notificationStatusDao;

    @Autowired
    private ActionDao actionDao;

    @Autowired
    private DateUtil dateUtil;

    public MessageLogInfoFilter() {
        parameterExtractors.put(MIN_ENTITY_ID, (filter) -> handleMinEntityId(filter));
        parameterExtractors.put(MAX_ENTITY_ID, (filter) -> handleMaxEntityId(filter));

        parameterExtractors.put(PROPERTY_MESSAGE_STATUS, (filter) -> handleSingleValueDictionary(filter));
        parameterExtractors.put(PROPERTY_NOTIFICATION_STATUS, (filter) -> handleSingleValueDictionary(filter));
        parameterExtractors.put(PROPERTY_MSH_ROLE, (filter) -> handleSingleValueDictionary(filter));
        parameterExtractors.put(MESSAGE_ACTION, (filter) -> handleSingleValueDictionary(filter));

        parameterExtractors.put(MESSAGE_SERVICE_TYPE, (filter) -> handleServiceType(filter));
        parameterExtractors.put(MESSAGE_SERVICE_VALUE, (filter) -> handleServiceValue(filter));

        parameterExtractors.put(PROPERTY_FROM_PARTY_ID, (filter) -> handlePartyIdDictionary(filter));
        parameterExtractors.put(PROPERTY_TO_PARTY_ID, (filter) -> handlePartyIdDictionary(filter));
    }

    protected String getHQLKey(String originalColumn) {
        switch (originalColumn) {
            case "entityId":
                return "message.entityId";
            case "messageId":
                return LOG_MESSAGE_ID;
            case PROPERTY_MSH_ROLE:
                return LOG_MSH_ROLE;
            case PROPERTY_MESSAGE_STATUS:
                return LOG_MESSAGE_STATUS;
            case PROPERTY_NOTIFICATION_STATUS:
                return LOG_NOTIFICATION_STATUS;
            case "deleted":
                return LOG_DELETED;
            case "received":
            case RECEIVED_FROM:
            case RECEIVED_TO:
                return LOG_RECEIVED;
            case MIN_ENTITY_ID:
            case MAX_ENTITY_ID:
                return LOG_MESSAGE_ENTITY_ID;
            case "sendAttempts":
                return LOG_SEND_ATTEMPTS;
            case "sendAttemptsMax":
                return LOG_SEND_ATTEMPTS_MAX;
            case "nextAttempt":
                return LOG_NEXT_ATTEMPT;
            case PROPERTY_FROM_PARTY_ID:
                return MESSAGE_PARTY_INFO_FROM_FROM_PARTY_ID;
            case PROPERTY_TO_PARTY_ID:
                return MESSAGE_PARTY_INFO_TO_TO_PARTY_ID;
            case "refToMessageId":
                return INFO_REF_TO_MESSAGE_ID;
            case ORIGINAL_SENDER:
                return PROPS_FROM_VALUE;
            case FINAL_RECIPIENT:
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
            case MESSAGE_SERVICE_VALUE:
                return MESSAGE_COLLABORATION_INFO_SERVICE;
            default:
                return "";
        }
    }

    protected StringBuilder filterQuery(String query, String sortColumn, boolean asc, Map<String, Object> filters) {
        StringBuilder result = new StringBuilder(query);
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            handleFilter(result, query, filter);
        }
        if (sortColumn == null) {
            return result;
        }

        String usedColumn = getHQLKey(sortColumn);
        if (!StringUtils.isBlank(usedColumn)) {
            result.append(" order by ").append(usedColumn).append(asc ? " asc" : " desc");
        }
        return result;
    }

    private void handleFilter(StringBuilder result, String query, Map.Entry<String, Object> filter) {
        if (filter.getValue() == null || StringUtils.isBlank(filter.getValue().toString())) {
            LOG.trace("Filter value for key [{}] is empty; exit", filter.getKey());
            return;
        }
        String fieldName = getHQLKey(filter.getKey());
        if (StringUtils.isBlank(fieldName)) {
            LOG.info("HQLKey for filter param [{}] is empty; exit", filter.getKey());
            return;
        }

        setSeparator(query, result);

        String operator = getOperator(filter);
        result.append(fieldName).append(operator).append(filter.getKey());
    }

    private String getOperator(Map.Entry<String, Object> filter) {
        String filterKey = filter.getKey();
        if (StringUtils.equalsAny(filterKey, RECEIVED_FROM, MIN_ENTITY_ID)) {
            return " >= :";
        } else if (StringUtils.equalsAny(filterKey, RECEIVED_TO, MAX_ENTITY_ID)) {
            return " <= :";
        } else if (StringUtils.equalsAny(filterKey, MESSAGE_SERVICE_TYPE, MESSAGE_SERVICE_VALUE, PROPERTY_FROM_PARTY_ID, PROPERTY_TO_PARTY_ID)) {
            return " IN :";
        }
        return " = :";
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
            setFilterParameter(query, filter);
        }
        return query;
    }

    private <E> void setFilterParameter(TypedQuery<E> query, Map.Entry<String, Object> filter) {
        if (filter.getValue() == null || StringUtils.isBlank(filter.getValue().toString())) {
            LOG.debug("Filter value for field [{}] is empty", filter.getKey());
            return;
        }
        Object value = getParameterValue(filter);
        LOG.debug("Set parameter [{}] the value [{}]", filter.getKey(), value);
        query.setParameter(filter.getKey(), value);
    }

    private Object getParameterValue(Map.Entry<String, Object> filter) {
        FilterParameterExtractor extractor = parameterExtractors.get(filter.getKey());
        if (extractor != null) {
            LOG.debug("Found filter parameter extractor [{}] for parameter [{}]", extractor, filter);
            return extractor.execute(filter);
        }
        LOG.debug("Found no custom filter parameter extractor for parameter [{}]. Just calling filter.getValue()", filter);
        return filter.getValue();
    }

    private Object handleSingleValueDictionary(Map.Entry<String, Object> filter) {
        SingleValueDictionaryDao dao = getSingleValueDao(filter.getKey());
        LOG.debug("Using [{}] dao service to get the value for filter param [{}]", dao.getClass(), filter);
        return dao.findByValue(filter.getValue());
    }

    private SingleValueDictionaryDao getSingleValueDao(String field) {
        switch (field) {
            case PROPERTY_MESSAGE_STATUS:
                return messageStatusDao;
            case PROPERTY_MSH_ROLE:
                return mshRoleDao;
            case PROPERTY_NOTIFICATION_STATUS:
                return notificationStatusDao;
            case MESSAGE_ACTION:
                return actionDao;
            default:
                throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "No DAO service found for field [" + field + "]");
        }
    }

    private List<ServiceEntity> handleServiceValue(Map.Entry<String, Object> filter) {
        return serviceDao.searchByValue(filter.getValue());
    }

    private List<ServiceEntity> handleServiceType(Map.Entry<String, Object> filter) {
        return serviceDao.searchByType(filter.getValue());
    }

    private Object handlePartyIdDictionary(Map.Entry<String, Object> filter) {
        return partyIdDao.searchByValue((String) filter.getValue());
    }

    private Object handleMaxEntityId(Map.Entry<String, Object> filter) {
        Date filterValue = (Date) filter.getValue();
        ZonedDateTime instant = ZonedDateTime.ofInstant(filterValue.toInstant(), ZoneOffset.UTC);
        return dateUtil.getMaxEntityId(instant, 0);
    }

    private Object handleMinEntityId(Map.Entry<String, Object> filter) {
        Date filterValue = (Date) filter.getValue();
        ZonedDateTime instant = ZonedDateTime.ofInstant(filterValue.toInstant(), ZoneOffset.UTC);
        return dateUtil.getMinEntityId(instant, 0);
    }

    public abstract String getFilterMessageLogQuery(String column, boolean asc, Map<String, Object> filters, List<String> fields);

    /**
     * Constructs the query body based on different conditions
     *
     * @return String query body
     */
    public String getQueryBody(Map<String, Object> filters, List<String> fields) {
        String query = getBaseQueryBody();

        if (!fields.contains(ORIGINAL_SENDER) && !fields.contains(FINAL_RECIPIENT)
                && filters.get(ORIGINAL_SENDER) == null && filters.get(FINAL_RECIPIENT) == null) {
            return query;
        }

        if (fields.contains(ORIGINAL_SENDER) || filters.containsKey(ORIGINAL_SENDER)) {
            query += "left join message.messageProperties propsFrom ";
        }
        if (fields.contains(FINAL_RECIPIENT) || filters.containsKey(FINAL_RECIPIENT)) {
            query += "left join message.messageProperties propsTo ";
        }
        query += "where ";
        if (fields.contains(ORIGINAL_SENDER) || filters.containsKey(ORIGINAL_SENDER)) {
            query += "propsFrom.name = '" + ORIGINAL_SENDER + "' ";
        }
        if (fields.contains(FINAL_RECIPIENT) || filters.containsKey(FINAL_RECIPIENT)) {
            if (query.contains(ORIGINAL_SENDER)) {
                query += "and ";
            }
            query += "propsTo.name = '" + FINAL_RECIPIENT + "' ";
        }
        return query;
    }

    protected abstract String getBaseQueryBody();

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
                LOG.info("HQLKey for filter param [{}] is empty; exit", filterParam);
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

    protected Map<String, List<String>> createFromMappings() {
        Map<String, List<String>> mappings = new HashMap<>();
        String messageTable = getMessageTable();

        mappings.put("message", Arrays.asList(messageTable));
        mappings.put("propsFrom", Arrays.asList(messageTable, "left join message.messageProperties propsFrom "));
        mappings.put("propsTo", Arrays.asList(messageTable, "left join message.messageProperties propsTo "));

        return mappings;
    }

    protected abstract String getMessageTable();

    protected Map<String, List<String>> createWhereMappings() {
        Map<String, List<String>> mappings = new HashMap<>();

        String messageCriteria = " 1=1 ";
        mappings.put("message", Arrays.asList(messageCriteria));
        mappings.put("propsFrom", Arrays.asList(messageCriteria, "and propsFrom.name = 'originalSender' "));
        mappings.put("propsTo", Arrays.asList(messageCriteria, "and propsTo.name = 'finalRecipient' "));

        return mappings;
    }

}
