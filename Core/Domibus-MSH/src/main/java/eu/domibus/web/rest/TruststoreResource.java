package eu.domibus.web.rest;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.*;
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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.cert.X509Certificate;
import java.util.List;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

/**
 * @author Mircea Musat
 * @author Ion Perpegel
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/truststore")
public class TruststoreResource extends TruststoreResourceBase {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TruststoreResource.class);

    private final MultiDomainCryptoService multiDomainCertificateProvider;

    private final DomainContextProvider domainProvider;

    private final CertificateService certificateService;

    public TruststoreResource(MultiDomainCryptoService multiDomainCertificateProvider,
                              DomainContextProvider domainProvider,
                              CertificateService certificateService,
                              PartyCoreMapper partyConverter,
                              ErrorHandlerService errorHandlerService,
                              MultiPartFileUtil multiPartFileUtil,
                              AuditService auditService,
                              DomainContextProvider domainContextProvider,
                              DomibusConfigurationService domibusConfigurationService,
                              CertificateHelper certificateHelper,
                              KeystorePersistenceService keystorePersistenceService) {
        super(partyConverter, errorHandlerService, multiPartFileUtil, auditService, domainContextProvider, domibusConfigurationService,
                certificateHelper, keystorePersistenceService);

        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.domainProvider = domainProvider;
        this.certificateService = certificateService;
    }

    @PostMapping(value = "/save")
    public String uploadTruststoreFile(@RequestPart("file") MultipartFile truststoreFile,
                                       @SkipWhiteListed @RequestParam("password") String password) throws RequestValidationException {
        LOG.debug("Uploading file [{}] as the truststore for the current domain ", truststoreFile.getName());

        uploadStore(truststoreFile, password);

        return "Truststore file has been successfully replaced.";
    }

    @GetMapping(value = "/download", produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadTrustStore() {
        LOG.debug("Downloading the truststore as byte array for the current domain");

        return downloadTruststoreContent();
    }

    @PostMapping(value = "/reset")
    public void reset() {
        LOG.debug("Resetting the keystore for the current domain");

        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.resetTrustStore(currentDomain);
    }

    @GetMapping(value = {"/list"})
    public List<TrustStoreRO> trustStoreEntries() {
        LOG.debug("Listing entries of the truststore for the current domain");

        return getTrustStoreEntries();
    }

    @PostMapping(value = "/entries")
    public String addDomibusCertificate(@RequestPart("file") MultipartFile certificateFile,
                                        @RequestParam("alias") @Valid @NotNull String alias) throws RequestValidationException {
        return addCertificate(certificateFile, alias);
    }

    @DeleteMapping(value = "/entries/{alias:.+}")
    public String removeDomibusCertificate(@PathVariable String alias) throws RequestValidationException {
        return removeCertificate(alias);
    }

    @GetMapping(value = "/changedOnDisk")
    public boolean isChangedOnDisk() {
        LOG.debug("Checking if the truststore has changed on disk for the current domain");

        Domain currentDomain = domainProvider.getCurrentDomain();
        return multiDomainCertificateProvider.isTrustStoreChangedOnDisk(currentDomain);
    }

    @GetMapping(path = "/csv")
    public ResponseEntity<String> getEntriesAsCsv() {
        LOG.debug("Downloading the keystore as CSV for the current domain");
        return getEntriesAsCSV(getStoreName());
    }

    @Override
    protected void doUploadStore(KeyStoreContentInfo storeInfo) {
        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.replaceTrustStore(currentDomain, storeInfo);
    }

    @Override
    protected KeyStoreContentInfo getTrustStoreContent() {
        return multiDomainCertificateProvider.getTrustStoreContent(domainProvider.getCurrentDomain());
    }

    @Override
    protected List<TrustStoreEntry> doGetStoreEntries() {
        return multiDomainCertificateProvider.getTrustStoreEntries(domainProvider.getCurrentDomain());
    }

    @Override
    protected String getStoreName() {
        return DOMIBUS_TRUSTSTORE_NAME;
    }

    @Override
    protected boolean doAddCertificate(String alias, byte[] fileContent) {
        Domain currentDomain = domainProvider.getCurrentDomain();
        X509Certificate cert = certificateService.loadCertificate(fileContent);
        return multiDomainCertificateProvider.addCertificate(currentDomain, cert, alias, true);
    }

    @Override
    protected boolean doRemoveCertificate(String alias) {
        Domain currentDomain = domainProvider.getCurrentDomain();
        return multiDomainCertificateProvider.removeCertificate(currentDomain, alias);
    }
}
