package eu.domibus.core.util;


import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.SecurityProfile;
import eu.domibus.core.ebms3.ws.algorithm.DomibusAlgorithmSuiteLoader;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.wss4j.policy.model.AlgorithmSuite;
import org.springframework.stereotype.Service;

import static eu.domibus.common.model.configuration.SecurityProfile.RSA;

/**
 * Provides services needed by the Security Profiles feature
 *
 * @since 5.1
 */
@Service
public class SecurityProfileService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecurityProfileService.class);

    private final DomibusAlgorithmSuiteLoader domibusAlgorithmSuiteLoader;

    public SecurityProfileService(DomibusAlgorithmSuiteLoader domibusAlgorithmSuiteLoader) {
        this.domibusAlgorithmSuiteLoader = domibusAlgorithmSuiteLoader;
    }

    /**
     * Retrieves the Asymmetric Signature Algorithm corresponding to the security profile, defaulting to RSA_SHA256
     * correspondent if no security profile is defined
     *
     * @param legConfiguration the leg configuration containing the security profile
     * @throws ConfigurationException thrown when the legConfiguration contains an invalid security profile
     * @return the Asymmetric Signature Algorithm
     */
    public String getSecurityAlgorithm(LegConfiguration legConfiguration) throws ConfigurationException {
        SecurityProfile securityProfile = legConfiguration.getSecurity().getProfile();

        if (securityProfile == null) {
            LOG.info("The leg configuration contains no security profile info so the default RSA_SHA256 algorithm is used.");
            securityProfile = RSA;
        }
        final AlgorithmSuite.AlgorithmSuiteType algorithmSuiteType = domibusAlgorithmSuiteLoader.getAlgorithmSuiteType(securityProfile);
        return algorithmSuiteType.getAsymmetricSignature();
    }
}
