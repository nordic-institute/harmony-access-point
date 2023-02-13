package eu.domibus.core.util;


import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.CertificateException;
import eu.domibus.api.security.SecurityProfile;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.crypto.SecurityProfileAliasConfiguration;
import eu.domibus.core.ebms3.ws.algorithm.DomibusAlgorithmSuiteLoader;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.wss4j.policy.model.AlgorithmSuite;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;


/**
 * Provides services needed by the Security Profiles feature
 *
 * @since 5.1
 */
@Service
public class SecurityProfileService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecurityProfileService.class);

    protected final DomibusAlgorithmSuiteLoader domibusAlgorithmSuiteLoader;

    protected final DomibusPropertyProvider domibusPropertyProvider;

    public static final String CERTIFICATE_ALGORITHM_RSA = "RSA";
    public static final String CERTIFICATE_PURPOSE_DECRYPT = "decrypt";
    public static final String CERTIFICATE_PURPOSE_SIGN = "sign";

    public SecurityProfileService(DomibusAlgorithmSuiteLoader domibusAlgorithmSuiteLoader, DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusAlgorithmSuiteLoader = domibusAlgorithmSuiteLoader;
        this.domibusPropertyProvider = domibusPropertyProvider;
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

    public void validateStoreCertificates(List<SecurityProfileAliasConfiguration> securityProfileAliasConfigurations, KeyStore store) {
        securityProfileAliasConfigurations.forEach(
                profileConfiguration -> validateCertificateType(profileConfiguration, store));
    }

    private void validateCertificateType(SecurityProfileAliasConfiguration profileConfiguration, KeyStore store) {
        try {
            String alias = profileConfiguration.getAlias();
            X509Certificate certificate = (X509Certificate) store.getCertificate(alias);
            if (certificate == null) {
                String exceptionMessage = String.format("Alias: [%s] does not exist in the keystore", alias);
                throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
            }

            String certificateAlgorithm = certificate.getPublicKey().getAlgorithm();
            SecurityProfile securityProfile = profileConfiguration.getSecurityProfile();
            if (securityProfile == null) {
                validateLegacyAliasCertificate(certificateAlgorithm, alias);
                return;
            }
            String certificatePurpose = StringUtils.substringAfterLast(alias,"_").toLowerCase();
            switch (certificatePurpose) {
                case CERTIFICATE_PURPOSE_DECRYPT:
                    validateDecryptionCertificate(securityProfile, certificateAlgorithm, certificatePurpose, alias);
                    break;
                case CERTIFICATE_PURPOSE_SIGN:
                    validateSigningCertificate(securityProfile, certificateAlgorithm, certificatePurpose, alias);
                    break;
                default:
                    String exceptionMessage = String.format("Invalid naming of alias [%s], it should end with sign or decrypt", alias);
                    throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
            }
        } catch (KeyStoreException e) {
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, "Keystore exception: " + e.getMessage());
        }
    }

    private void validateLegacyAliasCertificate(String certificateAlgorithm, String alias) {
        if (!isLegacySingleAliasKeystoreDefined()) {
            String exceptionMessage = String.format("Legacy keystore alias [%s] is not defined", alias);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
        if (!certificateAlgorithm.equalsIgnoreCase(CERTIFICATE_ALGORITHM_RSA)) {
            String exceptionMessage = String.format("Invalid certificate type with alias: [%s] defined", alias);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
    }

    private void validateDecryptionCertificate(SecurityProfile securityProfile, String certificateAlgorithm,
                                               String certificatePurpose, String alias) {
        List<String> certificateTypes = new ArrayList<>();
        if (securityProfile == SecurityProfile.RSA) {
            certificateTypes = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_DECRYPT_TYPE);
        } else if (securityProfile == SecurityProfile.ECC) {
            certificateTypes = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_DECRYPT_TYPE);
        }
        checkCertificateType(certificateTypes, certificateAlgorithm, certificatePurpose, alias, securityProfile);
    }

    private void validateSigningCertificate(SecurityProfile securityProfile, String certificateAlgorithm,
                                            String certificatePurpose, String alias) {
        List<String> certificateTypes = new ArrayList<>();

        if (securityProfile == SecurityProfile.RSA) {
            certificateTypes = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_SIGN_TYPE);
        } else if (securityProfile == SecurityProfile.ECC) {
            certificateTypes = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_SIGN_TYPE);
        }
        checkCertificateType(certificateTypes, certificateAlgorithm, certificatePurpose, alias, securityProfile);
    }

    private void checkCertificateType(List<String> certificateTypes, String certificateAlgorithm, String certificatePurpose,
                                      String alias, SecurityProfile securityProfile) {
        boolean certificateTypeWasFound = certificateTypes.stream().anyMatch(certificateAlgorithm::equalsIgnoreCase);
        if (!certificateTypeWasFound) {
            String exceptionMessage = String.format("Invalid [%s] certificate type with alias: [%s] used in security profile: [%s]", certificatePurpose, alias, securityProfile);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
    }

    public boolean isLegacySingleAliasKeystoreDefined() {
        return domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS) != null;
    }

}
