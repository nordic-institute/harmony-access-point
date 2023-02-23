package eu.domibus.web.rest;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.pki.KeystorePersistenceService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.converter.PartyCoreMapper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.springframework.core.io.ByteArrayResource;
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

    private final ErrorHandlerService errorHandlerService;

    public KeystoreResource(MultiDomainCryptoService multiDomainCertificateProvider,
                            DomainContextProvider domainProvider,
                            PartyCoreMapper partyConverter,
                            ErrorHandlerService errorHandlerService,
                            MultiPartFileUtil multiPartFileUtil,
                            AuditService auditService,
                            DomainContextProvider domainContextProvider,
                            DomibusConfigurationService domibusConfigurationService,
                            CertificateHelper certificateHelper,
                            KeystorePersistenceService keystorePersistenceService) {
        super(partyConverter, errorHandlerService, multiPartFileUtil, auditService, domainContextProvider,
                domibusConfigurationService, certificateHelper, keystorePersistenceService);

        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.domainProvider = domainProvider;
        this.errorHandlerService = errorHandlerService;
    }

    @Override
    protected void doUploadStore(KeyStoreContentInfo storeInfo) {
        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.replaceKeyStore(currentDomain, storeInfo);
    }

    @Override
    protected KeyStoreContentInfo getTrustStoreContent() {
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
        Domain currentDomain = domainProvider.getCurrentDomain();
        return multiDomainCertificateProvider.isKeyStoreChangedOnDisk(currentDomain);
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

        uploadStore(keystoreFile, password);

        return "Keystore file has been successfully replaced.";
    }

    @Override
    protected String getStoreName() {
        return DOMIBUS_KEYSTORE_NAME;
    }

    @Override
    protected boolean doAddCertificate(String alias, byte[] fileContent) {
        return false;
    }

    @Override
    protected boolean doRemoveCertificate(String alias) {
        return false;
    }
}
