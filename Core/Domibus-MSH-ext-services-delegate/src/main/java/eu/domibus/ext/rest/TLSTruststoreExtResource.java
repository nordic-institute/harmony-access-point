package eu.domibus.ext.rest;


import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.TrustStoreDTO;
import eu.domibus.ext.exceptions.TruststoreExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.TLSTruststoreExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Truststore Domibus services.
 * Domibus expose the REST API to upload and get the truststore.
 *
 * @author Soumya Chandran
 * @since 5.1
 */
@RestController
@RequestMapping(value = "/ext/tlstruststore")
@SecurityScheme(type = SecuritySchemeType.HTTP, name = "DomibusBasicAuth", scheme = "basic")
@Tag(name = "TLS truststore", description = "Domibus TLS truststore services API")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_AP_ADMIN')")
public class TLSTruststoreExtResource {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TLSTruststoreExtResource.class);

    final TLSTruststoreExtService tlsTruststoreExtService;

    final ExtExceptionHelper extExceptionHelper;

    public TLSTruststoreExtResource(TLSTruststoreExtService tlsTruststoreExtService, ExtExceptionHelper extExceptionHelper) {
        this.tlsTruststoreExtService = tlsTruststoreExtService;
        this.extExceptionHelper = extExceptionHelper;
    }

    @ExceptionHandler(TruststoreExtException.class)
    protected ResponseEntity<ErrorDTO> handleTrustStoreExtException(TruststoreExtException e) {
        return extExceptionHelper.handleExtException(e);
    }

    @Operation(summary = "Get TLS truststore entries", description = "Get the TLS truststore details",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @GetMapping(value = {"/entries"})
    public List<TrustStoreDTO> getTLSTruststoreEntries() {
        return tlsTruststoreExtService.getTLSTrustStoreEntries();
    }

    @Operation(summary = "Download TLS truststore", description = "Upload the TLS truststore file",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @GetMapping(value = "/download", produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadTLSTrustStore() {
        return tlsTruststoreExtService.downloadTLSTruststoreContent();
    }

    @Operation(summary = "Upload truststore", description = "Upload the truststore file",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PostMapping(consumes = {"multipart/form-data"})
    public String uploadTLSTruststoreFile(
            @RequestPart("file") MultipartFile truststoreFile,
            @SkipWhiteListed @RequestParam("password") String password) {

        String truststoreUploadMessage = tlsTruststoreExtService.uploadTLSTruststoreFile(truststoreFile, password);

        return truststoreUploadMessage;
    }

}
