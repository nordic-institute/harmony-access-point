package eu.domibus.web.rest;

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
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    private final MultiDomainCryptoService multiDomainCertificateProvider;

    private final DomainContextProvider domainProvider;

    private final CertificateService certificateService;

    public TruststoreResource(MultiDomainCryptoService multiDomainCertificateProvider,
                              DomainContextProvider domainProvider, CertificateService certificateService,
                              PartyCoreMapper partyConverter, ErrorHandlerService errorHandlerService,
                              MultiPartFileUtil multiPartFileUtil, AuditService auditService) {
        super(partyConverter, errorHandlerService, multiPartFileUtil, auditService);

        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.domainProvider = domainProvider;
        this.certificateService = certificateService;
    }

    @PostMapping(value = "/save")
    public String uploadTruststoreFile(@RequestPart("file") MultipartFile truststoreFile,
                                       @SkipWhiteListed @RequestParam("password") String password) throws RequestValidationException {
        replaceTruststore(truststoreFile, password);

        return "Truststore file has been successfully replaced.";
    }

    @GetMapping(value = "/download", produces = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadTrustStore() {
        return downloadTruststoreContent();
    }

    @GetMapping(value = {"/list"})
    public List<TrustStoreRO> trustStoreEntries() {
        return getTrustStoreEntries();
    }

    @GetMapping(path = "/csv")
    public ResponseEntity<String> getEntriesAsCsv() {
        return getEntriesAsCSV("truststore");
    }

    @Override
    protected void doReplaceTrustStore(byte[] truststoreFileContent, String fileName, String password) {
        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.replaceTrustStore(currentDomain, fileName, truststoreFileContent, password);
    }

    @Override
    protected void auditDownload(Long entityId) {
        auditService.addTruststoreDownloadedAudit(entityId != null ? entityId.toString() : "truststore");
    }

    @Override
    protected TrustStoreContentDTO getTrustStoreContent() {
        return multiDomainCertificateProvider.getTruststoreContent(domainProvider.getCurrentDomain());
    }

    @Override
    protected List<TrustStoreEntry> doGetTrustStoreEntries() {
        return certificateService.getTrustStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
    }

}
