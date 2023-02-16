package eu.domibus.core.crypto;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.CertificateException;
import eu.domibus.api.security.SecurityProfile;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Validates keystore and truststore certificates
 *
 * @author Lucian FURCA
 * @since 5.1
 */
@Service
public class SecurityProfileValidatorService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecurityProfileValidatorService.class);

    public static final String CERTIFICATE_ALGORITHM_RSA = "RSA";

    protected final DomibusPropertyProvider domibusPropertyProvider;

    public SecurityProfileValidatorService(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    /**
     * Parses all the certificate alias configurations from domibus.properties and validates that the certificate types
     * defined in the Keystore match these configurations
     *
     * @param securityProfileAliasConfigurations the Security Profile configurations list for all the aliases defined in domibus.properties
     * @param keyStore the domain's KeyStore
     */
    public void validateKeyStoreCertificateTypes(List<SecurityProfileAliasConfiguration> securityProfileAliasConfigurations, KeyStore keyStore) {
        securityProfileAliasConfigurations.forEach(
                profileConfiguration -> validateCertificateType(profileConfiguration.getAlias(), profileConfiguration.getSecurityProfile(), keyStore, StoreType.KEYSTORE));

        LOG.info(" ******* KeyStore certificate types are valid *******");
    }

    /**
     * Parses all the TrustStore aliases and validates their certificate algorithms against the SecurityProfiles
     *
     * @param securityProfileAliasConfigurations the Security Profile configurations list for all the aliases defined in domibus.properties
     * @param trustStore the domain's TrustStore
     */
    public void validateTrustStoreCertificateTypes(List<SecurityProfileAliasConfiguration> securityProfileAliasConfigurations, KeyStore trustStore) {
        try {
            Enumeration<String> aliasesEnumeration = trustStore.aliases();
            do {
                validateCertificateTypeForTrustStoreAlias(securityProfileAliasConfigurations, aliasesEnumeration.nextElement(), trustStore);
            } while(aliasesEnumeration.hasMoreElements());

        } catch (KeyStoreException e) {
            String exceptionMessage = String.format("[%s] exception: %s", StoreType.TRUSTSTORE, e.getMessage());
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }

        LOG.info(" ******* TrustStore certificate types are valid *******");
    }

    private void validateCertificateTypeForTrustStoreAlias(List<SecurityProfileAliasConfiguration> securityProfileAliasConfigurations,
                                                           String alias, KeyStore trustStore) {
        SecurityProfile securityProfileExtractedFromAlias = getSecurityProfileFromAlias(alias);
        if (securityProfileExtractedFromAlias == null) {
            //Legacy alias
            try {
                X509Certificate certificate = (X509Certificate) trustStore.getCertificate(alias);
                validateLegacyAliasCertificateType(certificate.getPublicKey().getAlgorithm(), alias, StoreType.TRUSTSTORE);
                return;
            } catch (KeyStoreException e) {
                String exceptionMessage = String.format("[%s] exception: %s", StoreType.TRUSTSTORE, e.getMessage());
                throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
            }
        }

        SecurityProfileAliasConfiguration securityProfileConfigurationCorrespondingToAlias =
                getSecurityProfileConfigurationForTrustStoreAlias(securityProfileAliasConfigurations, alias);
        validateCertificateType(alias, securityProfileConfigurationCorrespondingToAlias.getSecurityProfile(), trustStore, StoreType.TRUSTSTORE);
    }

    /**
     * Retrieves the SecurityProfileConfiguration corresponding to the SecurityProfile and certificate purpose extracted
     * from the TrustStore alias
     *
     * @param securityProfileAliasConfigurations the entire security profile configurations defined in domibus.properties
     * @param alias the alias for the TrustStore certificate
     * @return the SecurityProfileAliasConfiguration that matches the SecurityProfile and certificate purpose of the TrustStore
     *         certificate defined by the alias
     */
    private SecurityProfileAliasConfiguration getSecurityProfileConfigurationForTrustStoreAlias(List<SecurityProfileAliasConfiguration> securityProfileAliasConfigurations,
                                                                                                String alias) {
        CertificatePurpose certificatePurpose = getCertificatePurposeFromAlias(alias);
        if (certificatePurpose == null) {
            String exceptionMessage = String.format("[%s] alias [%s] does not contain a possible certificate purpose name(sign/encrypt)", StoreType.TRUSTSTORE, alias);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }

        SecurityProfile securityProfileExtractedFromAlias = getSecurityProfileFromAlias(alias);

        SecurityProfileAliasConfiguration  securityProfileConfigurationForAlias = securityProfileAliasConfigurations.stream()
                .filter(profile -> profile.getSecurityProfile().equals(securityProfileExtractedFromAlias)
                        && certificatePurpose.equals(getCertificatePurposeFromAlias(profile.getAlias())))
                .findFirst().orElse(null);

        if (securityProfileConfigurationForAlias == null) {
            String exceptionMessage = String.format("[%s] alias [%s] does not correspond to any security profile configuration", StoreType.TRUSTSTORE, alias);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }

        return securityProfileConfigurationForAlias;
    }

    /**
     * Extracts the CertificatePurpose from the given alias
     *
     * @param alias the alias from the store
     * @return the CertificatePurpose name extracted from the alias definition
     */
    private CertificatePurpose getCertificatePurposeFromAlias(String alias) {
        CertificatePurpose certificatePurpose;
        if (isLegacySingleAliasKeystoreDefined()) {
            return null;
        }
        try {
            certificatePurpose = CertificatePurpose.valueOf(StringUtils.substringAfterLast(alias, "_").toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            String exceptionMessage = String.format("[%s] alias [%s] does not contain a possible certificate purpose name(sign/encrypt)", StoreType.TRUSTSTORE, alias);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
        return certificatePurpose;
    }

    /**
     * Extracts the SecurityProfile from the given alias
     *
     * @param alias the alias from the store
     * @return the SecurityProfile name extracted from the alias definition
     */
    private SecurityProfile getSecurityProfileFromAlias(String alias) {
        SecurityProfile securityProfile;
        if (isLegacySingleAliasKeystoreDefined()) {
            return null;
        }

        String profileString = StringUtils.substringAfterLast(StringUtils.substringBeforeLast(alias,"_"), "_").toUpperCase();
        try {
            securityProfile = SecurityProfile.valueOf(profileString);
        } catch (IllegalArgumentException | NullPointerException e) {
            String exceptionMessage = String.format("[%s] alias [%s] does not contain a possible profile name(rsa/ecc)", StoreType.TRUSTSTORE, alias);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
        return securityProfile;
    }

    private void validateCertificateType(String alias, SecurityProfile securityProfile, KeyStore store, StoreType storeType) {
        try {
            X509Certificate certificate = (X509Certificate) store.getCertificate(alias);
            if (certificate == null) {
                String exceptionMessage = String.format("Alias [%s] does not exist in the [%s]", alias, storeType);
                throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
            }

            String certificateAlgorithm = certificate.getPublicKey().getAlgorithm();
            if (securityProfile == null) {
                validateLegacyAliasCertificateType(certificateAlgorithm, alias, storeType);
                return;
            }
            CertificatePurpose certificatePurpose;
            try {
                certificatePurpose = CertificatePurpose.valueOf(StringUtils.substringAfterLast(alias,"_").toUpperCase());
            } catch (IllegalArgumentException | NullPointerException e) {
                String exceptionMessage = String.format("[%s] alias [%s] does not contain a valid certificate purpose name()", storeType, alias);
                throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
            }
            switch (certificatePurpose) {
                case DECRYPT:
                    validateDecryptionCertificateType(securityProfile, certificateAlgorithm, certificatePurpose, alias, storeType);
                    break;
                case SIGN:
                    validateSigningCertificateType(securityProfile, certificateAlgorithm, certificatePurpose, alias, storeType);
                    break;
                default:
                    String exceptionMessage = String.format("Invalid naming of alias [%s], it should end with sign or decrypt", alias);
                    throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
            }
        } catch (KeyStoreException e) {
            String exceptionMessage = String.format("[%s] exception: %s", storeType, e.getMessage());
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
    }

    private void validateLegacyAliasCertificateType(String certificateAlgorithm, String alias, StoreType storeType) {
        if (!isLegacySingleAliasKeystoreDefined()) {
            String exceptionMessage = String.format("Legacy keystore alias [%s] is not defined in [%s]", alias, storeType);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
        if (!certificateAlgorithm.equalsIgnoreCase(CERTIFICATE_ALGORITHM_RSA)) {
            String exceptionMessage = String.format("Invalid certificate type with alias: [%s] defined in [%s]", alias, storeType);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
    }

    private void validateDecryptionCertificateType(SecurityProfile securityProfile, String certificateAlgorithm,
                                                   CertificatePurpose certificatePurpose, String alias, StoreType storeType) {
        List<String> certificateTypes = new ArrayList<>();
        if (securityProfile == SecurityProfile.RSA) {
            certificateTypes = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_DECRYPT_TYPE);
        } else if (securityProfile == SecurityProfile.ECC) {
            certificateTypes = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_DECRYPT_TYPE);
        }
        checkCertificateType(certificateTypes, certificateAlgorithm, certificatePurpose, alias, securityProfile, storeType);
    }

    private void validateSigningCertificateType(SecurityProfile securityProfile, String certificateAlgorithm,
                                                CertificatePurpose certificatePurpose, String alias, StoreType storeType) {
        List<String> certificateTypes = new ArrayList<>();

        if (securityProfile == SecurityProfile.RSA) {
            certificateTypes = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_SIGN_TYPE);
        } else if (securityProfile == SecurityProfile.ECC) {
            certificateTypes = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_SIGN_TYPE);
        }
        checkCertificateType(certificateTypes, certificateAlgorithm, certificatePurpose, alias, securityProfile, storeType);
    }

    private void checkCertificateType(List<String> certificateTypes, String certificateAlgorithm, CertificatePurpose certificatePurpose,
                                      String alias, SecurityProfile securityProfile, StoreType storeType) {
        boolean certificateTypeWasFound = certificateTypes.stream().anyMatch(certificateAlgorithm::equalsIgnoreCase);
        if (!certificateTypeWasFound) {
            String exceptionMessage = String.format("Invalid [%s] certificate type in [%s] with alias: [%s] used in security profile: [%s]",
                    certificatePurpose, storeType, alias, securityProfile);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
    }

    public boolean isLegacySingleAliasKeystoreDefined() {
        return domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS) != null;
    }
}
