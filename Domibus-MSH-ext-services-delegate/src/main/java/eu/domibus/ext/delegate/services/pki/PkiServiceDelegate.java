package eu.domibus.ext.delegate.services.pki;

import eu.domibus.api.pki.CertificateService;
import eu.domibus.ext.services.PkiExtService;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.cert.Certificate;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
public class PkiServiceDelegate implements PkiExtService {

    @Autowired
    private CertificateService certificateService;

    @Override
    public Certificate extractLeafCertificateFromChain(final List<? extends Certificate> certificates) {
        return certificateService.extractLeafCertificateFromChain(certificates);
    }
}
