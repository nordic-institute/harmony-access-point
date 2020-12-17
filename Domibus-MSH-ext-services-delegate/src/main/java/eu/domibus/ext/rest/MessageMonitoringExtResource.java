package eu.domibus.ext.rest;

import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.FailedMessagesCriteriaRO;
import eu.domibus.ext.domain.MessageAttemptDTO;
import eu.domibus.ext.exceptions.MessageMonitorExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.MessageMonitorExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/ext/monitoring/messages")
public class MessageMonitoringExtResource {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageMonitoringExtResource.class);

    @Autowired
    ExtExceptionHelper extExceptionHelper;

    @Autowired
    MessageMonitorExtService messageMonitorExtService;

    @ExceptionHandler(MessageMonitorExtException.class)
    public ResponseEntity<ErrorDTO> handleMessageMonitorExtException(MessageMonitorExtException e) {
        return extExceptionHelper.handleExtException(e);
    }

    @ApiOperation(value = "Get failed messages", notes = "Retrieve all the messages with the specified finalRecipient(if provided) that are currently in a SEND_FAILURE status",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @GetMapping(path = "/failed")
    public List<String> getFailedMessages(@RequestParam(value = "finalRecipient", required = false) String finalRecipient) {
        if (StringUtils.isNotEmpty(finalRecipient)) {
            return messageMonitorExtService.getFailedMessages(finalRecipient);
        } else {
            return messageMonitorExtService.getFailedMessages();
        }
    }

    @ApiOperation(value = "Get failed message elapsed time", notes = "Retrieve the time that a message has been in a SEND_FAILURE status",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @GetMapping(path = "/failed/{messageId:.+}/elapsedtime")
    public Long getFailedMessageInterval(@PathVariable(value = "messageId") String messageId) {
        return messageMonitorExtService.getFailedMessageInterval(messageId);
    }

    @ApiOperation(value = "Resend failed message", notes = "Resend a message which has a SEND_FAILURE status",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @PutMapping(path = "/failed/{messageId:.+}/restore")
    public void restoreFailedMessage(@PathVariable(value = "messageId") String messageId) {
        messageMonitorExtService.restoreFailedMessage(messageId);
    }

    @ApiOperation(value = "Send enqueued message", notes = "Send a message which has a SEND_ENQUEUED status",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @PutMapping(path = "/enqueued/{messageId:.+}/send")
    public void sendEnqueuedMessage(@PathVariable(value = "messageId") String messageId) {
        messageMonitorExtService.sendEnqueuedMessage(messageId);
    }

    @ApiOperation(value = "Resend all messages with SEND_FAILURE status within a certain time interval", notes = "Resend all messages with SEND_FAILURE status within a certain time interval",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @PostMapping(path = "/failed/restore")
    public List<String> restoreFailedMessages(@RequestBody FailedMessagesCriteriaRO failedMessagesCriteriaRO) {
        return messageMonitorExtService.restoreFailedMessagesDuringPeriod(failedMessagesCriteriaRO.getFromDate(), failedMessagesCriteriaRO.getToDate());
    }

    @ApiOperation(value = "Delete failed message payload", notes = "Delete the payload of a message which has a SEND_FAILURE status",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @ResponseBody
    @DeleteMapping(path = "/failed/{messageId:.+}")
    public void deleteFailedMessage(@PathVariable(value = "messageId") String messageId) {
        messageMonitorExtService.deleteFailedMessage(messageId);
    }

    @ApiOperation(value = "Get message attempts", notes = "Retrieve the history of the delivery attempts for a certain message",
            response = MessageAttemptDTO.class, responseContainer = "List", authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @GetMapping(path = "/{messageId:.+}/attempts")
    public List<MessageAttemptDTO> getMessageAttempts(@PathVariable(value = "messageId") String messageId) {
        return messageMonitorExtService.getAttemptsHistory(messageId);
    }
}
