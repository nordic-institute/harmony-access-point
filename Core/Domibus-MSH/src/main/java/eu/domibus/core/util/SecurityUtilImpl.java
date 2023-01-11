package eu.domibus.core.util;

import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.common.model.configuration.SecurityProfile;
import eu.domibus.core.ebms3.ws.algorithm.DomibusAlgorithmSuiteLoader;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Enumeration;

import static eu.domibus.common.model.configuration.SecurityProfile.ECC;
import static eu.domibus.common.model.configuration.SecurityProfile.RSA;

/**
 * Provides functionality for security certificates configuration
 *
 * @author Lucian FURCA
 * @since 5.1
 */
@Service
public class SecurityUtilImpl {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecurityUtilImpl.class);

    private final DomibusAlgorithmSuiteLoader domibusAlgorithmSuiteLoader;

    public SecurityUtilImpl(DomibusAlgorithmSuiteLoader domibusAlgorithmSuiteLoader) {
        this.domibusAlgorithmSuiteLoader = domibusAlgorithmSuiteLoader;
    }

    /**
     * Retrieves the Asymmetric Signature Algorithm corresponding to the security profile, defaulting to RSA_SHA256
     * correspondent if no security profile is defined
     *
     * @param securityProfile the configured security profile
     * @return the Asymmetric Signature Algorithm
     */
    public String getSecurityAlgorithm(SecurityProfile securityProfile) {
        if (securityProfile == null) {
            LOG.info("No security profile was specified so the default RSA_SHA256 algorithm is used.");
            return domibusAlgorithmSuiteLoader.getAsymmetricSignature(RSA);
        }

        switch (securityProfile) {
            case ECC:
                return domibusAlgorithmSuiteLoader.getAsymmetricSignature(ECC);
            case RSA:
            default: {
                return domibusAlgorithmSuiteLoader.getAsymmetricSignature(RSA);
            }
        }
    }

    public boolean areKeystoresIdentical(KeyStore store1, KeyStore store2) {
        if (store1 == null && store2 == null) {
            LOG.debug("Identical keystores: both are null");
            return true;
        }
        if (store1 == null || store2 == null) {
            LOG.debug("Different keystores: [{}] vs [{}]", store1, store2);
            return false;
        }
        try {
            if (store1.size() != store2.size()) {
                LOG.debug("Different keystores: [{}] vs [{}] entries", store1.size(), store2.size());
                return false;
            }
            final Enumeration<String> aliases = store1.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                if (!store2.containsAlias(alias)) {
                    LOG.debug("Different keystores: [{}] alias is not found in both", alias);
                    return false;
                }
                if (!store1.getCertificate(alias).equals(store2.getCertificate(alias))) {
                    LOG.debug("Different keystores: [{}] certificate is different", alias);
                    return false;
                }
            }
            return true;
        } catch (KeyStoreException e) {
            throw new DomibusCertificateException("Invalid keystore", e);
        }
    }
}
