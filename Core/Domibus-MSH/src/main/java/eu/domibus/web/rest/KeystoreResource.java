package eu.domibus.web.rest;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.crypto.TrustStoreContentDTO;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.PartyCoreMapper;
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

    private final MultiDomainCryptoService multiDomainCertificateProvider;

    private final DomainContextProvider domainProvider;

    private final CertificateService certificateService;

    private final ErrorHandlerService errorHandlerService;

    public KeystoreResource(MultiDomainCryptoService multiDomainCertificateProvider,
                            DomainContextProvider domainProvider, CertificateService certificateService,
                            PartyCoreMapper partyConverter, ErrorHandlerService errorHandlerService,
                            MultiPartFileUtil multiPartFileUtil, AuditService auditService) {
        super(partyConverter, errorHandlerService, multiPartFileUtil, auditService);

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
    protected void auditDownload(Long entityId) {
        auditService.addTruststoreDownloadedAudit(entityId != null ? entityId.toString() : getStoreName());
    }

    @Override
    protected TrustStoreContentDTO getTrustStoreContent() {
        return certificateService.getTruststoreContent(DOMIBUS_KEYSTORE_NAME);
    }

    @PostMapping(value = "/resets")
    public void reset() {
        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.resetKeyStore(currentDomain);
    }

    @GetMapping(value = {"/list"})
    public List<TrustStoreRO> listEntries() {
        return getTrustStoreEntries();
    }

    @GetMapping(path = "/csv")
    public ResponseEntity<String> getEntriesAsCsv() {
        return getEntriesAsCSV(getStoreName());
    }

    @Override
    protected List<TrustStoreEntry> doGetTrustStoreEntries() {
        return certificateService.getTrustStoreEntries(DOMIBUS_KEYSTORE_NAME);
    }

    @GetMapping(value = "/download", produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadKeystore() {
        return downloadTruststoreContent();
    }

    @PostMapping(value = "/save")
    public String uploadKeystoreFile(@RequestPart("file") MultipartFile keystoreFile,
                                       @SkipWhiteListed @RequestParam("password") String password) throws RequestValidationException {
        replaceTruststore(keystoreFile, password);

        return "Keystore file has been successfully replaced.";
    }

    @Override
    protected String getStoreName() {
        return "keystore";
    }
}
