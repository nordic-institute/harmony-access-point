package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.TLSCertificateManager;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RestController
@RequestMapping(value = "/rest/tlstruststore")
public class TLSTruststoreResource extends BaseResource {

    private final TLSCertificateManager tlsCertificateManager;

    private final DomainCoreConverter domainConverter;

    private final ErrorHandlerService errorHandlerService;

    private final MultiPartFileUtil multiPartFileUtil;

    private final AuditService auditService;

    public TLSTruststoreResource(TLSCertificateManager tlsCertificateManager,
                                 DomainCoreConverter domainConverter, ErrorHandlerService errorHandlerService,
                                 MultiPartFileUtil multiPartFileUtil, AuditService auditService) {
        this.tlsCertificateManager = tlsCertificateManager;
        this.domainConverter = domainConverter;
        this.errorHandlerService = errorHandlerService;
        this.multiPartFileUtil = multiPartFileUtil;
        this.auditService = auditService;
    }

    @ExceptionHandler({CryptoException.class})
    public ResponseEntity<ErrorRO> handleCryptoException(CryptoException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "")
    public String uploadTLSTruststoreFile(@RequestPart("file") MultipartFile truststoreFile,
                                          @SkipWhiteListed @RequestParam("password") String password) throws RequestValidationException {
        replaceTruststore(truststoreFile, password);
        return "TLS truststore file has been successfully replaced.";
    }

    @GetMapping(value = "", produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadTLSTrustStore() {
        return downloadTruststoreContent(() -> auditService.addTLSTruststoreDownloadedAudit());
    }

    @GetMapping(value = {"/entries"})
    public List<TrustStoreRO> getTLSTruststoreEntries() {
        return getTrustStoreEntries();
    }

    @GetMapping(path = "/entries/csv")
    public ResponseEntity<String> getTLSEntriesAsCsv() {
        return getEntriesAsCSV("tlsTruststore");
    }

    @PostMapping(value = "/entries")
    public String addTLSCertificate(@RequestPart("file") MultipartFile certificateFile,
                                    @RequestParam("alias") @Valid @NotNull String alias) throws RequestValidationException {
        byte[] fileContent = multiPartFileUtil.validateAndGetFileContent(certificateFile);

        if (StringUtils.isBlank(alias)) {
            throw new IllegalArgumentException("Please provide an alias for the certificate.");
        }

        tlsCertificateManager.addCertificate(fileContent, alias);

        return "Certificate [" + alias + "] has been successfully added to the TLS truststore.";
    }

    @DeleteMapping(value = "/entries/{alias:.+}")
    public String removeTLSCertificate(@PathVariable String alias) throws RequestValidationException {
        tlsCertificateManager.removeCertificate(alias);
        return "Certificate [" + alias + "] has been successfully removed from the TLS truststore.";
    }

    protected void replaceTruststore(MultipartFile truststoreFile, String password) {
        byte[] truststoreFileContent = multiPartFileUtil.validateAndGetFileContent(truststoreFile);

        if (StringUtils.isBlank(password)) {
            throw new RequestValidationException("Failed to upload the tls truststore file file since its password was empty.");
        }

        tlsCertificateManager.replaceTrustStore(truststoreFile.getOriginalFilename(), truststoreFileContent, password);
    }

    protected ResponseEntity<ByteArrayResource> downloadTruststoreContent(Runnable auditMethod) {
        byte[] content = tlsCertificateManager.getTruststoreContent();
        ByteArrayResource resource = new ByteArrayResource(content);

        HttpStatus status = HttpStatus.OK;
        if (resource.getByteArray().length == 0) {
            status = HttpStatus.NO_CONTENT;
        }

        auditMethod.run();

        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header("content-disposition", "attachment; filename=TrustStore.jks")
                .body(resource);
    }

    protected List<TrustStoreRO> getTrustStoreEntries() {
        List<TrustStoreEntry> trustStoreEntries = tlsCertificateManager.getTrustStoreEntries();
        return domainConverter.convert(trustStoreEntries, TrustStoreRO.class);
    }

    protected ResponseEntity<String> getEntriesAsCSV(String moduleName) {
        final List<TrustStoreRO> entries = getTrustStoreEntries();
        getCsvService().validateMaxRows(entries.size());

        return exportToCSV(entries,
                TrustStoreRO.class,
                ImmutableMap.of(
                        "ValidFrom".toUpperCase(), "Valid from",
                        "ValidUntil".toUpperCase(), "Valid until"
                ),
                Arrays.asList("fingerprints"), moduleName);
    }
}
