package eu.domibus.ext.rest;


import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.exceptions.TruststoreExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.TruststoreExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Truststore Domibus services.
 * Domibus expose the REST API to upload and get the truststore.
 *
 * @author Soumya Chandran
 * @since 5.1
 */
@RestController
@RequestMapping(value = "/ext/truststore")
@SecurityScheme(type = SecuritySchemeType.HTTP, name = "DomibusBasicAuth", scheme = "basic")
@Tag(name = "truststore", description = "Domibus truststore services API")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_AP_ADMIN')")
public class TruststoreExtResource {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeFileExtResource.class);

    final TruststoreExtService truststoreExtService;

    final ExtExceptionHelper extExceptionHelper;

    public TruststoreExtResource(TruststoreExtService truststoreExtService, ExtExceptionHelper extExceptionHelper) {
        this.truststoreExtService = truststoreExtService;
        this.extExceptionHelper = extExceptionHelper;
    }

    @ExceptionHandler(TruststoreExtException.class)
    protected ResponseEntity<ErrorDTO> handlePModeExtException(TruststoreExtException e) {
        return extExceptionHelper.handleExtException(e);
    }


    @Operation(summary = "Upload truststore", description = "Upload the truststore file",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PostMapping(consumes = {"multipart/form-data"})
    public String uploadTruststoreFile(
            @RequestPart("file") MultipartFile truststoreFile,
            @SkipWhiteListed @RequestParam("password") String password) {

        String truststoreUploadMessage = truststoreExtService.uploadTruststoreFile(truststoreFile, password);

        return truststoreUploadMessage;
    }

}
