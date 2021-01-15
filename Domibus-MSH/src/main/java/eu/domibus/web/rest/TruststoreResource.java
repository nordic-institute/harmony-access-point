package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
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

import java.io.IOException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Mircea Musat
 * @author Ion Perpegel
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest")
public class TruststoreResource extends BaseResource {

    public static final String ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD = "Failed to upload the truststoreFile file since its password was empty."; //NOSONAR

    private MultiDomainCryptoService multiDomainCertificateProvider;

    private DomainContextProvider domainProvider;

    private CertificateService certificateService;

    private DomainCoreConverter domainConverter;

    private ErrorHandlerService errorHandlerService;

    private MultiPartFileUtil multiPartFileUtil;

    private AuditService auditService;

    public TruststoreResource(MultiDomainCryptoService multiDomainCertificateProvider,
                              DomainContextProvider domainProvider, CertificateService certificateService,
                              DomainCoreConverter domainConverter, ErrorHandlerService errorHandlerService,
                              MultiPartFileUtil multiPartFileUtil, AuditService auditService) {
        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.domainProvider = domainProvider;
        this.certificateService = certificateService;
        this.domainConverter = domainConverter;
        this.errorHandlerService = errorHandlerService;
        this.multiPartFileUtil = multiPartFileUtil;
        this.auditService = auditService;
    }

    @ExceptionHandler({CryptoException.class})
    public ResponseEntity<ErrorRO> handleCryptoException(CryptoException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/truststore/save")
    public String uploadTruststoreFile(@RequestPart("file") MultipartFile truststoreFile,
                                       @SkipWhiteListed @RequestParam("password") String password) throws IllegalArgumentException {
        replaceTruststore(multiDomainCertificateProvider, truststoreFile, password);

        // trigger update certificate table
        Domain currentDomain = domainProvider.getCurrentDomain();
        final KeyStore trustStore = multiDomainCertificateProvider.getTrustStore(currentDomain);
        final KeyStore keyStore = multiDomainCertificateProvider.getKeyStore(currentDomain);
        certificateService.saveCertificateAndLogRevocation(trustStore, keyStore);

        return "Truststore file has been successfully replaced.";
    }

    @GetMapping(value = "/truststore/download", produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadTrustStore() {
        return downloadTruststoreContent(() -> auditService.addTruststoreDownloadedAudit());
    }

    @RequestMapping(value = {"/truststore/list"}, method = GET)
    public List<TrustStoreRO> trustStoreEntries() {
        return getTrustStoreEntries(multiDomainCertificateProvider);
    }

    @GetMapping(path = "/truststore/csv")
    public ResponseEntity<String> getEntriesAsCsv() {
        return getEntriesAsCSV(multiDomainCertificateProvider, "truststore");
    }

    protected void replaceTruststore(MultiDomainCryptoService certificateProvider, MultipartFile truststoreFile, String password) {
        byte[] truststoreFileContent = multiPartFileUtil.validateAndGetFileContent(truststoreFile);

        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException(ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD);
        }

        certificateProvider.replaceTrustStore(domainProvider.getCurrentDomain(), truststoreFile.getOriginalFilename(), truststoreFileContent, password);
    }

    protected ResponseEntity<ByteArrayResource> downloadTruststoreContent(Runnable auditMethod) {
        Domain currentDomain = domainProvider.getCurrentDomain();
        byte[] content = multiDomainCertificateProvider.getTruststoreContent(currentDomain);
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

    protected List<TrustStoreRO> getTrustStoreEntries(MultiDomainCryptoService multiDomainCertificateProvider) {
        final KeyStore store = multiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());
        List<TrustStoreEntry> trustStoreEntries = certificateService.getTrustStoreEntries(store);
        return domainConverter.convert(trustStoreEntries, TrustStoreRO.class);
    }

    protected ResponseEntity<String> getEntriesAsCSV(MultiDomainCryptoService cryptoService, String moduleName) {
        final List<TrustStoreRO> entries = getTrustStoreEntries(cryptoService);
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
