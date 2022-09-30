package eu.domibus.core.util;

import eu.domibus.common.model.configuration.AsymmetricSignatureAlgorithm;
import eu.domibus.common.model.configuration.SecurityProfile;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Provides functionality for security certificates configuration
 *
 * @author Lucian FURCA
 * @since 5.1
 */
@Component
public class SecurityUtilImpl {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecurityUtilImpl.class);

    public String getSecurityAlgorithm(SecurityProfile profile) {

        if (profile == null) {
            LOG.info("No security profile was specified so the default RSA_SHA256 algorithm is used.");
            return AsymmetricSignatureAlgorithm.RSA_SHA256.getAlgorithm();
        }

        switch (profile) {
            case RSA:
                return AsymmetricSignatureAlgorithm.RSA_SHA256.getAlgorithm();
            case ECC:
                return AsymmetricSignatureAlgorithm.ECC_SHA256.getAlgorithm();
            default:
                LOG.error("Profile [{}] is not a valid profile. No security profile was set.", profile);
        }

        return null;
    }
}
