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

/**
 * @author Tiago Miguel, Catalin Enache
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/messagelog")
@Validated
public class MessageLogResource extends BaseResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageLogResource.class);

    private static final String MODULE_NAME_MESSAGES = "messages";

    private static final String PROPERTY_FINAL_RECIPIENT = "finalRecipient";
    private static final String PROPERTY_MESSAGE_FRAGMENT = "messageFragment";
    private static final String PROPERTY_MESSAGE_TYPE = "messageType";
    private static final String PROPERTY_MSH_ROLE = "mshRole";
    private static final String PROPERTY_ORIGINAL_SENDER = "originalSender";
    private static final String PROPERTY_RECEIVED_FROM = "receivedFrom";
    private static final String PROPERTY_RECEIVED_TO = "receivedTo";
    private static final String PROPERTY_MIN_ENTITY_ID = "minEntityId";
    private static final String PROPERTY_MAX_ENTITY_ID = "maxEntityId";
    private static final String PROPERTY_SOURCE_MESSAGE = "sourceMessage";
    private static final String PROPERTY_NEXTATTEMPT_TIMEZONEID = "nextAttemptTimezoneId";
    private static final String PROPERTY_NEXTATTEMPT_OFFSET = "nextAttemptOffsetSeconds";

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
                request.getPageSize(), request.getOrderBy(), request.getAsc(), filters);

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
        List<MessageLogInfo> resultList = messagesLogService.findAllInfoCSV(request.getMessageType(), maxNumberRowsToExport, request.getOrderBy(), request.getAsc(), filters);
        getCsvService().validateMaxRows(resultList.size(), () -> messagesLogService.countMessages(request.getMessageType(), filters));

        return exportToCSV(resultList,
                MessageLogInfo.class,
                ImmutableMap.of(PROPERTY_MSH_ROLE.toUpperCase(), COLUMN_NAME_AP_ROLE),
                getExcludedProperties(),
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

    private List<String> getExcludedProperties() {
        List<String> excludedProperties = Lists.newArrayList(PROPERTY_SOURCE_MESSAGE, PROPERTY_MESSAGE_FRAGMENT, PROPERTY_NEXTATTEMPT_TIMEZONEID, PROPERTY_NEXTATTEMPT_OFFSET, "testMessage", "pluginType", "partLength");
        if (!domibusConfigurationService.isFourCornerEnabled()) {
            excludedProperties.add(PROPERTY_ORIGINAL_SENDER);
            excludedProperties.add(PROPERTY_FINAL_RECIPIENT);
        }
        LOG.debug("Found properties to exclude from the generated CSV file: {}", excludedProperties);
        return excludedProperties;
    }
}
