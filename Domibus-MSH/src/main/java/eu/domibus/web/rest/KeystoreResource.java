package eu.domibus.web.rest;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RestController
@RequestMapping(value = "/rest/keystore")
public class KeystoreResource extends BaseResource {

    private final MultiDomainCryptoService multiDomainCertificateProvider;

    private final DomainContextProvider domainProvider;

    protected final ErrorHandlerService errorHandlerService;

    public KeystoreResource(MultiDomainCryptoService multiDomainCertificateProvider,
                            DomainContextProvider domainProvider, ErrorHandlerService errorHandlerService) {
        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.domainProvider = domainProvider;
        this.errorHandlerService = errorHandlerService;
    }

    @ExceptionHandler({CryptoException.class})
    public ResponseEntity<ErrorRO> handleCryptoException(CryptoException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/resets")
    public void reset() {
        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.resetKeyStore(currentDomain);
    }

}
