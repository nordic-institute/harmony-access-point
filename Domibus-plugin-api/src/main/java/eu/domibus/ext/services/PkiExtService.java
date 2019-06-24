package eu.domibus.ext.services;

import java.security.cert.Certificate;
import java.util.List;

/**
 * Responsible for operations related to the certificates.
 *
 * @author Thomas Dussart
 * @since 4.1
 */
public interface PkiExtService {


    Certificate extractLeafCertificateFromChain(List<? extends Certificate> certificates);

}
