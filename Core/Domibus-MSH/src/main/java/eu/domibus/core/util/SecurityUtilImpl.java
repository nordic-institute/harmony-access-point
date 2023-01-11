package eu.domibus.core.util;

import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.common.model.configuration.SecurityProfile;
import eu.domibus.core.ebms3.ws.algorithm.DomibusAlgorithmSuiteLoader;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.wss4j.policy.model.AlgorithmSuite;
import org.springframework.stereotype.Component;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Enumeration;
import java.util.Map;

import static eu.domibus.common.model.configuration.SecurityProfile.RSA;
import static eu.domibus.core.ebms3.ws.algorithm.DomibusAlgorithmSuiteLoader.BASIC_128_GCM_SHA_256_MGF_SHA_256_ECC;
import static eu.domibus.core.ebms3.ws.algorithm.DomibusAlgorithmSuiteLoader.BASIC_128_GCM_SHA_256_MGF_SHA_256_RSA;

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
        Map<String, AlgorithmSuite.AlgorithmSuiteType> algorithmSuiteTypes = DomibusAlgorithmSuiteLoader.DomibusAlgorithmSuite.getAlgorithmSuiteTypes();

        if (profile == null) {
            LOG.info("No security profile was specified so the default RSA_SHA256 algorithm is used.");
            return algorithmSuiteTypes.get(BASIC_128_GCM_SHA_256_MGF_SHA_256_RSA).getAsymmetricSignature();
        }

        switch (profile) {
            case ECC:
                return algorithmSuiteTypes.get(BASIC_128_GCM_SHA_256_MGF_SHA_256_ECC).getAsymmetricSignature();
            case RSA:
            default: {
                if (profile != RSA) {
                    LOG.info("Unsupported security profile specified: [{}] defaulting to RSA_SHA256 algorithm.", profile);
                }
                return algorithmSuiteTypes.get(BASIC_128_GCM_SHA_256_MGF_SHA_256_RSA).getAsymmetricSignature();
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
