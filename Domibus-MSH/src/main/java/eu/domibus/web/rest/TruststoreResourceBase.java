package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * Base class for TrustStore resources common functionality
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public abstract class TruststoreResourceBase extends BaseResource {

    public static final String ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD = "Failed to upload the truststoreFile file since its password was empty."; //NOSONAR

    protected final DomainCoreConverter domainConverter;

    protected final ErrorHandlerService errorHandlerService;

    protected final MultiPartFileUtil multiPartFileUtil;

    protected final AuditService auditService;

    public TruststoreResourceBase(DomainCoreConverter domainConverter, ErrorHandlerService errorHandlerService,
                                  MultiPartFileUtil multiPartFileUtil, AuditService auditService) {
        this.domainConverter = domainConverter;
        this.errorHandlerService = errorHandlerService;
        this.multiPartFileUtil = multiPartFileUtil;
        this.auditService = auditService;
    }

    @ExceptionHandler({CryptoException.class})
    public ResponseEntity<ErrorRO> handleCryptoException(CryptoException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    protected void replaceTruststore(MultipartFile truststoreFile, String password) {
        byte[] truststoreFileContent = multiPartFileUtil.validateAndGetFileContent(truststoreFile);

        if (StringUtils.isBlank(password)) {
            throw new RequestValidationException(ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD);
        }

        doReplaceTrustStore(truststoreFileContent, truststoreFile.getOriginalFilename(), password);
    }

    protected abstract void doReplaceTrustStore(byte[] truststoreFileContent, String fileName, String password);

    protected ResponseEntity<ByteArrayResource> downloadTruststoreContent() {
        byte[] content = getTrustStoreContent();

        ByteArrayResource resource = new ByteArrayResource(content);

        HttpStatus status = HttpStatus.OK;
        if (resource.getByteArray().length == 0) {
            status = HttpStatus.NO_CONTENT;
        }

        auditDownload();

        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header("content-disposition", "attachment; filename=TrustStore.jks")
                .body(resource);
    }

    protected abstract void auditDownload();

    protected abstract byte[] getTrustStoreContent();

    protected List<TrustStoreRO> getTrustStoreEntries() {
        List<TrustStoreEntry> trustStoreEntries = doGetTrustStoreEntries();
        return domainConverter.convert(trustStoreEntries, TrustStoreRO.class);
    }

    protected abstract List<TrustStoreEntry> doGetTrustStoreEntries();

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
