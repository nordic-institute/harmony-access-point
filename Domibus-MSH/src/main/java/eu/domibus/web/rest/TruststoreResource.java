package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
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

//    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TruststoreResource.class);

    public static final String ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD = "Failed to upload the truststoreFile file since its password was empty."; //NOSONAR

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    @Qualifier("TLSMultiDomainCryptoServiceImpl") //or a specific interface??
    protected MultiDomainCryptoService tlsMultiDomainCertificateProvider;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private ErrorHandlerService errorHandlerService;

    @Autowired
    MultiPartFileUtil multiPartFileUtil;

    @Autowired
    private AuditService auditService;

    @ExceptionHandler({CryptoException.class})
    public ResponseEntity<ErrorRO> handleCryptoException(CryptoException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/truststore/save")
    public String uploadTruststoreFile(@RequestPart("file") MultipartFile truststoreFile,
                                       @SkipWhiteListed @RequestParam("password") String password) throws IllegalArgumentException {
        replaceTruststore(multiDomainCertificateProvider, truststoreFile, password);

        // trigger update certificate table
        certificateService.saveCertificateAndLogRevocation(domainProvider.getCurrentDomain());

        return "Truststore file has been successfully replaced.";
    }

    @GetMapping(value = "/truststore/download", produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadTrustStore() {
//        byte[] content = certificateService.getTruststoreContent();

        return downloadTruststoreContent(multiDomainCertificateProvider);
    }

    @RequestMapping(value = {"/truststore/list"}, method = GET)
    public List<TrustStoreRO> trustStoreEntries() {
        return getTrustStoreEntries(multiDomainCertificateProvider);
    }

    /**
     * This method returns a CSV file with the contents of Truststore table
     *
     * @return CSV file with the contents of Truststore table
     */
    @GetMapping(path = "/truststore/csv")
    public ResponseEntity<String> getCsv() {
        final List<TrustStoreRO> entries = trustStoreEntries();
        getCsvService().validateMaxRows(entries.size());

        return exportToCSV(entries,
                TrustStoreRO.class,
                ImmutableMap.of(
                        "ValidFrom".toUpperCase(), "Valid from",
                        "ValidUntil".toUpperCase(), "Valid until"
                ),
                Arrays.asList("fingerprints"),
                "truststore");
    }

    @PostMapping(value = "/tlstruststore")
    public String uploadTLSTruststoreFile(@RequestPart("truststore") MultipartFile truststoreFile,
                                          @SkipWhiteListed @RequestParam("password") String password) throws RequestValidationException {
        replaceTruststore(tlsMultiDomainCertificateProvider, truststoreFile, password);
        return "TLS truststore file has been successfully replaced.";
    }

    @GetMapping(value = "/tlstruststore", produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadTLSTrustStore() throws IOException {
        return downloadTruststoreContent(tlsMultiDomainCertificateProvider);
    }

    @GetMapping(value = {"/tlstruststore/entries"})
    public List<TrustStoreRO> getTLSTruststoreEntries() {
        return getTrustStoreEntries(tlsMultiDomainCertificateProvider);
    }

    @PostMapping(value = "/tlstruststore/entries")
    public String addTLSCertificate(@RequestPart("file") MultipartFile certificateFile,
                                    @RequestParam("alias") @Valid @NotNull String alias) throws RequestValidationException {
        byte[] fileContent = multiPartFileUtil.validateAndGetFileContent(certificateFile);

        if (StringUtils.isBlank(alias)) {
            throw new IllegalArgumentException("Please provide an alisas for the secrtificate.");
        }

        X509Certificate cert = certificateService.loadCertificateFromString(new String(fileContent));

        tlsMultiDomainCertificateProvider.addCertificate(domainProvider.getCurrentDomain(), cert, alias, true);

        return "TLS certificate file has been successfully added.";
    }

    @DeleteMapping(value = "/tlstruststore/entries/{alias:.+}")
    public String removeTLSCertificate(@PathVariable String alias) throws RequestValidationException {
        tlsMultiDomainCertificateProvider.removeCertificate(domainProvider.getCurrentDomain(), alias);
        return "TLS certificate file has been successfully deleted.";
    }

    private void replaceTruststore(MultiDomainCryptoService certificateProvider, MultipartFile truststoreFile, String password) {
        byte[] truststoreFileContent = multiPartFileUtil.validateAndGetFileContent(truststoreFile);

        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException(ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD);
        }

        certificateProvider.replaceTrustStore(domainProvider.getCurrentDomain(), truststoreFile.getOriginalFilename(), truststoreFileContent, password);
    }

    private ResponseEntity<ByteArrayResource> downloadTruststoreContent(MultiDomainCryptoService multiDomainCertificateProvider) {
        byte[] content = multiDomainCertificateProvider.getTruststoreContent(domainProvider.getCurrentDomain());

        //todo: adjust this call for both truststores
        auditService.addTruststoreDownloadedAudit();

        ByteArrayResource resource = new ByteArrayResource(content);

        HttpStatus status = HttpStatus.OK;
        if (resource.getByteArray().length == 0) {
            status = HttpStatus.NO_CONTENT;
        }

        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header("content-disposition", "attachment; filename=TrustStore.jks")
                .body(resource);
    }

    private List<TrustStoreRO> getTrustStoreEntries(MultiDomainCryptoService multiDomainCertificateProvider) {
        final KeyStore store = multiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());
        List<TrustStoreEntry> trustStoreEntries = certificateService.getTrustStoreEntries(store);
        return domainConverter.convert(trustStoreEntries, TrustStoreRO.class);
    }
}
