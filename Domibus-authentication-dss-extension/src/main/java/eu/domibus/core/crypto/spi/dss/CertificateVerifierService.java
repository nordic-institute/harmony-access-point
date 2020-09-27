package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.validation.CertificateVerifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class in charge of reloading the CertificateVerifier.
 *
 * @author Thomas Dussart
 * @since 4.2
 */
public class CertificateVerifierService {

    private final DssCache dssCache;

    public CertificateVerifierService(DssCache dssCache) {
        this.dssCache = dssCache;
    }

    @Autowired
    protected ObjectProvider<CertificateVerifier> certificateVerifierObjectProvider;

    public CertificateVerifier getCertificateVerifier() {
        return certificateVerifierObjectProvider.getObject();
    }

    public void clearCertificateVerifier() {
        dssCache.clear();
    }

}
