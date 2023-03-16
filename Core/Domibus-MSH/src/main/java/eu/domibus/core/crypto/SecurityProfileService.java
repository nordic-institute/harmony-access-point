package eu.domibus.core.crypto;


import eu.domibus.api.security.SecurityProfile;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.ws.algorithm.DomibusAlgorithmSuiteLoader;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.neethi.Policy;
import org.apache.wss4j.policy.model.AlgorithmSuite;
import org.springframework.stereotype.Service;

/**
 * Provides services needed by the Security Profiles feature
 * @author Lucian FURCA
 * @since 5.1
 */
@Service
public class SecurityProfileService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecurityProfileService.class);

    protected final DomibusAlgorithmSuiteLoader domibusAlgorithmSuiteLoader;

    protected final PolicyService policyService;

    public SecurityProfileService(DomibusAlgorithmSuiteLoader domibusAlgorithmSuiteLoader, PolicyService policyService) {
        this.domibusAlgorithmSuiteLoader = domibusAlgorithmSuiteLoader;
        this.policyService = policyService;
    }

    public boolean isSecurityPolicySet(LegConfiguration legConfiguration) {
        Policy policy;
        try {
            policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy(), legConfiguration.getSecurity().getProfile());
        } catch (final ConfigurationException e) {
            String message = String.format("Error retrieving policy for leg [%s]", legConfiguration.getName());
            throw new ConfigurationException(message);
        }

        return !policyService.isNoSecurityPolicy(policy);
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
        if (!isSecurityPolicySet(legConfiguration)) {
            return null;
        }

        SecurityProfile securityProfile = legConfiguration.getSecurity().getProfile();
        if (securityProfile == null) {
            LOG.info("The leg configuration contains no security profile info so the default RSA_SHA256 algorithm is used.");
            securityProfile = SecurityProfile.RSA;
        }
        final AlgorithmSuite.AlgorithmSuiteType algorithmSuiteType = domibusAlgorithmSuiteLoader.getAlgorithmSuiteType(securityProfile);
        return algorithmSuiteType.getAsymmetricSignature();
    }

    public String getAliasForSigning(LegConfiguration legConfiguration, String senderName) {
        return getAliasForSigning(legConfiguration.getSecurity().getProfile(), senderName);
    }

    public String getAliasForSigning(SecurityProfile securityProfile, String senderName) {
        String alias = senderName;
        if (securityProfile != null) {
            alias = senderName + "_" + StringUtils.lowerCase(securityProfile.getProfile()) + "_sign";
        }
        LOG.info("The following alias was determined for signing: [{}]", alias);
        return alias;
    }

    public String getAliasForEncrypting(LegConfiguration legConfiguration, String receiverName) {
        String alias = receiverName;
        SecurityProfile securityProfile = legConfiguration.getSecurity().getProfile();
        if (securityProfile != null) {
            alias = receiverName + "_" + StringUtils.lowerCase(securityProfile.getProfile()) + "_encrypt";

        }
        LOG.info("The following alias was determined for encrypting: [{}]", alias);
        return alias;
    }

    public CertificatePurpose extractCertificatePurpose(String alias) {
        return CertificatePurpose.lookupByName(StringUtils.substringAfterLast(alias, "_").toUpperCase());
    }

    public SecurityProfile extractSecurityProfile(String alias) {
        return SecurityProfile.lookupByName(StringUtils.substringAfterLast(StringUtils.substringBeforeLast(alias,"_"), "_").toUpperCase());
    }
}
