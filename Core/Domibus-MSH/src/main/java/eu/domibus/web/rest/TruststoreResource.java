package eu.domibus.web.rest;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.KeyStoreInfo;
import eu.domibus.api.pki.KeystorePersistenceService;
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
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.cert.X509Certificate;
import java.util.List;

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

    private final KeystorePersistenceService keystorePersistenceService;

    public TruststoreResource(MultiDomainCryptoService multiDomainCertificateProvider,
                              DomainContextProvider domainProvider, CertificateService certificateService,
                              PartyCoreMapper partyConverter, ErrorHandlerService errorHandlerService,
                              MultiPartFileUtil multiPartFileUtil, AuditService auditService, DomainContextProvider domainContextProvider,
                              DomibusConfigurationService domibusConfigurationService, KeystorePersistenceService keystorePersistenceService) {
        super(partyConverter, errorHandlerService, multiPartFileUtil, auditService, domainContextProvider, domibusConfigurationService);

        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.domainProvider = domainProvider;
        this.certificateService = certificateService;
        this.keystorePersistenceService = keystorePersistenceService;
    }

    @PostMapping(value = "/save")
    public String uploadTruststoreFile(@RequestPart("file") MultipartFile truststoreFile,
                                       @SkipWhiteListed @RequestParam("password") String password) throws RequestValidationException {
        LOG.debug("Uploading file [{}] as the truststore for the current domain ", truststoreFile.getName());

        replaceTruststore(truststoreFile, password);

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
    public String removeCertificate(@PathVariable String alias) throws RequestValidationException {
        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.removeCertificate(currentDomain, alias);
        return "Certificate [" + alias + "] has been successfully removed from the domibus truststore.";
    }

    @GetMapping(value = "/changedOnDisk")
    public boolean isChangedOnDisk() {
        LOG.debug("Checking if the truststore has changed on disk for the current domain");

        Domain currentDomain = domainProvider.getCurrentDomain();
        return multiDomainCertificateProvider.isTrustStoreChangedOnDisk(currentDomain);
//        return certificateService.isStoreChangedOnDisk(keystorePersistenceService.getTrustStorePersistenceInfo());
    }

    @GetMapping(path = "/csv")
    public ResponseEntity<String> getEntriesAsCsv() {
        LOG.debug("Downloading the keystore as CSV for the current domain");
        return getEntriesAsCSV(getStoreName());
    }

    @Override
    protected void doReplaceTrustStore(byte[] truststoreFileContent, String fileName, String password) {
        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.replaceTrustStore(currentDomain, fileName, truststoreFileContent, password);
    }

    @Override
    protected void auditDownload() {
        auditService.addTruststoreDownloadedAudit(getStoreName());
    }

    @Override
    protected KeyStoreInfo getTrustStoreContent() {
        return multiDomainCertificateProvider.getTrustStoreContent(domainProvider.getCurrentDomain());
    }

    @Override
    protected List<TrustStoreEntry> doGetStoreEntries() {
        return multiDomainCertificateProvider.getTrustStoreEntries(domainProvider.getCurrentDomain());
    }

    @Override
    protected String getStoreName() {
        return "truststore";
    }

    @Override
    protected void doAddCertificate(String alias, byte[] fileContent) {
        Domain currentDomain = domainProvider.getCurrentDomain();
        X509Certificate cert = certificateService.loadCertificateFromByteArray(fileContent);
        multiDomainCertificateProvider.addCertificate(currentDomain, cert, alias, true);
    }
}
