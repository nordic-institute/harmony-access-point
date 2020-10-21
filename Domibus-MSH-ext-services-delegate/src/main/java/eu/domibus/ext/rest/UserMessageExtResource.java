package eu.domibus.ext.rest;

import eu.domibus.api.messaging.MessageNotFoundException;
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

    @RequestMapping(value = "/envelopes")
    public ResponseEntity<String> downloadEnvelopes(@RequestParam(value = "messageId", required = true) String messageId,
                                                    @RequestParam(value = "messageType", required = true, defaultValue = "USER_MESSAGE") String messageType)
            throws MessageNotFoundException {

        String result = userMessageExtService.getMessageEnvelope(messageId, messageType);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/xml"))
                .header("content-disposition", "attachment; filename=envelope_" + messageId + ".zip")
                .body(result);
    }
}
