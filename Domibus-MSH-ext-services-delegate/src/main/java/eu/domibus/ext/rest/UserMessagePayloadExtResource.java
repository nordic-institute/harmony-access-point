package eu.domibus.ext.rest;

import eu.domibus.api.message.validation.UserMessageValidatorServiceDelegate;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.UserMessageExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@RestController
@RequestMapping(value = "/ext/messages/payloads")
@OpenAPIDefinition(tags = {
        @Tag(name = "payloads", description = "Domibus User Message Payloads management API"),
})
public class UserMessagePayloadExtResource {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessagePayloadExtResource.class);

    @Autowired
    UserMessageValidatorServiceDelegate userMessageValidatorServiceDelegate;

    @Autowired
    ExtExceptionHelper extExceptionHelper;

    @ExceptionHandler(UserMessageExtException.class)
    public ResponseEntity<ErrorDTO> handleUserMessageExtException(UserMessageExtException e) {
        return extExceptionHelper.createResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = "Validates a payload using the configured validator SPI extension", description = "Validates a payload using the configured validator SPI extension",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PostMapping(value = "validation", consumes = {"multipart/form-data"})
    public void validatePayload(@RequestPart("file") MultipartFile payload) {
        try {
            userMessageValidatorServiceDelegate.validatePayload(payload.getInputStream(), payload.getContentType());
        } catch (IOException e) {
            throw new UserMessageExtException(DomibusErrorCode.DOM_005, "Could not get the payload inputstream");
        }
    }
}
