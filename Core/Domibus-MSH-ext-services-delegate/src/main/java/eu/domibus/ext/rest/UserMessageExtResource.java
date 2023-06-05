package eu.domibus.ext.rest;

import eu.domibus.common.MSHRole;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.UserMessageExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.UserMessageExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
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
@OpenAPIDefinition(tags = {
        @Tag(name = "usermessage", description = "Domibus User Message management API"),
        @Tag(name = "envelope", description = "Domibus Message envelope management API"),
        @Tag(name = "signalEnvelope", description = "Domibus Signal Message envelope management API")
})
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
     * @param mshRole The message msh role
     * @return The User Message with the specified messageId and msh role
     * @throws UserMessageExtException Raised in case an exception occurs while trying to get a User Message
     */
    @Operation(summary = "Get user message", description = "Retrieve the user message with the specified message id",
            security = @SecurityRequirement(name = "DomibusBasicAuth"), tags = {"usermessage"})
    @GetMapping(path = "/{messageId:.+}")
    public UserMessageDTO getUserMessage(@PathVariable(value = "messageId") String messageId,
                                         @RequestParam(value = "mshRole", required = false) MSHRole mshRole) throws MessageNotFoundException {
        LOG.debug("Getting User Message with id = [{}] and mshRole = [{}]", messageId, mshRole);
        return userMessageExtService.getMessage(messageId, mshRole);
    }

    @Operation(summary = "Get user message envelope", description = "Retrieve the user message envelope with the specified message id",
            security = @SecurityRequirement(name = "DomibusBasicAuth"), tags = {"envelope"})
    @GetMapping(path = "/{messageId:.+}/envelope")
    public ResponseEntity<String> downloadUserMessageEnvelope(@PathVariable(value = "messageId") String messageId,
                                                              @RequestParam(value = "mshRole", required = false) MSHRole mshRole) {
        LOG.debug("Getting User Message Envelope with id = [{}] and mshRole = [{}]", messageId, mshRole);

        String result = userMessageExtService.getUserMessageEnvelope(messageId, mshRole);

        return buildResponse(result, "user_message_envelope_" + messageId + ".xml");
    }

    @Operation(summary = "Get signal message envelope", description = "Retrieve the signal message envelope with the specified user message id",
            security = @SecurityRequirement(name = "DomibusBasicAuth"), tags = {"signalEnvelope"})
    @GetMapping(path = "/{messageId:.+}/signalEnvelope")
    public ResponseEntity<String> downloadSignalMessageEnvelope(@PathVariable(value = "messageId") String messageId,
                                                                @RequestParam(value = "mshRole", required = false) MSHRole mshRole) {
        LOG.debug("Getting Signal Message Envelope with id = [{}] and mshRole = [{}]", messageId, mshRole);

        String result = userMessageExtService.getSignalMessageEnvelope(messageId, mshRole);

        return buildResponse(result, "signal_message_envelope_" + messageId + ".xml");
    }

    private ResponseEntity<String> buildResponse(String result, String fileName) {
        if (StringUtils.isBlank(result)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_XML_VALUE))
                .header("content-disposition", "attachment; filename=" + fileName)
                .body(result);
    }
}
