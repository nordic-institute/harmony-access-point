package eu.domibus.ext.rest;


import eu.domibus.api.exceptions.RequestValidationException;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * TLS Truststore Domibus services.
 * Domibus expose the REST API to upload and get the TLS truststore.
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

    public static final String ERROR_MESSAGE_EMPTY_TLS_TRUSTSTORE_PASSWORD = "Failed to upload the TLS truststoreFile file since its password was empty.";

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
        byte[] content;
        try {
            content = tlsTruststoreExtService.downloadTLSTruststoreContent();
        } catch (Exception e) {
            LOG.error("Could not find TLS truststore.", e);
            return ResponseEntity.notFound().build();
        }
        HttpStatus status = HttpStatus.OK;
        if (content.length == 0) {
            status = HttpStatus.NO_CONTENT;
        }

        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header("content-disposition", "attachment; filename=" + "tlsTruststore" + ".jks")
                .body(new ByteArrayResource(content));
    }

    @Operation(summary = "Upload TLS truststore", description = "Upload the TLS truststore file",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PostMapping(consumes = {"multipart/form-data"})
    public String uploadTLSTruststoreFile(
            @RequestPart("file") MultipartFile truststoreFile,
            @SkipWhiteListed @RequestParam("password") String password) {
        if (StringUtils.isBlank(password)) {
            throw new RequestValidationException(ERROR_MESSAGE_EMPTY_TLS_TRUSTSTORE_PASSWORD);
        }
        tlsTruststoreExtService.uploadTLSTruststoreFile(truststoreFile, password);
        return "TLS truststore file has been successfully replaced.";
    }

    @Operation(summary = "Add Certificate", description = "Add Certificate to the TLS truststore",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PostMapping(value = "/entries")
    public String addTLSCertificate(@RequestPart("file") MultipartFile certificateFile,
                                    @RequestParam("alias") @Valid @NotNull String alias) throws RequestValidationException {

        tlsTruststoreExtService.addTLSCertificate(certificateFile, alias);

        return "Certificate [" + alias + "] has been successfully added to the TLS truststore.";
    }

    @Operation(summary = "Remove Certificate", description = "Remove Certificate from the TLS truststore",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @DeleteMapping(value = "/entries/{alias:.+}")
    public String removeTLSCertificate(@PathVariable String alias) throws RequestValidationException {
        tlsTruststoreExtService.removeTLSCertificate(alias);
        return "Certificate [" + alias + "] has been successfully removed from the TLS truststore.";
    }

}
