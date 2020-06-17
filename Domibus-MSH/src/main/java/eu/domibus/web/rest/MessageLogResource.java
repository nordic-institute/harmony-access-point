package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.MessagesLogService;
import eu.domibus.core.message.testservice.TestService;
import eu.domibus.core.message.testservice.TestServiceException;
import eu.domibus.core.plugin.notification.NotificationStatus;
import eu.domibus.core.replication.UIMessageService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    private static final String PROPERTY_CONVERSATION_ID = "conversationId";
    private static final String PROPERTY_FINAL_RECIPIENT = "finalRecipient";
    private static final String PROPERTY_FROM_PARTY_ID = "fromPartyId";
    private static final String PROPERTY_MESSAGE_FRAGMENT = "messageFragment";
    private static final String PROPERTY_MESSAGE_ID = "messageId";
    private static final String PROPERTY_MESSAGE_STATUS = "messageStatus";
    private static final String PROPERTY_MESSAGE_SUBTYPE = "messageSubtype";
    private static final String PROPERTY_MESSAGE_TYPE = "messageType";
    private static final String PROPERTY_MSH_ROLE = "mshRole";
    private static final String PROPERTY_NOTIFICATION_STATUS = "notificationStatus";
    private static final String PROPERTY_ORIGINAL_SENDER = "originalSender";
    private static final String PROPERTY_RECEIVED_FROM = "receivedFrom";
    private static final String PROPERTY_RECEIVED_TO = "receivedTo";
    private static final String PROPERTY_REF_TO_MESSAGE_ID = "refToMessageId";
    private static final String PROPERTY_SOURCE_MESSAGE = "sourceMessage";
    private static final String PROPERTY_TO_PARTY_ID = "toPartyId";

    public static final String COLUMN_NAME_AP_ROLE = "AP Role";

    private final TestService testService;

    private final DateUtil dateUtil;

    private final UIMessageService uiMessageService;

    private final MessagesLogService messagesLogService;

    private final UIReplicationSignalService uiReplicationSignalService;

    private final DomibusConfigurationService domibusConfigurationService;

    Date defaultFrom;
    Date defaultTo;

    public MessageLogResource(TestService testService, DateUtil dateUtil, UIMessageService uiMessageService, MessagesLogService messagesLogService, UIReplicationSignalService uiReplicationSignalService, DomibusConfigurationService domibusConfigurationService) {
        this.testService = testService;
        this.dateUtil = dateUtil;
        this.uiMessageService = uiMessageService;
        this.messagesLogService = messagesLogService;
        this.uiReplicationSignalService = uiReplicationSignalService;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    @PostConstruct
    public void init() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        try {
            defaultFrom = ft.parse("1970-01-01 23:59:00");
            defaultTo = ft.parse("2977-10-25 23:59:00");
        } catch (ParseException e) {
            LOG.error("Impossible to initiate default dates");
        }
    }

    @GetMapping
    public MessageLogResultRO getMessageLog(@Valid MessageLogFilterRequestRO request) {
        LOG.debug("Getting message log");

        //creating the filters
        HashMap<String, Object> filters = createFilterMap(request);

        //we just set default values for received column
        // in order to improve pagination on large amount of data
        Date from = dateUtil.fromString(request.getReceivedFrom());
        if (from == null) {
            from = defaultFrom;
        }
        Date to = dateUtil.fromString(request.getReceivedTo());
        if (to == null) {
            to = defaultTo;
        }
        filters.put(PROPERTY_RECEIVED_FROM, from);
        filters.put(PROPERTY_RECEIVED_TO, to);
        filters.put(PROPERTY_MESSAGE_TYPE, request.getMessageType());

        LOG.debug("using filters [{}]", filters);

        MessageLogResultRO result;
        if (uiReplicationSignalService.isReplicationEnabled()) {
            /** use TB_MESSAGE_UI table instead */
            result = uiMessageService.countAndFindPaged(request.getPageSize() * request.getPage(), request.getPageSize(),
                    request.getOrderBy(), request.getAsc(), filters);
        } else {
            //old, fashioned way
            result = messagesLogService.countAndFindPaged(request.getMessageType(), request.getPageSize() * request.getPage(),
                    request.getPageSize(), request.getOrderBy(), request.getAsc(), filters);
        }

        if (defaultFrom.equals(from)) {
            filters.remove(PROPERTY_RECEIVED_FROM);
        }
        if (defaultTo.equals(to)) {
            filters.remove(PROPERTY_RECEIVED_TO);
        }
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
        HashMap<String, Object> filters = createFilterMap(request);

        filters.put(PROPERTY_RECEIVED_FROM, dateUtil.fromString(request.getReceivedFrom()));
        filters.put(PROPERTY_RECEIVED_TO, dateUtil.fromString(request.getReceivedTo()));
        filters.put(PROPERTY_MESSAGE_TYPE, request.getMessageType());

        int maxNumberRowsToExport = getCsvService().getPageSizeForExport();
        List<MessageLogInfo> resultList;
        if (uiReplicationSignalService.isReplicationEnabled()) {
            /** use TB_MESSAGE_UI table instead */
            resultList = uiMessageService.findPaged(0, maxNumberRowsToExport, request.getOrderBy(), request.getAsc(), filters);
            getCsvService().validateMaxRows(resultList.size(), () -> uiMessageService.countMessages(filters));
        } else {
            resultList = messagesLogService.findAllInfoCSV(request.getMessageType(), maxNumberRowsToExport, request.getOrderBy(), request.getAsc(), filters);
            getCsvService().validateMaxRows(resultList.size(), () -> messagesLogService.countMessages(request.getMessageType(), filters));
        }

        return exportToCSV(resultList,
                MessageLogInfo.class,
                ImmutableMap.of(PROPERTY_MSH_ROLE.toUpperCase(), COLUMN_NAME_AP_ROLE),
                getExcludedProperties(),
                MODULE_NAME_MESSAGES);
    }

    private List<String> getExcludedProperties() {
        List<String> excludedProperties = Lists.newArrayList(PROPERTY_SOURCE_MESSAGE, PROPERTY_MESSAGE_FRAGMENT);
        if(!domibusConfigurationService.isFourCornerEnabled()) {
            excludedProperties.add(PROPERTY_ORIGINAL_SENDER);
            excludedProperties.add(PROPERTY_FINAL_RECIPIENT);
        }
        LOG.debug("Found properties to exclude from the generated CSV file: {}", excludedProperties);
        return excludedProperties;
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
        TestServiceMessageInfoRO testServiceMessageInfoRO = testService.getLastTestSentWithErrors(request.getPartyId());
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
        TestServiceMessageInfoRO testServiceMessageInfoRO = testService.getLastTestReceivedWithErrors(request.getPartyId(), request.getUserMessageId());
        return ResponseEntity.ok().body(testServiceMessageInfoRO);
    }

    private HashMap<String, Object> createFilterMap(MessageLogFilterRequestRO request) {
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
        filters.put(PROPERTY_MESSAGE_SUBTYPE, request.getMessageSubtype());
        return filters;
    }

}
