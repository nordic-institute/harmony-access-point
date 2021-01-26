package eu.domibus.web.rest;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.KeyStore;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

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
                              DomainCoreConverter domainConverter, ErrorHandlerService errorHandlerService,
                              MultiPartFileUtil multiPartFileUtil, AuditService auditService) {
        super(domainConverter, errorHandlerService, multiPartFileUtil, auditService);

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

    @RequestMapping(value = {"/list"}, method = GET)
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

        // trigger update certificate table
        final KeyStore trustStore = multiDomainCertificateProvider.getTrustStore(currentDomain);
        final KeyStore keyStore = multiDomainCertificateProvider.getKeyStore(currentDomain);
        certificateService.saveCertificateAndLogRevocation(trustStore, keyStore);
    }

    @Override
    protected void auditDownload() {
        auditService.addTruststoreDownloadedAudit();
    }

    @Override
    protected byte[] getTrustStoreContent() {
        return multiDomainCertificateProvider.getTruststoreContent(domainProvider.getCurrentDomain());
    }

    @Override
    protected List<TrustStoreEntry> doGetTrustStoreEntries() {
        final KeyStore store = multiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());
        return certificateService.getTrustStoreEntries(store);
    }

}
