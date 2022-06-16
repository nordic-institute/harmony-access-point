package eu.domibus.web.rest;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.crypto.TrustStoreContentDTO;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.PartyCoreMapper;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    }

    @Override
    protected void auditDownload(Long id) {

    }

    @Override
    protected TrustStoreContentDTO getTrustStoreContent() {
        return null;
    }

    @Override
    protected List<TrustStoreEntry> doGetTrustStoreEntries() {
        return null;
    }

    @PostMapping(value = "/resets")
    public void reset() {
        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.resetKeyStore(currentDomain);
    }

}
