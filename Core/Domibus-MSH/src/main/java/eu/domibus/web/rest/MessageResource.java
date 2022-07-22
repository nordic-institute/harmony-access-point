package eu.domibus.web.rest;

import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageRestoreService;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.core.message.MessagesLogService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Created by musatmi on 10/05/2017.
 */
@RestController
@RequestMapping(value = "/rest/message")
public class MessageResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageResource.class);

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    private ErrorHandlerService errorHandlerService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private UserMessageRestoreService restoreService;

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

    @RequestMapping(value = "/download")
    public ResponseEntity<ByteArrayResource> downloadUserMessage(@RequestParam(value = "messageId", required = true) String messageId,
                                                                 @RequestParam(value = "mshRole") String mshRole)
            throws MessageNotFoundException, IOException {
        try {
            byte[] zip = userMessageService.getMessageWithAttachmentsAsZip(messageId, MSHRole.valueOf(mshRole));

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
                                 @RequestParam(value = "mshRole") String mshRole) {
        userMessageService.checkCanGetMessageContent(messageId, MSHRole.valueOf(mshRole));
    }

    @GetMapping(value = "/envelopes")
    public ResponseEntity<ByteArrayResource> downloadEnvelopes(@RequestParam(value = "messageId", required = true) String messageId,
                                                               @RequestParam(value = "mshRole") String mshRole) {
        return getByteArrayResourceResponseEntity(messageId, MSHRole.valueOf(mshRole));
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
