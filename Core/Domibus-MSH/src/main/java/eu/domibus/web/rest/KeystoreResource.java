package eu.domibus.web.rest;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.KeyStoreInfo;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.PartyCoreMapper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_KEYSTORE_NAME;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RestController
@RequestMapping(value = "/rest/keystore")
public class KeystoreResource extends TruststoreResourceBase {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(KeystoreResource.class);

    private final MultiDomainCryptoService multiDomainCertificateProvider;

    private final DomainContextProvider domainProvider;

    private final CertificateService certificateService;

    private final ErrorHandlerService errorHandlerService;

    public KeystoreResource(MultiDomainCryptoService multiDomainCertificateProvider,
                            DomainContextProvider domainProvider, CertificateService certificateService,
                            PartyCoreMapper partyConverter, ErrorHandlerService errorHandlerService,
                            MultiPartFileUtil multiPartFileUtil, AuditService auditService, DomainContextProvider domainContextProvider, DomibusConfigurationService domibusConfigurationService) {
        super(partyConverter, errorHandlerService, multiPartFileUtil, auditService, domainContextProvider, domibusConfigurationService);

        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.domainProvider = domainProvider;
        this.certificateService = certificateService;
        this.errorHandlerService = errorHandlerService;
    }

    @ExceptionHandler({CryptoException.class})
    public ResponseEntity<ErrorRO> handleCryptoException(CryptoException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected void doReplaceTrustStore(byte[] truststoreFileContent, String fileName, String password) {
        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.replaceKeyStore(currentDomain, fileName, truststoreFileContent, password);
    }

    @Override
    protected void auditDownload() {
        auditService.addTruststoreDownloadedAudit(getStoreName());
    }

    @Override
    protected KeyStoreInfo getTrustStoreContent() {
        return multiDomainCertificateProvider.getKeyStoreContent(domainProvider.getCurrentDomain());
    }

    @PostMapping(value = "/reset")
    public void reset() {
        LOG.debug("Resetting the keystore for the current domain");
        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.resetKeyStore(currentDomain);
    }

    @GetMapping(value = {"/list"})
    public List<TrustStoreRO> listEntries() {
        LOG.debug("Listing entries of the keystore for the current domain");
        return getTrustStoreEntries();
    }

    @GetMapping(value = "/changedOnDisk")
    public boolean isChangedOnDisk() {
        LOG.debug("Checking if the keystore has changed on disk for the current domain");
        return certificateService.isStoreNewerOnDisk(DOMIBUS_KEYSTORE_NAME);
    }

    @GetMapping(path = "/csv")
    public ResponseEntity<String> getEntriesAsCsv() {
        LOG.debug("Downloading the keystore as CSV for the current domain");
        return getEntriesAsCSV(getStoreName());
    }

    @Override
    protected List<TrustStoreEntry> doGetStoreEntries() {
        return multiDomainCertificateProvider.getKeyStoreEntries(domainProvider.getCurrentDomain());
    }

    @GetMapping(value = "/download", produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadKeystore() {
        LOG.debug("Downloading the keystore as byte array for the current domain");
        return downloadTruststoreContent();
    }

    @PostMapping(value = "/save")
    public String uploadKeystoreFile(@RequestPart("file") MultipartFile keystoreFile,
                                     @SkipWhiteListed @RequestParam("password") String password) throws RequestValidationException {
        LOG.debug("Uploading file [{}] as the keystore for the current domain ", keystoreFile.getName());

        replaceTruststore(keystoreFile, password);

        return "Keystore file has been successfully replaced.";
    }

    @Override
    protected String getStoreName() {
        return "keystore";
    }

    @Override
    protected void doAddCertificate(String alias, byte[] fileContent) {
    }
}
