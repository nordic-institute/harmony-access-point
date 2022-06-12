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
     * @param signingCertificateTrustChain the chain of certificate contained in the UserMessage.
     * @param signingCertificate the signing certificate.
     * @param userMessage the UserMessage
     */
    void authorize(
            List<X509Certificate> signingCertificateTrustChain,
            X509Certificate signingCertificate,
            UserMessage userMessage);


    /**
     * Authorize a PullRequest
     * @param signingCertificateTrustChain the chain of certificate contained in the UserMessage.
     * @param signingCertificate the signing certificate.
     * @param mpc the mpc contained in the pull request.
     */
    void authorize(
            List<X509Certificate> signingCertificateTrustChain,
            X509Certificate signingCertificate,
            String mpc);


}
