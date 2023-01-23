package eu.domibus.api.authorization;

import eu.domibus.api.security.SecurityProfile;
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
     * @param securityProfile the security profile defined in pMode leg configuration
     */
    void authorize(
            List<X509Certificate> signingCertificateTrustChain,
            X509Certificate signingCertificate,
            UserMessage userMessage,
            SecurityProfile securityProfile);


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
