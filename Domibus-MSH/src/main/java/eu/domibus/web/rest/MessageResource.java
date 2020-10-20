package eu.domibus.web.rest;

import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.core.message.MessagesLogService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.MessageLogRO;
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
    private MessagesLogService messagesLogService;

    @Autowired
    private ErrorHandlerService errorHandlerService;

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
        userMessageService.resendFailedOrSendEnqueuedMessage(messageId);
    }

    @RequestMapping(path = "/{messageId:.+}/downloadOld", method = RequestMethod.GET)
    public ResponseEntity<ByteArrayResource> download(@PathVariable(value = "messageId") String messageId) throws MessagingException {

        byte[] content = userMessageService.getMessageAsBytes(messageId);

        ByteArrayResource resource = new ByteArrayResource(content);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header("content-disposition", "attachment; filename=" + messageId + ".xml")
                .body(resource);
    }

    @RequestMapping(value = "/download")
    public ResponseEntity<ByteArrayResource> downloadUserMessage(@RequestParam(value = "messageId", required = true) String messageId)
            throws MessageNotFoundException, IOException {

        byte[] zip = userMessageService.getMessageWithAttachmentsAsZip(messageId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header("content-disposition", "attachment; filename=" + messageId + ".zip")
                .body(new ByteArrayResource(zip));
    }

    @RequestMapping(value = "/envelopes")
    public ResponseEntity<ByteArrayResource> downloadEnvelopes(@RequestParam(value = "messageId", required = true) String messageId)
            throws MessageNotFoundException, IOException {

        byte[] zip = userMessageService.getMessageEnvelopesAsZip(messageId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header("content-disposition", "attachment; filename=" + messageId + ".zip")
                .body(new ByteArrayResource(zip));
    }

    @RequestMapping(value = "/exists", method = RequestMethod.GET)
    public boolean checkMessageContentExists(@RequestParam(value = "messageId", required = true) String messageId) {
        MessageLogRO message = messagesLogService.findUserMessageById(messageId);

        if (message == null) {
            return false;
        }
        if (message.getDeleted() != null) {
            LOG.info("Could not find message content for message: [{}]", messageId);
            return false;
        }
        return true;
    }

}
