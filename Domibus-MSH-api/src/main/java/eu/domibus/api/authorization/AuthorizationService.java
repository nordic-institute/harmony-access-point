package eu.domibus.api.authorization;

import eu.domibus.api.usermessage.domain.UserMessage;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
public interface AuthorizationService {

    /**
     * Authorize a UserMessage
     * @param signingCertificateTrustChain the
     * @param signingCertificate
     * @param userMessage
     */
    void authorize(
            List<X509Certificate> signingCertificateTrustChain,
            X509Certificate signingCertificate,
            UserMessage userMessage);

    
    void authorize(
            List<X509Certificate> signingCertificateTrustChain,
            X509Certificate signingCertificate,
            String mpc);


}
