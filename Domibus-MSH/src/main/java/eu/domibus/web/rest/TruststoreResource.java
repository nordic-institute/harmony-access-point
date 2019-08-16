package eu.domibus.web.rest;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.csv.CsvCustomColumns;
import eu.domibus.core.csv.CsvExcludedItems;
import eu.domibus.core.csv.CsvService;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.ext.rest.ErrorRO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.KeyStore;
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

    public static final String ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD = "Failed to upload the truststore file since its password was empty.";

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private CsvServiceImpl csvServiceImpl;


    @Autowired
    private ErrorHandlerService errorHandlerService;

    @ExceptionHandler({CryptoException.class})
    public ResponseEntity<ErrorRO> handleCryptoException(CryptoException ex) {
        Throwable rootCause = ExceptionUtils.getRootCause(ex);
        rootCause = rootCause == null ? ex : rootCause;
        return errorHandlerService.createResponse(rootCause, HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/save")
    public ResponseEntity<String> uploadTruststoreFile(@RequestPart("truststore") MultipartFile truststore,
                                                       @RequestParam("password") String password) throws IOException {
        if (truststore.isEmpty()) {
            return ResponseEntity.badRequest().body("Failed to upload the truststore file since it was empty.");
        }
        if (StringUtils.isBlank(password)) {
            return ResponseEntity.badRequest().body(ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD);
        }

        multiDomainCertificateProvider.replaceTrustStore(domainProvider.getCurrentDomain(), truststore.getOriginalFilename(), truststore.getBytes(), password);
        return ResponseEntity.ok("Truststore file has been successfully replaced.");
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
        final List<TrustStoreRO> trustStoreROS = trustStoreEntries();

        return exportToCSV(trustStoreROS, TrustStoreRO.class,
                CsvCustomColumns.TRUSTSTORE_RESOURCE.getCustomColumns(),
                CsvExcludedItems.TRUSTSTORE_RESOURCE.getExcludedItems(),
                "truststore");

    }

    @Override
    public CsvService getCsvService() {
        return csvServiceImpl;
    }
}
