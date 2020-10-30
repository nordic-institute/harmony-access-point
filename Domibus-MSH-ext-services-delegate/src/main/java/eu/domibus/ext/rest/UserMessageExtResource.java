package eu.domibus.ext.rest;

import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.UserMessageExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.UserMessageExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
@RestController
@RequestMapping(value = "/ext/messages/usermessages")
public class UserMessageExtResource {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageExtResource.class);

    @Autowired
    UserMessageExtService userMessageExtService;

    @Autowired
    ExtExceptionHelper extExceptionHelper;

    @ExceptionHandler(UserMessageExtException.class)
    public ResponseEntity<ErrorDTO> handleUserMessageExtException(UserMessageExtException e) {
        return extExceptionHelper.handleExtException(e);
    }

    /**
     * Gets the User Message by messageId
     *
     * @param messageId The message Id
     * @return The User Message with the specified messageId
     * @throws UserMessageExtException Raised in case an exception occurs while trying to get a User Message
     */
    @ApiOperation(value = "Get user message", notes = "Retrieve the user message with the specified message id",
            authorizations = @Authorization(value = "basicAuth"), tags = "usermessage")
    @GetMapping(path = "/{messageId:.+}")
    public UserMessageDTO getUserMessage(@PathVariable(value = "messageId") String messageId) {
        LOG.debug("Getting User Message with id = '{}", messageId);
        return userMessageExtService.getMessage(messageId);
    }

    @ApiOperation(value = "Get user message envelope", notes = "Retrieve the user message envelope with the specified message id",
            authorizations = @Authorization(value = "basicAuth"), tags = "envelope")
    @GetMapping(path = "/{messageId:.+}/envelope")
    public ResponseEntity<String> downloadUserMessageEnvelope(@PathVariable(value = "messageId") String messageId) {
        LOG.debug("Getting User Message Envelope with id = [{}]", messageId);
        String result = userMessageExtService.getUserMessageEnvelope(messageId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/xml"))
                .header("content-disposition", "attachment; filename=user_message_envelope_" + messageId + ".xml")
                .body(result);
    }

    @ApiOperation(value = "Get signal message envelope", notes = "Retrieve the signal message envelope with the specified user message id",
            authorizations = @Authorization(value = "basicAuth"), tags = "signalEnvelope")
    @GetMapping(path = "/{messageId:.+}/signalEnvelope")
    public ResponseEntity<String> downloadSignalMessageEnvelope(@PathVariable(value = "messageId") String messageId) {
        LOG.debug("Getting Signal Message Envelope with id = [{}]", messageId);
        String result = userMessageExtService.getSignalMessageEnvelope(messageId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/xml"))
                .header("content-disposition", "attachment; filename=signal_message_envelope_" + messageId + ".xml")
                .body(result);
    }
}
