package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.ext.rest.ErrorRO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TruststoreResource.class);

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

    @ExceptionHandler({CryptoException.class})
    public ResponseEntity<ErrorRO> handleCryptoException(CryptoException ex) {
        Throwable rootCause = ExceptionUtils.getRootCause(ex);
        rootCause = rootCause == null ? ex : rootCause;
        return errorHandlerService.createResponse(rootCause, HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/save")
    public String uploadTruststoreFile(@RequestPart("truststore") MultipartFile truststoreFile,
                                       @SkipWhiteListed @RequestParam("password") String password) throws IllegalArgumentException {
        byte[] truststoreFileContent = multiPartFileUtil.validateAndGetFileContent(truststoreFile);

        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException(ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD);
        }

        multiDomainCertificateProvider.replaceTrustStore(domainProvider.getCurrentDomain(), truststoreFile.getOriginalFilename(), truststoreFileContent, password);
        return "Truststore file has been successfully replaced.";
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadTrustStore() throws IOException {
        byte[] content = certificateService.getTruststoreContent();
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
        validateMaxRows(entries.size());

        return exportToCSV(entries,
                TrustStoreRO.class,
                ImmutableMap.of(
                        "ValidFrom".toUpperCase(), "Valid from",
                        "ValidUntil".toUpperCase(), "Valid until"
                ),
                Arrays.asList("fingerprints"),
                "truststore");
    }

}
