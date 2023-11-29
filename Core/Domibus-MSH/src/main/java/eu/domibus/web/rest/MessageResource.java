package eu.domibus.web.rest;

import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.usermessage.UserMessageRestoreService;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.core.message.MessagesLogService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.MessageLogFilterRequestRO;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.apache.commons.lang3.ArrayUtils;
import org.quartz.SchedulerException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by musatmi on 10/05/2017.
 */
@RestController
@RequestMapping(value = "/rest/message")
public class MessageResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageResource.class);
    private static final String PROPERTY_MESSAGE_STATUS = "messageStatus";

    private UserMessageService userMessageService;

    private ErrorHandlerService errorHandlerService;

    protected DomibusPropertyProvider domibusPropertyProvider;

    private UserMessageRestoreService restoreService;

    private RequestFilterUtils requestFilterUtils;

    private MessagesLogService messagesLogService;

    public MessageResource(UserMessageService userMessageService, ErrorHandlerService errorHandlerService, DomibusPropertyProvider domibusPropertyProvider,
                           UserMessageRestoreService restoreService,  RequestFilterUtils requestFilterUtils, MessagesLogService messagesLogService) {
        this.userMessageService = userMessageService;
        this.errorHandlerService = errorHandlerService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.restoreService = restoreService;
        this.requestFilterUtils = requestFilterUtils;
        this.messagesLogService = messagesLogService;
    }

    @ExceptionHandler({MessagingException.class})
    public ResponseEntity<ErrorRO> handleMessagingException(MessagingException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler({eu.domibus.messaging.MessageNotFoundException.class})
    public ResponseEntity<ErrorRO> handleMessageNotFoundException(eu.domibus.messaging.MessageNotFoundException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(path = "/restore", method = RequestMethod.PUT)
    public void resend(@RequestParam(value = "messageId", required = true) String messageId) {
        restoreService.resendFailedOrSendEnqueuedMessage(messageId);
    }

    @PutMapping("/failed/restore/selected")
    public void restoreSelectedFailedMessages(@RequestBody List<MessageLogRO> messageLogEntries) throws SchedulerException {
        LOG.info("Restoring Selected Failed Messages...");
        List<String> messageIds = messageLogEntries.stream()
                .map(messageLogRO -> messageLogRO.getMessageId())
                .collect(Collectors.toList());

        restoreService.restoreFailedMessages(messageIds);
    }


    @PutMapping(value = "/failed/restore/filtered")
    public void restoreFilteredFailedMessages(@RequestBody MessageLogFilterRequestRO request) throws SchedulerException {
        LOG.debug("Getting all messages to restore");

        //creating the filters
        HashMap<String, Object> filters = requestFilterUtils.createFilterMap(request);

        requestFilterUtils.setDefaultFilters(request, filters);
        filters.put(PROPERTY_MESSAGE_STATUS, MessageStatus.SEND_FAILURE);

        int messageCount = (int) messagesLogService.countMessages(request.getMessageType(),filters );
        MessageLogResultRO result = messagesLogService.countAndFindPaged(request.getMessageType(), request.getPageSize() * request.getPage(),
                messageCount, request.getOrderBy(), request.getAsc(), filters, Collections.emptyList());


        List<String> messageIds = result.getMessageLogEntries().stream()
                .map(messageLogRO -> messageLogRO.getMessageId())
                .collect(Collectors.toList());

        restoreService.restoreFailedMessages(messageIds);
    }

    @RequestMapping(value = "/download")
    public ResponseEntity<ByteArrayResource> downloadUserMessage(@RequestParam(value = "messageId", required = true) String messageId,
                                                                 @RequestParam(value = "mshRole") MSHRole mshRole)
            throws MessageNotFoundException, IOException {
        try {
            byte[] zip = userMessageService.getMessageWithAttachmentsAsZip(messageId, mshRole);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .header("content-disposition", "attachment; filename=" + messageId + ".zip")
                    .body(new ByteArrayResource(zip));
        } catch (MessagingException ex) {
            LOG.warn("Could not get content for user message [{}]; returning empty.", messageId, ex);
            return ResponseEntity.noContent().build();
        }
    }

    @RequestMapping(value = "/exists", method = RequestMethod.GET)
    public void checkCanDownload(@RequestParam(value = "messageId") String messageId,
                                 @RequestParam(value = "mshRole") MSHRole mshRole) {
        userMessageService.checkCanGetMessageContent(messageId, mshRole);
    }

    @GetMapping(value = "/envelopes")
    public ResponseEntity<ByteArrayResource> downloadEnvelopes(@RequestParam(value = "messageId", required = true) String messageId,
                                                               @RequestParam(value = "mshRole") MSHRole mshRole) {
        return getByteArrayResourceResponseEntity(messageId, mshRole);
    }

    protected ResponseEntity<ByteArrayResource> getByteArrayResourceResponseEntity(String messageId, MSHRole mshRole) {
        byte[] zip = userMessageService.getMessageEnvelopesAsZip(messageId, mshRole);

        if (ArrayUtils.isEmpty(zip)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header("content-disposition", "attachment; filename=message_envelopes_" + messageId + ".zip")
                .body(new ByteArrayResource(zip));
    }

}
