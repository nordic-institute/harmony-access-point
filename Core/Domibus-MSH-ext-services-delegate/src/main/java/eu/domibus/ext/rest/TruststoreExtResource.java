package eu.domibus.ext.rest;


import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.util.DateUtil;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.KeyStoreContentInfoDTO;
import eu.domibus.ext.domain.TrustStoreDTO;
import eu.domibus.ext.exceptions.CryptoExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.TrustStoreExtService;
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
import java.time.LocalDateTime;
import java.util.List;

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

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TruststoreExtResource.class);

    public static final String ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD = "Failed to upload the truststoreFile file since its password was empty.";
    public static final String DOMIBUS_TRUSTSTORE = "domibus.truststore";

    final TrustStoreExtService truststoreExtService;

    final ExtExceptionHelper extExceptionHelper;

    private final MultiPartFileUtil multiPartFileUtil;

    final DomainContextExtService domainContextExtService;

    final DomibusConfigurationExtService domibusConfigurationExtService;

    public TruststoreExtResource(TrustStoreExtService truststoreExtService, ExtExceptionHelper extExceptionHelper,
                                 MultiPartFileUtil multiPartFileUtil, DomainContextExtService domainContextExtService,
                                 DomibusConfigurationExtService domibusConfigurationExtService) {
        this.truststoreExtService = truststoreExtService;
        this.extExceptionHelper = extExceptionHelper;
        this.multiPartFileUtil = multiPartFileUtil;
        this.domainContextExtService = domainContextExtService;
        this.domibusConfigurationExtService = domibusConfigurationExtService;
    }

    @ExceptionHandler(CryptoExtException.class)
    protected ResponseEntity<ErrorDTO> handleTrustStoreExtException(CryptoExtException e) {
        return extExceptionHelper.handleExtException(e);
    }

    @Operation(summary = "Download truststore", description = "Upload the truststore file",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @GetMapping(value = "/download", produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadTrustStore() {
        KeyStoreContentInfoDTO info;
        byte[] content;
        try {
            info = truststoreExtService.downloadTruststoreContent();
            content = info.getContent();
        } catch (Exception e) {
            LOG.error("Could not find truststore.", e);
            return ResponseEntity.notFound().build();
        }

        HttpStatus status = HttpStatus.OK;
        if (content.length == 0) {
            status = HttpStatus.NO_CONTENT;
        }
        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header("content-disposition", "attachment; filename=" + getFileName())
                .body(new ByteArrayResource(content));
    }

    @Operation(summary = "Get truststore entries", description = "Get the truststore details",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @GetMapping(value = {"/entries"})
    public List<TrustStoreDTO> getTruststoreEntries() {
        return truststoreExtService.getTrustStoreEntries();
    }

    @Operation(summary = "Upload truststore", description = "Upload the truststore file",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PostMapping(consumes = {"multipart/form-data"})
    public String uploadTruststoreFile(
            @RequestPart("file") MultipartFile truststoreFile,
            @SkipWhiteListed @RequestParam("password") String password) {

        byte[] truststoreFileContent = multiPartFileUtil.validateAndGetFileContent(truststoreFile);

        if (StringUtils.isBlank(password)) {
            throw new RequestValidationException(ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD);
        }

        KeyStoreContentInfoDTO contentInfo = new KeyStoreContentInfoDTO(DOMIBUS_TRUSTSTORE, truststoreFileContent, truststoreFile.getOriginalFilename(), password);
        truststoreExtService.uploadTruststoreFile(contentInfo);

        return "Truststore file has been successfully replaced.";
    }

    @Operation(summary = "Add Certificate", description = "Add Certificate to the truststore",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PostMapping(value = "/entries", consumes = {"multipart/form-data"})
    public String addCertificate(@RequestPart("file") MultipartFile certificateFile,
                                 @RequestParam("alias") @Valid @NotNull String alias) throws RequestValidationException {

        if (StringUtils.isBlank(alias)) {
            throw new RequestValidationException("Please provide an alias for the certificate.");
        }

        byte[] fileContent = multiPartFileUtil.validateAndGetFileContent(certificateFile);

        truststoreExtService.addCertificate(fileContent, alias);
        return "Certificate [" + alias + "] has been successfully added to the truststore.";
    }

    @Operation(summary = "Remove Certificate", description = "Remove Certificate from the truststore",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @DeleteMapping(value = "/entries/{alias:.+}")
    public String removeCertificate(@PathVariable String alias) throws RequestValidationException {
        truststoreExtService.removeCertificate(alias);
        return "Certificate [" + alias + "] has been successfully removed from the truststore.";
    }

    private String getFileName() {
        String fileName = DOMIBUS_TRUSTSTORE;
        DomainDTO domain = domainContextExtService.getCurrentDomainSafely();
        if (domibusConfigurationExtService.isMultiTenantAware() && domain != null) {
            fileName = fileName + "_" + domain.getName();
        }
        fileName = fileName + "_"
                + LocalDateTime.now().format(DateUtil.DEFAULT_FORMATTER)
                + "." + truststoreExtService.getStoreFileExtension();
        return fileName;
    }
}
