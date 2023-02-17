package eu.domibus.core.crypto;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.CertificateException;
import eu.domibus.api.security.SecurityProfile;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.*;

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

    protected final SecurityProfileService securityProfileService;

    public SecurityProfileValidatorService(DomibusPropertyProvider domibusPropertyProvider, SecurityProfileService securityProfileService) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.securityProfileService = securityProfileService;
    }

    /**
     * Parses all the certificate alias configurations based on the domibus.properties security profile fields and validates that the certificate types
     * defined in the KeyStore match these configurations
     *
     * @param securityProfileAliasConfigurations the Security Profile configurations list for all the aliases defined in domibus.properties
     * @param keyStore the domain's KeyStore
     */
    public void validateKeyStoreCertificateTypes(List<SecurityProfileAliasConfiguration> securityProfileAliasConfigurations, KeyStore keyStore) {
        securityProfileAliasConfigurations.forEach(
                profileConfiguration -> validateCertificateType(profileConfiguration.getAlias(), profileConfiguration.getSecurityProfile(), keyStore, StoreType.KEYSTORE));

        LOG.info("KeyStore certificate types are valid");
    }

    /**
     * Parses all the TrustStore aliases and validates their certificate algorithms against the SecurityProfiles
     *
     * @param securityProfileAliasConfigurations the Security Profile configurations list for all the aliases defined in domibus.properties
     * @param trustStore the domain's TrustStore
     */
    public void validateTrustStoreCertificateTypes(List<SecurityProfileAliasConfiguration> securityProfileAliasConfigurations, KeyStore trustStore) {
        List<String> aliasesList;
        try {
            aliasesList = Collections.list(trustStore.aliases());
        } catch (KeyStoreException e) {
            String exceptionMessage = String.format("[%s] exception: %s", StoreType.TRUSTSTORE, e.getMessage());
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
        aliasesList.forEach(alias -> validateCertificateTypeForTrustStoreAlias(securityProfileAliasConfigurations, alias, trustStore));

        LOG.info("TrustStore certificate types are valid");
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

        Optional<SecurityProfileAliasConfiguration>  securityProfileConfigurationForAlias = securityProfileAliasConfigurations.stream()
                .filter(profile -> profile.getSecurityProfile() == securityProfileExtractedFromAlias
                        && getCertificatePurposeFromAlias(profile.getAlias()) == certificatePurpose)
                .findFirst();

        if (!securityProfileConfigurationForAlias.isPresent()) {
            String exceptionMessage = String.format("[%s] alias [%s] does not correspond to any security profile configuration", StoreType.TRUSTSTORE, alias);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }

        return securityProfileConfigurationForAlias.get();
    }

    /**
     * Extracts the CertificatePurpose from the given alias
     *
     * @param alias the alias from the store
     * @throws CertificateException if the certificate purpose can't be extracted from the alias
     * @return the CertificatePurpose name extracted from the alias definition
     */
    private CertificatePurpose getCertificatePurposeFromAlias(String alias) {
        if (isLegacySingleAliasKeystoreDefined()) {
            return null;
        }
        CertificatePurpose certificatePurpose = securityProfileService.extractCertificatePurpose(alias);
        if (certificatePurpose == null) {
            String exceptionMessage = String.format("[%s] alias [%s] does not contain a possible certificate purpose name(sign/decrypt)", StoreType.TRUSTSTORE, alias);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
        return certificatePurpose;
    }

    /**
     * Extracts the SecurityProfile from the given alias
     *
     * @param alias the alias from the store
     * @throws CertificateException if the security profile can't be extracted from the alias
     * @return the SecurityProfile name extracted from the alias definition
     */
    private SecurityProfile getSecurityProfileFromAlias(String alias) {
        if (isLegacySingleAliasKeystoreDefined()) {
            return null;
        }
        SecurityProfile securityProfile = securityProfileService.extractSecurityProfile(alias);
        if (securityProfile == null) {
            String exceptionMessage = String.format("[%s] alias [%s] does not contain a possible profile name(rsa/ecc)", StoreType.TRUSTSTORE, alias);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }

        return securityProfile;
    }

    private void validateCertificateType(String alias, SecurityProfile securityProfile, KeyStore store, StoreType storeType) {
        X509Certificate certificate;
        try {
            certificate = (X509Certificate) store.getCertificate(alias);
        } catch (KeyStoreException e) {
            String exceptionMessage = String.format("[%s] exception: %s", storeType, e.getMessage());
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
        if (certificate == null) {
            String exceptionMessage = String.format("Alias [%s] does not exist in the [%s]", alias, storeType);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }

        String certificateAlgorithm = certificate.getPublicKey().getAlgorithm();
        CertificatePurpose certificatePurpose = getCertificatePurposeFromAlias(alias);
        if (securityProfile == null || certificatePurpose == null) {
            validateLegacyAliasCertificateType(certificateAlgorithm, alias, storeType);
            return;
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
        List<String> acceptedCertificateAlgorithms = new ArrayList<>();
        if (securityProfile == SecurityProfile.RSA) {
            acceptedCertificateAlgorithms = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_DECRYPT_TYPE);
        } else if (securityProfile == SecurityProfile.ECC) {
            acceptedCertificateAlgorithms = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_DECRYPT_TYPE);
        }
        checkCertificateType(acceptedCertificateAlgorithms, certificateAlgorithm, certificatePurpose, alias, securityProfile, storeType);
    }

    private void validateSigningCertificateType(SecurityProfile securityProfile, String certificateAlgorithm,
                                                CertificatePurpose certificatePurpose, String alias, StoreType storeType) {
        List<String> acceptedCertificateAlgorithms = new ArrayList<>();

        if (securityProfile == SecurityProfile.RSA) {
            acceptedCertificateAlgorithms = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_SIGN_TYPE);
        } else if (securityProfile == SecurityProfile.ECC) {
            acceptedCertificateAlgorithms = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_SIGN_TYPE);
        }
        checkCertificateType(acceptedCertificateAlgorithms, certificateAlgorithm, certificatePurpose, alias, securityProfile, storeType);
    }

    private void checkCertificateType(List<String> acceptedCertificateAlgorithms, String certificateAlgorithm, CertificatePurpose certificatePurpose,
                                      String alias, SecurityProfile securityProfile, StoreType storeType) {
        boolean certificateTypeIsAccepted = acceptedCertificateAlgorithms.stream().anyMatch(certificateAlgorithm::equalsIgnoreCase);
        if (!certificateTypeIsAccepted) {
            String exceptionMessage = String.format("Invalid [%s] certificate type in [%s] with alias: [%s] used in security profile: [%s]",
                    certificatePurpose, storeType, alias, securityProfile);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
    }

    public boolean isLegacySingleAliasKeystoreDefined() {
        return domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS) != null;
    }
}
