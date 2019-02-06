package eu.domibus.core.crypto.api;

import org.apache.wss4j.common.ext.WSSecurityException;

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface X509PkiPathTrustProvider {
    void verifyTrust(
            X509Certificate[] certs, boolean enableRevocation,
            Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints
    ) throws WSSecurityException;
}
