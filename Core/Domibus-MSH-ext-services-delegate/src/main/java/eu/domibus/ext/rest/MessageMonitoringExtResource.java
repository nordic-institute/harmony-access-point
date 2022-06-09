package eu.domibus.ext.rest;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusDateTimeException;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.FailedMessagesCriteriaRO;
import eu.domibus.ext.domain.MessageAttemptDTO;
import eu.domibus.ext.exceptions.DomibusDateTimeExtException;
import eu.domibus.ext.exceptions.MessageMonitorExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.DateExtService;
import eu.domibus.ext.services.MessageMonitorExtService;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "monitoring", description = "Domibus Message Monitoring API")
public class MessageMonitoringExtResource {

    public static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageMonitoringExtResource.class);

    @Autowired
    ExtExceptionHelper extExceptionHelper;

    @Autowired
    MessageMonitorExtService messageMonitorExtService;

    @Autowired
    DateExtService dateExtService;

    @ExceptionHandler(MessageMonitorExtException.class)
    public ResponseEntity<ErrorDTO> handleMessageMonitorExtException(MessageMonitorExtException e) {
        return extExceptionHelper.handleExtException(e);
    }

    @Operation(summary = "Get failed messages", description = "Retrieve all the messages with the specified finalRecipient(if provided) that are currently in a SEND_FAILURE status",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @GetMapping(path = "/failed")
    public List<String> getFailedMessages(@RequestParam(value = "finalRecipient", required = false) String finalRecipient) {
        if (StringUtils.isNotEmpty(finalRecipient)) {
            return messageMonitorExtService.getFailedMessages(finalRecipient);
        } else {
            return messageMonitorExtService.getFailedMessages();
        }
    }

    @Operation(summary = "Get failed message elapsed time", description = "Retrieve the time that a message has been in a SEND_FAILURE status",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @GetMapping(path = "/failed/{messageId:.+}/elapsedtime")
    public Long getFailedMessageInterval(@PathVariable(value = "messageId") String messageId) {
        return messageMonitorExtService.getFailedMessageInterval(messageId);
    }

    @Operation(summary = "Resend failed message", description = "Resend a message which has a SEND_FAILURE status",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PutMapping(path = "/failed/{messageId:.+}/restore")
    public void restoreFailedMessage(@PathVariable(value = "messageId") String messageId) {
        messageMonitorExtService.restoreFailedMessage(messageId);
    }

    @Operation(summary = "Send enqueued message", description = "Send a message which has a SEND_ENQUEUED status",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PutMapping(path = "/enqueued/{messageId:.+}/send")
    public void sendEnqueuedMessage(@PathVariable(value = "messageId") String messageId) {
        messageMonitorExtService.sendEnqueuedMessage(messageId);
    }

    @Operation(summary = "Resend all messages with SEND_FAILURE status within a certain time interval", description = "Resend all messages with SEND_FAILURE status within a certain time interval",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PostMapping(path = "/failed/restore")
    public List<String> restoreFailedMessages(@RequestBody FailedMessagesCriteriaRO failedMessagesCriteriaRO) {
        Long fromDateHour = dateExtService.getIdPkDateHour(failedMessagesCriteriaRO.getFromDate());
        Long toDateHour = dateExtService.getIdPkDateHour(failedMessagesCriteriaRO.getToDate());
        if (fromDateHour >= toDateHour) {
            throw getDatesValidationError();
        }
        return messageMonitorExtService.restoreFailedMessagesDuringPeriod(fromDateHour, toDateHour);
    }

    @Operation(summary = "Delete failed message payload", description = "Delete the payload of a message which has a SEND_FAILURE status",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @ResponseBody
    @DeleteMapping(path = "/failed/{messageId:.+}")
    public void deleteFailedMessage(@PathVariable(value = "messageId") String messageId) {
        messageMonitorExtService.deleteFailedMessage(messageId);
    }

    @Operation(summary = "Get message attempts",
            description = "Retrieve the history of the delivery attempts for a certain message",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MessageAttemptDTO.class))))
    })
    @GetMapping(path = "/{messageId:.+}/attempts")
    public List<MessageAttemptDTO> getMessageAttempts(@PathVariable(value = "messageId") String messageId) {
        return messageMonitorExtService.getAttemptsHistory(messageId);
    }

    @Operation(summary = "Delete message payload", description = "Delete the payload of a message which is not in final statuses.",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @ResponseBody
    @DeleteMapping(path = "/delete/{messageId:.+}")
    public void deleteMessage(@PathVariable(value = "messageId") String messageId) {
        messageMonitorExtService.deleteMessageNotInFinalStatus(messageId);
    }

    @Operation(summary = "Delete messages payload",
            description = "Delete the payload of messages within a certain time interval which are not in final statuses.",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @ResponseBody
    @DeleteMapping(path = "/delete")
    public List<String> deleteMessages(@RequestBody FailedMessagesCriteriaRO deleteMessagesCriteriaRO) {
        LOG.debug("Delete messages from date-hour [{}] to date-hour [{}]", deleteMessagesCriteriaRO.getFromDate(),
                deleteMessagesCriteriaRO.getToDate());
        Long fromDateHour = dateExtService.getIdPkDateHour(deleteMessagesCriteriaRO.getFromDate());
        Long toDateHour = dateExtService.getIdPkDateHour(deleteMessagesCriteriaRO.getToDate());
        if (fromDateHour >= toDateHour) {
            throw getDatesValidationError();
        }
        return messageMonitorExtService.deleteMessagesDuringPeriod(fromDateHour, toDateHour);
    }

    private DomibusDateTimeExtException getDatesValidationError() {
        return new DomibusDateTimeExtException("starting date-hour and ending date-hour validation error", new DomibusDateTimeException(DomibusCoreErrorCode.DOM_007, "Starting date hour is after Ending date hour"));
    }
}
