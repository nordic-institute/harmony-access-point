package eu.domibus.core.util;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.CertificateException;
import eu.domibus.api.security.SecurityProfile;
import eu.domibus.core.crypto.SecurityProfileAliasConfiguration;
import eu.domibus.core.crypto.StoreType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
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

    public static final String CERTIFICATE_ALGORITHM_RSA = "RSA";
    public static final String CERTIFICATE_PURPOSE_DECRYPT = "decrypt";
    public static final String CERTIFICATE_PURPOSE_SIGN = "sign";

    protected final DomibusPropertyProvider domibusPropertyProvider;

    public SecurityProfileValidatorService(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    public void validateStoreCertificateTypes(List<SecurityProfileAliasConfiguration> securityProfileAliasConfigurations, KeyStore store, StoreType storeType) {
        securityProfileAliasConfigurations.forEach(
                profileConfiguration -> validateCertificateType(profileConfiguration, store, storeType));
    }

    private void validateCertificateType(SecurityProfileAliasConfiguration profileConfiguration, KeyStore store, StoreType storeType) {
        try {
            String alias = profileConfiguration.getAlias();
            X509Certificate certificate = (X509Certificate) store.getCertificate(alias);
            if (certificate == null) {
                String exceptionMessage = String.format("Alias [%s] does not exist in the [%s]", alias, storeType);
                throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
            }

            String certificateAlgorithm = certificate.getPublicKey().getAlgorithm();
            SecurityProfile securityProfile = profileConfiguration.getSecurityProfile();
            if (securityProfile == null) {
                validateLegacyAliasCertificateType(certificateAlgorithm, alias);
                return;
            }
            String certificatePurpose = StringUtils.substringAfterLast(alias,"_").toLowerCase();
            switch (certificatePurpose) {
                case CERTIFICATE_PURPOSE_DECRYPT:
                    validateDecryptionCertificateType(securityProfile, certificateAlgorithm, certificatePurpose, alias);
                    break;
                case CERTIFICATE_PURPOSE_SIGN:
                    validateSigningCertificateType(securityProfile, certificateAlgorithm, certificatePurpose, alias);
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

    private void validateLegacyAliasCertificateType(String certificateAlgorithm, String alias) {
        if (!isLegacySingleAliasKeystoreDefined()) {
            String exceptionMessage = String.format("Legacy keystore alias [%s] is not defined", alias);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
        if (!certificateAlgorithm.equalsIgnoreCase(CERTIFICATE_ALGORITHM_RSA)) {
            String exceptionMessage = String.format("Invalid certificate type with alias: [%s] defined", alias);
            throw new CertificateException(DomibusCoreErrorCode.DOM_005, exceptionMessage);
        }
    }

    private void validateDecryptionCertificateType(SecurityProfile securityProfile, String certificateAlgorithm,
                                                   String certificatePurpose, String alias) {
        List<String> certificateTypes = new ArrayList<>();
        if (securityProfile == SecurityProfile.RSA) {
            certificateTypes = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_DECRYPT_TYPE);
        } else if (securityProfile == SecurityProfile.ECC) {
            certificateTypes = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_DECRYPT_TYPE);
        }
        checkCertificateType(certificateTypes, certificateAlgorithm, certificatePurpose, alias, securityProfile);
    }

    private void validateSigningCertificateType(SecurityProfile securityProfile, String certificateAlgorithm,
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
