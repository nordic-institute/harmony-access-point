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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

    private static final String RECEIVED_FROM_STR = "receivedFrom";
    private static final String RECEIVED_TO_STR = "receivedTo";

    @Autowired
    protected TestService testService;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private UIMessageService uiMessageService;

    @Autowired
    private MessagesLogService messagesLogService;

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    Date defaultFrom;
    Date defaultTo;

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
        filters.put(RECEIVED_FROM_STR, from);
        filters.put(RECEIVED_TO_STR, to);
        filters.put("messageType", request.getMessageType());

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
            filters.remove(RECEIVED_FROM_STR);
        }
        if (defaultTo.equals(to)) {
            filters.remove(RECEIVED_TO_STR);
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

        filters.put(RECEIVED_FROM_STR, dateUtil.fromString(request.getReceivedFrom()));
        filters.put(RECEIVED_TO_STR, dateUtil.fromString(request.getReceivedTo()));
        filters.put("messageType", request.getMessageType());

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

        List<String> excludedColumns = Lists.newArrayList("sourceMessage", "messageFragment");
        if(!domibusConfigurationService.isFourCornerEnabled()) {
            excludedColumns.add("originalSender");
            excludedColumns.add("finalRecipient");
        }

        return exportToCSV(resultList,
                MessageLogInfo.class,
                ImmutableMap.of("mshRole".toUpperCase(), "AP Role"),
                excludedColumns,
                "messages");
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
        filters.put("messageId", request.getMessageId());
        filters.put("conversationId", request.getConversationId());
        filters.put("mshRole", request.getMshRole());
        filters.put("messageStatus", request.getMessageStatus());
        filters.put("notificationStatus", request.getNotificationStatus());
        filters.put("fromPartyId", request.getFromPartyId());
        filters.put("toPartyId", request.getToPartyId());
        filters.put("refToMessageId", request.getRefToMessageId());
        filters.put("originalSender", request.getOriginalSender());
        filters.put("finalRecipient", request.getFinalRecipient());
        filters.put("messageSubtype", request.getMessageSubtype());
        return filters;
    }

}
