package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.PartyCoreMapper;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Base class for TrustStore resources common functionality
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public abstract class TruststoreResourceBase extends BaseResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TruststoreResourceBase.class);

    public static final String ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD = "Failed to upload the truststoreFile file since its password was empty."; //NOSONAR

    protected final PartyCoreMapper partyConverter;

    protected final ErrorHandlerService errorHandlerService;

    protected final MultiPartFileUtil multiPartFileUtil;

    protected final AuditService auditService;

    public TruststoreResourceBase(PartyCoreMapper partyConverter, ErrorHandlerService errorHandlerService,
                                  MultiPartFileUtil multiPartFileUtil, AuditService auditService) {
        this.partyConverter = partyConverter;
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
        Pair<Long, byte[]> content = getTrustStoreContent();

        ByteArrayResource resource = new ByteArrayResource(content.getRight());

        HttpStatus status = HttpStatus.OK;
        if (resource.getByteArray().length == 0) {
            status = HttpStatus.NO_CONTENT;
        }

        auditDownload(content.getLeft());

        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header("content-disposition", "attachment; filename=TrustStore.jks")
                .body(resource);
    }

    protected abstract void auditDownload(Long id);

    protected abstract Pair<Long, byte[]> getTrustStoreContent();

    protected List<TrustStoreRO> getTrustStoreEntries() {
        List<TrustStoreEntry> trustStoreEntries = doGetTrustStoreEntries();
        return partyConverter.trustStoreEntryListToTrustStoreROList(trustStoreEntries);
    }

    protected abstract List<TrustStoreEntry> doGetTrustStoreEntries();

    protected ResponseEntity<String> getEntriesAsCSV(String moduleName) {
        List<TrustStoreRO> entries = Collections.emptyList();
        try {
            entries = getTrustStoreEntries();
        } catch (ConfigurationException ex) {
            LOG.error("Could not find TLS truststore.", ex);
        }
        getCsvService().validateMaxRows(entries.size());

        return exportToCSV(entries,
                TrustStoreRO.class,
                ImmutableMap.of(
                        "ValidFrom".toUpperCase(), "Valid from",
                        "ValidUntil".toUpperCase(), "Valid until"
                ),
                Arrays.asList("fingerprints", "certificateExpiryAlertDays"), moduleName);
    }
}
