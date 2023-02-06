package eu.domibus.web.rest;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.pki.KeystorePersistenceService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.converter.PartyCoreMapper;
import eu.domibus.api.crypto.TLSCertificateManager;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static eu.domibus.api.crypto.TLSCertificateManager.TLS_TRUSTSTORE_NAME;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RestController
@RequestMapping(value = "/rest/tlstruststore")
public class TLSTruststoreResource extends TruststoreResourceBase {

    private final TLSCertificateManager tlsCertificateManager;

    public TLSTruststoreResource(TLSCertificateManager tlsCertificateManager,
                                 PartyCoreMapper coreMapper,
                                 ErrorHandlerService errorHandlerService,
                                 MultiPartFileUtil multiPartFileUtil,
                                 AuditService auditService,
                                 DomainContextProvider domainContextProvider,
                                 DomibusConfigurationService domibusConfigurationService,
                                 CertificateHelper certificateHelper,
                                 KeystorePersistenceService keystorePersistenceService) {
        super(coreMapper, errorHandlerService, multiPartFileUtil, auditService, domainContextProvider, domibusConfigurationService,
                certificateHelper, keystorePersistenceService);
        this.tlsCertificateManager = tlsCertificateManager;
    }

    @PostMapping()
    public String uploadTLSTruststoreFile(@RequestPart("file") MultipartFile file,
                                          @SkipWhiteListed @RequestParam("password") String password) throws RequestValidationException {
        uploadStore(file, password);
        return "TLS truststore file has been successfully replaced.";
    }

    @GetMapping(produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadTLSTrustStore() {
        return downloadTruststoreContent();
    }

    @GetMapping(value = {"/entries"})
    public List<TrustStoreRO> getTLSTruststoreEntries() {
        return getTrustStoreEntries();
    }

    @GetMapping(path = "/entries/csv")
    public ResponseEntity<String> getTLSEntriesAsCsv() {
        return getEntriesAsCSV(getStoreName());
    }

    @PostMapping(value = "/entries")
    public String addTLSCertificate(@RequestPart("file") MultipartFile certificateFile,
                                    @RequestParam("alias") @Valid @NotNull String alias) throws RequestValidationException {
        return addCertificate(certificateFile, alias);
    }

    @DeleteMapping(value = "/entries/{alias:.+}")
    public String removeTLSCertificate(@PathVariable String alias) throws RequestValidationException {
        return removeCertificate(alias);
    }

    @Override
    protected void doUploadStore(KeyStoreContentInfo storeInfo) {
        tlsCertificateManager.replaceTrustStore(storeInfo);
    }

    @Override
    protected KeyStoreContentInfo getTrustStoreContent() {
        return tlsCertificateManager.getTruststoreContent();
    }

    @Override
    protected List<TrustStoreEntry> doGetStoreEntries() {
        return tlsCertificateManager.getTrustStoreEntries();
    }

    @Override
    protected String getStoreName() {
        return TLS_TRUSTSTORE_NAME;
    }

    @Override
    protected boolean doAddCertificate(String alias, byte[] fileContent) {
        return tlsCertificateManager.addCertificate(fileContent, alias);
    }

    @Override
    protected boolean doRemoveCertificate(String alias) {
        return tlsCertificateManager.removeCertificate(alias);
    }

}
