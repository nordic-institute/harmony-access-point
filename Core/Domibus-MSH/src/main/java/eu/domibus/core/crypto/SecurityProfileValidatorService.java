package eu.domibus.core.crypto;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.CertificateException;
import eu.domibus.api.security.SecurityProfile;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
     * Parses all the store(keystore/truststore) aliases and validates their certificate algorithms against the SecurityProfiles configurations
     * defined in domibus.properties
     *
     * @param securityProfileAliasConfigurations the Security Profile configurations list for all the aliases defined in domibus.properties
     * @param store the domain's store(keystore/truststore)
     */
    public void validateStoreCertificateTypes(List<SecurityProfileAliasConfiguration> securityProfileAliasConfigurations, KeyStore store, StoreType storeType) {
        List<String> aliasesList;
        if (store == null) {
            String exceptionMessage = String.format("[%s] is null", storeType);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
        try {
            aliasesList = Collections.list(store.aliases());
        } catch (KeyStoreException e) {
            String exceptionMessage = String.format("Error while getting the aliases from the [%s]: %s", storeType, e.getMessage());
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }

        int totalNumberOfAliasesInitiallyInStore = aliasesList.size();
        List<String> invalidCertificateAliases = new ArrayList<>();
        aliasesList.forEach(alias -> {
            if (!isCertificateTypeForStoreAliasValid(securityProfileAliasConfigurations, alias, store, storeType)) {
                invalidCertificateAliases.add(alias);
            }
        });
        invalidCertificateAliases.forEach(invalidAlias -> {
            try {
                store.deleteEntry(invalidAlias);
            } catch (KeyStoreException e) {
                LOG.warn("Error while removing invalid alias [{}] from [{}]", invalidAlias, storeType);
            }
        });
        aliasesList.removeAll(invalidCertificateAliases);

        String infoMessage;
        if (aliasesList.size() == totalNumberOfAliasesInitiallyInStore) {
            infoMessage = String.format("All [%s] certificate types are valid", storeType);
        } else if (aliasesList.size() > 0) {
            infoMessage = String.format("Not all [%s] certificate types are valid", storeType);
        } else {
            infoMessage = String.format("There are no valid [%s] certificate types", storeType);
        }

        LOG.info(infoMessage);
    }

    private boolean isCertificateTypeForStoreAliasValid(List<SecurityProfileAliasConfiguration> securityProfileAliasConfigurations,
                                                        String alias, KeyStore store, StoreType storeType) {
        SecurityProfile securityProfileExtractedFromAlias;
        try {
            securityProfileExtractedFromAlias = getSecurityProfileFromAlias(alias);
        } catch (CertificateException e) {
            LOG.error("{}", e.getMessage());
            return false;
        }
        if (securityProfileExtractedFromAlias == null) {
            //Legacy alias
            try {
                X509Certificate certificate = (X509Certificate) store.getCertificate(alias);
                validateLegacyAliasCertificateType(certificate.getPublicKey().getAlgorithm(), alias, storeType);
                return true;
            } catch (KeyStoreException e) {
                LOG.error("Error getting legacy alias [{}] from keystore with type [{}]: exception: {}", alias, storeType, e.getMessage());
                return false;
            }
        }

        SecurityProfileAliasConfiguration securityProfileConfigurationCorrespondingToAlias =
                getSecurityProfileConfigurationForStoreAlias(securityProfileAliasConfigurations, alias, storeType);
        validateCertificateType(alias, securityProfileConfigurationCorrespondingToAlias.getSecurityProfile(), store, storeType);
        return true;
    }

    /**
     * Retrieves the SecurityProfileConfiguration corresponding to the SecurityProfile and certificate purpose extracted
     * from the store(keystore/truststore) alias
     *
     * @param securityProfileAliasConfigurations the entire security profile configurations defined in domibus.properties
     * @param alias the alias for the store(keystore/truststore) certificate
     * @param storeType the store type (keystore/truststore)
     * @return the SecurityProfileAliasConfiguration that matches the SecurityProfile and certificate purpose of the store(keystore/truststore)
     *         certificate defined by the alias
     */
    private SecurityProfileAliasConfiguration getSecurityProfileConfigurationForStoreAlias(List<SecurityProfileAliasConfiguration> securityProfileAliasConfigurations,
                                                                                           String alias, StoreType storeType) {
        CertificatePurpose certificatePurpose = getCertificatePurposeFromAlias(alias);
        if (certificatePurpose == null) {
            String exceptionMessage = String.format("[%s] alias [%s] does not contain a possible certificate purpose name(sign/encrypt)", storeType, alias);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
        if (certificatePurpose == CertificatePurpose.ENCRYPT) {
            //for encrypt we use the same certificate type definition as specified for decrypt
            certificatePurpose = CertificatePurpose.DECRYPT;
        }

        SecurityProfile securityProfileExtractedFromAlias = getSecurityProfileFromAlias(alias);

        CertificatePurpose certificatePurposeToTest = certificatePurpose;
        Optional<SecurityProfileAliasConfiguration>  securityProfileConfigurationForAlias = securityProfileAliasConfigurations.stream()
                .filter(profile -> profile.getSecurityProfile() == securityProfileExtractedFromAlias
                        && getCertificatePurposeFromAlias(profile.getAlias()) == certificatePurposeToTest)
                .findFirst();

        if (!securityProfileConfigurationForAlias.isPresent()) {
            String exceptionMessage = String.format("[%s] alias [%s] does not correspond to any security profile configuration", storeType, alias);
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
            String exceptionMessage = String.format("Error getting alias [%s] from keystore with type [%s]: exception: %s", alias, storeType, e.getMessage());
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
            case ENCRYPT:
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

    public boolean isLegacySingleAliasKeystoreDefined(Domain domain) {
        return domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS) != null;
    }
}
