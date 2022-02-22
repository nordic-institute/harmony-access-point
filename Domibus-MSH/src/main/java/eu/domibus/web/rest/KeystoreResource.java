package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.MultiDomainCryptoService;
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

    public KeystoreResource(MultiDomainCryptoService multiDomainCertificateProvider,
                            DomainContextProvider domainProvider) {
        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.domainProvider = domainProvider;
    }

    @PostMapping(value = "/resets")
    public void uploadTruststoreFile() {
        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.resetKeyStore(currentDomain);
    }

}
