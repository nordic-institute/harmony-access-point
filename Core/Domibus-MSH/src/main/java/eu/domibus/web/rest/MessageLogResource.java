package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.NotificationStatus;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.MessagesLogService;
import eu.domibus.core.message.testservice.TestService;
import eu.domibus.core.message.testservice.TestServiceException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

import static eu.domibus.core.message.MessageLogInfoFilter.*;

/**
 * @author Tiago Miguel, Catalin Enache
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/messagelog")
@Validated
public class MessageLogResource extends BaseResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageLogResource.class);

    public static final int DEFAULT_MESSAGES_SEARCH_INTERVAL_IN_MINUTES = 60;

    private static final String MODULE_NAME_MESSAGES = "messages";

    private static final String PROPERTY_CONVERSATION_ID = "conversationId";
    private static final String PROPERTY_FINAL_RECIPIENT = "finalRecipient";
    public static final String PROPERTY_FROM_PARTY_ID = "fromPartyId";
    public static final String PROPERTY_TO_PARTY_ID = "toPartyId";
    public static final String PROPERTY_MESSAGE_FRAGMENT = "messageFragment";
    private static final String PROPERTY_MESSAGE_ID = "messageId";
    public static final String PROPERTY_MESSAGE_STATUS = "messageStatus";
    private static final String PROPERTY_TEST_MESSAGE = "testMessage";
    private static final String PROPERTY_MESSAGE_TYPE = "messageType";
    public static final String PROPERTY_MSH_ROLE = "mshRole";
    public static final String PROPERTY_NOTIFICATION_STATUS = "notificationStatus";
    private static final String PROPERTY_ORIGINAL_SENDER = "originalSender";
    private static final String PROPERTY_RECEIVED_FROM = "receivedFrom";
    private static final String PROPERTY_RECEIVED_TO = "receivedTo";
    private static final String PROPERTY_MIN_ENTITY_ID = "minEntityId";
    private static final String PROPERTY_MAX_ENTITY_ID = "maxEntityId";
    private static final String PROPERTY_REF_TO_MESSAGE_ID = "refToMessageId";
    private static final String PROPERTY_SOURCE_MESSAGE = "sourceMessage";
    public static final String PROPERTY_NEXT_ATTEMPT_TIMEZONEID = "nextAttemptTimezoneId";
    public static final String PROPERTY_NEXT_ATTEMPT_OFFSET = "nextAttemptOffsetSeconds";

    public static final String COLUMN_NAME_AP_ROLE = "AP Role";
    private final TestService testService;

    private final DateUtil dateUtil;

    private final DomibusConfigurationService domibusConfigurationService;

    private final RequestFilterUtils requestFilterUtils;

    private final MessagesLogService messagesLogService;

    public MessageLogResource(TestService testService, DateUtil dateUtil, DomibusConfigurationService domibusConfigurationService,
                              RequestFilterUtils requestFilterUtils, MessagesLogService messagesLogService) {
        this.testService = testService;
        this.dateUtil = dateUtil;
        this.domibusConfigurationService = domibusConfigurationService;
        this.requestFilterUtils = requestFilterUtils;
        this.messagesLogService = messagesLogService;
    }

    @GetMapping
    public MessageLogResultRO getMessageLog(@Valid MessageLogFilterRequestRO request) {
        LOG.debug("Getting message log");

        //creating the filters
        HashMap<String, Object> filters = requestFilterUtils.createFilterMap(request);

        requestFilterUtils.setDefaultFilters(request, filters);

        MessageLogResultRO result = messagesLogService.countAndFindPaged(request.getMessageType(), request.getPageSize() * request.getPage(),
                request.getPageSize(), request.getOrderBy(), request.getAsc(), filters, request.getFields());

        // return also the current messageType to be shown in GUI
        filters.put(PROPERTY_MESSAGE_TYPE, request.getMessageType());
        // remove
        filters.remove(PROPERTY_MIN_ENTITY_ID);
        filters.remove(PROPERTY_MAX_ENTITY_ID);

        result.setFilter(filters);
        result.setMshRoles(MSHRole.values());
        result.setMsgTypes(MessageType.values());
        result.setMsgStatus(MessageStatus.values());
        result.setNotifStatus(NotificationStatus.values());
        result.setPage(request.getPage());
        result.setPageSize(request.getPageSize());

        return result;
    }

    /**
     * This method returns a CSV file with the contents of Messages table
     *
     * @return CSV file with the contents of Messages table
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv(@Valid final MessageLogFilterRequestRO request) {
        HashMap<String, Object> filters = requestFilterUtils.createFilterMap(request);

        filters.put(PROPERTY_RECEIVED_FROM, dateUtil.fromString(request.getReceivedFrom()));
        filters.put(PROPERTY_RECEIVED_TO, dateUtil.fromString(request.getReceivedTo()));

        int maxNumberRowsToExport = getCsvService().getPageSizeForExport();
        List<MessageLogInfo> resultList = messagesLogService.findAllInfoCSV(request.getMessageType(), maxNumberRowsToExport, request.getOrderBy(), request.getAsc(), filters, request.getFields());
        getCsvService().validateMaxRows(resultList.size(), () -> messagesLogService.countMessages(request.getMessageType(), filters));

        return exportToCSV(resultList,
                MessageLogInfo.class,
                ImmutableMap.of(PROPERTY_MSH_ROLE.toUpperCase(), COLUMN_NAME_AP_ROLE),
                getExcludedProperties(request.getFields()),
                MODULE_NAME_MESSAGES);
    }

    /**
     * This method gets the last send UserMessage for the given party Id
     *
     * @param request
     * @return ResposeEntity of TestServiceMessageInfoRO
     * @throws TestServiceException
     */
    @GetMapping(value = "test/outgoing/latest")
    public ResponseEntity<TestServiceMessageInfoRO> getLastTestSent(@Valid LatestOutgoingMessageRequestRO request) throws TestServiceException {
        TestServiceMessageInfoRO testServiceMessageInfoRO = testService.getLastTestSentWithErrors(request.getSenderPartyId(), request.getPartyId());
        return ResponseEntity.ok().body(testServiceMessageInfoRO);
    }


    /**
     * This method gets last Received Signal Message for the given party Id and User MessageId
     *
     * @param request
     * @return ResposeEntity of TestServiceMessageInfoRO
     * @throws TestServiceException
     */
    @GetMapping(value = "test/incoming/latest")
    public ResponseEntity<TestServiceMessageInfoRO> getLastTestReceived(@Valid LatestIncomingMessageRequestRO request) throws TestServiceException {
        TestServiceMessageInfoRO testServiceMessageInfoRO = testService.getLastTestReceivedWithErrors(request.getSenderPartyId(), request.getPartyId(), request.getUserMessageId());
        return ResponseEntity.ok().body(testServiceMessageInfoRO);
    }

    private List<String> getExcludedProperties(List<String> displayedFields) {
        final List<String> excludedProperties = Lists.newArrayList(PROPERTY_SOURCE_MESSAGE, PROPERTY_MESSAGE_FRAGMENT,
                PROPERTY_NEXT_ATTEMPT_TIMEZONEID, PROPERTY_NEXT_ATTEMPT_OFFSET, "testMessage", "pluginType", "partLength",
                "messageStatusId","notificationStatusId", "mshRoleId", "nextAttemptTimezonePk", "fromPartyIdPk", "toPartyIdPk",
                "actionId", "serviceId");
        List<String> optionalFields = Lists.newArrayList(PROPERTY_ORIGINAL_SENDER, PROPERTY_FINAL_RECIPIENT, MESSAGE_ACTION,
                MESSAGE_SERVICE_TYPE, MESSAGE_SERVICE_VALUE, PROPERTY_MESSAGE_STATUS, PROPERTY_MSH_ROLE, PROPERTY_NOTIFICATION_STATUS,
                PROPERTY_NEXT_ATTEMPT_TIMEZONEID, PROPERTY_FROM_PARTY_ID, PROPERTY_TO_PARTY_ID);
        optionalFields.forEach(field -> {
            if (!displayedFields.contains(field)) {
                excludedProperties.add(field);
            }
        });
        LOG.debug("Found properties to exclude from the generated CSV file: {}", excludedProperties);
        return excludedProperties;
    }
}
