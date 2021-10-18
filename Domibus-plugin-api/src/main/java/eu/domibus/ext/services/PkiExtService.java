package eu.domibus.ext.services;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Responsible for operations related to certificates.
 *
 * @author Thomas Dussart
 * @since 4.1
 */
public interface PkiExtService {

    /**
     * Given a chain of signing certificates (Trust chain + leaf), extract the leaf one.
     *
     * @param certificates list containing the trust chain and the leaf.
     * @return the leaf certificate.
     */
    Certificate extractLeafCertificateFromChain(List<? extends Certificate> certificates);

    /**
     * Get the certificates with the specified provider
     *
     * @param certificates the array of certificates.
     * @param provider the provider string (e.g. Bouncy Castle)
     * @return the array of certificates loaded with the given provider
     */
    X509Certificate[] getCertificatesWithProvider(X509Certificate[] certificates, String provider);

}
