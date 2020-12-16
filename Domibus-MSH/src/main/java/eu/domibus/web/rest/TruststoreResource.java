package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
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

import java.io.IOException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Mircea Musat
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/truststore")
public class TruststoreResource extends BaseResource {

//    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TruststoreResource.class);

    public static final String ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD = "Failed to upload the truststoreFile file since its password was empty."; //NOSONAR

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;

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

    @PostMapping(value = "/save")
    public String uploadTruststoreFile(@RequestPart("truststore") MultipartFile truststoreFile,
                                       @SkipWhiteListed @RequestParam("password") String password) throws IllegalArgumentException {
        byte[] truststoreFileContent = multiPartFileUtil.validateAndGetFileContent(truststoreFile);

        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException(ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD);
        }

        multiDomainCertificateProvider.replaceTrustStore(domainProvider.getCurrentDomain(), truststoreFile.getOriginalFilename(), truststoreFileContent, password);
        // trigger update certificate table
        certificateService.saveCertificateAndLogRevocation(domainProvider.getCurrentDomain());
        return "Truststore file has been successfully replaced.";
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadTrustStore() throws IOException {
        byte[] content = certificateService.getTruststoreContent();

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

    @RequestMapping(value = {"/list"}, method = GET)
    public List<TrustStoreRO> trustStoreEntries() {
        final KeyStore trustStore = multiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());
        return domainConverter.convert(certificateService.getTrustStoreEntries(trustStore), TrustStoreRO.class);
    }

    /**
     * This method returns a CSV file with the contents of Truststore table
     *
     * @return CSV file with the contents of Truststore table
     */
    @GetMapping(path = "/csv")
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

    // TLS truststore
    @Autowired
    @Qualifier("TLSMultiDomainCryptoServiceImpl")
    protected MultiDomainCryptoService tlsMultiDomainCertificateProvider;

    @PostMapping(value = "/tls")
    public String uploadTLSTruststoreFile(@RequestPart("truststore") MultipartFile file,
                                          @SkipWhiteListed @RequestParam("password") String password)
            throws RequestValidationException {

        byte[] fileContent = multiPartFileUtil.validateAndGetFileContent(file);

        if (StringUtils.isBlank(password)) {
            throw new RequestValidationException(ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD);
        }

        tlsMultiDomainCertificateProvider.replaceTrustStore(domainProvider.getCurrentDomain(), file.getOriginalFilename(), fileContent, password);

        return "TLS truststore file has been successfully replaced.";
    }

    @GetMapping(value = "/tls", produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadTLSTrustStore() throws IOException {
        byte[] content = certificateService.getTruststoreContent();

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

    @GetMapping(value = {"/tls/entries"})
    public List<TrustStoreRO> getTLSTruststoreEntries() {
        final KeyStore trustStore = tlsMultiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());
        return domainConverter.convert(certificateService.getTrustStoreEntries(trustStore), TrustStoreRO.class);
    }
}
