package eu.domibus.ext.services;

import java.security.cert.Certificate;
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

}
