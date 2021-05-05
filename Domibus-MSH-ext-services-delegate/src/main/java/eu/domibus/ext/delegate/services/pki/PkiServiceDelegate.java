package eu.domibus.ext.delegate.services.pki;

import eu.domibus.api.pki.CertificateService;
import eu.domibus.ext.services.PkiExtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.cert.Certificate;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.1
 *
 * Delegate class allowing Domibus extension/plugin to use some certificate related functions of Domibus.
 */
@Service
public class PkiServiceDelegate implements PkiExtService {

    @Autowired
    protected CertificateService certificateService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Certificate extractLeafCertificateFromChain(final List<? extends Certificate> certificates) {
        return certificateService.extractLeafCertificateFromChain(certificates);
    }
}
