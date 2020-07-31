package eu.domibus.core.certificate.crl;

import java.security.cert.X509Certificate;

/**
 * Created by Cosmin Baciu on 07-Jul-16.
 */
public interface CRLService {

    /**
     * Extracts the CRL distribution points from the pki (if available)
     * and checks the pki revocation status against the CRLs coming from
     * the distribution points. Supports HTTP, HTTPS, FTP, File based URLs.
     *
     * @param cert the pki to be checked for revocation
     * @return true if the pki is revoked
     * @throws DomibusCRLException if the CRLs from the pki could not be retrieved
     */
    boolean isCertificateRevoked(X509Certificate cert) throws DomibusCRLException;

    /**
     * Checks the pki revocation status against the CRLs coming from
     * the distribution points. Supports HTTP, HTTPS, FTP, File based URLs.
     *
     * @param cert                    the pki to be checked for revocation
     * @param crlDistributionPointURL URI of the CRL
     * @return true if the pki is revoked
     */
    boolean isCertificateRevoked(X509Certificate cert, String crlDistributionPointURL);

    /**
     * Reset cache and Crl Protocols
     */
    void resetCacheCrlProtocols();
}
