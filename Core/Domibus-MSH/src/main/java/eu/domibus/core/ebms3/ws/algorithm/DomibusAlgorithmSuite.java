package eu.domibus.core.ebms3.ws.algorithm;

import eu.domibus.common.model.configuration.AsymmetricSignatureAlgorithm;
import eu.domibus.common.model.configuration.SecurityProfile;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.apache.wss4j.common.WSS4JConstants;
import org.apache.wss4j.policy.SPConstants;
import org.apache.wss4j.policy.model.AbstractSecurityAssertion;
import org.apache.wss4j.policy.model.AlgorithmSuite;

import java.util.HashMap;
import java.util.Map;

import static eu.domibus.common.model.configuration.SecurityProfile.ECC;
import static eu.domibus.common.model.configuration.SecurityProfile.RSA;
import static org.apache.wss4j.common.WSS4JConstants.MGF_SHA256;

public class DomibusAlgorithmSuite extends AlgorithmSuite {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusAlgorithmSuite.class);

    public static final String AES128_GCM_ALGORITHM = "http://www.w3.org/2009/xmlenc11#aes128-gcm";

    public static final String BASIC_128_GCM_SHA_256_RSA = "Basic128GCMSha256";

    public static final String BASIC_128_GCM_SHA_256_MGF_SHA_256_RSA = "Basic128GCMSha256MgfSha256";

    //TODO: check below value for correctness when the ECC library is chosen
    public static final String BASIC_128_GCM_SHA_256_MGF_SHA_256_ECC = "Basic128GCMSha256MgfSha256ECC";

    protected Map<String, AlgorithmSuiteType> algorithmSuiteTypesCopy;

    static {
        ALGORITHM_SUITE_TYPES.put(
                RSA.getProfile(),
                new AlgorithmSuiteType(
                        BASIC_128_GCM_SHA_256_MGF_SHA_256_RSA,
                        SPConstants.SHA256,
                        AES128_GCM_ALGORITHM,
                        SPConstants.KW_AES128,
                        WSS4JConstants.KEYTRANSPORT_RSAOAEP_XENC11,
                        SPConstants.P_SHA1_L128,
                        SPConstants.P_SHA1_L128,
                        null,
                        AsymmetricSignatureAlgorithm.RSA_SHA256.getAlgorithm(),
                        128, 128, 128, 256, 1024, 4096
                )
        );
        ALGORITHM_SUITE_TYPES.get(RSA.getProfile()).setMGFAlgo(MGF_SHA256);
        ALGORITHM_SUITE_TYPES.get(RSA.getProfile()).setEncryptionDigest(SPConstants.SHA256);


        ALGORITHM_SUITE_TYPES.put(
                ECC.getProfile(),
                new AlgorithmSuiteType(
                        BASIC_128_GCM_SHA_256_MGF_SHA_256_ECC,
                        SPConstants.SHA256,
                        AES128_GCM_ALGORITHM,
                        SPConstants.KW_AES128,
                        BASIC_128_GCM_SHA_256_MGF_SHA_256_ECC, //TODO: replace with WSS4JConstants.ECC_???,
                        SPConstants.P_SHA1_L128,
                        SPConstants.P_SHA1_L128,
                        null,
                        AsymmetricSignatureAlgorithm.ECC_SHA256.getAlgorithm(),
                        128, 128, 128, 256, 1024, 4096
                )
        );
        ALGORITHM_SUITE_TYPES.get(ECC.getProfile()).setMGFAlgo(MGF_SHA256);
        ALGORITHM_SUITE_TYPES.get(ECC.getProfile()).setEncryptionDigest(SPConstants.SHA256);
    }

    DomibusAlgorithmSuite(final SPConstants.SPVersion version, final Policy nestedPolicy) {
        super(version, nestedPolicy);
    }

    @Override
    protected AbstractSecurityAssertion cloneAssertion(final Policy nestedPolicy) {
        return new DomibusAlgorithmSuite(this.getVersion(), nestedPolicy);
    }

    @Override
    protected void parseCustomAssertion(final Assertion assertion) {
        final String assertionName = assertion.getName().getLocalPart();
        final String assertionNamespace = assertion.getName().getNamespaceURI();
        if (!DomibusAlgorithmSuiteLoader.E_DELIVERY_ALGORITHM_NAMESPACE.equals(assertionNamespace)) {
            return;
        }

        if (BASIC_128_GCM_SHA_256_MGF_SHA_256_RSA.equals(assertionName)) {
            setAlgorithmSuiteType(ALGORITHM_SUITE_TYPES.get(RSA.getProfile()));
            getAlgorithmSuiteType().setNamespace(assertionNamespace);
        } else if (BASIC_128_GCM_SHA_256_MGF_SHA_256_ECC.equals(assertionName)) {
            setAlgorithmSuiteType(ALGORITHM_SUITE_TYPES.get(ECC.getProfile()));
            getAlgorithmSuiteType().setNamespace(assertionNamespace);
        }
    }

    /**
     * Retrieves the Algorithm Suite Type corresponding to the security profile, defaulting to the RSA correspondent
     * if no security profile is defined
     *
     * @param securityProfile the configured security profile
     * @throws ConfigurationException exception thrown when an invalid security profile is set
     * @return the Algorithm Suite Type for a security profile
     */
    public AlgorithmSuiteType getAlgorithmSuiteType(SecurityProfile securityProfile) throws ConfigurationException {
        if (algorithmSuiteTypesCopy == null) {
            algorithmSuiteTypesCopy = new HashMap<>(ALGORITHM_SUITE_TYPES);
        }

        if (securityProfile == null) {
            LOG.info("No security profile was specified so the default RSA_SHA256 algorithm is used.");
            return algorithmSuiteTypesCopy.get(RSA.getProfile());
        }

        switch (securityProfile) {
            case ECC:
                return algorithmSuiteTypesCopy.get(ECC.getProfile());
            case RSA:
                return algorithmSuiteTypesCopy.get(RSA.getProfile());
            default: {
                String errorMessage = "Unsupported security profile specified: [" + securityProfile + "]";
                LOG.error(errorMessage);
                throw new ConfigurationException(errorMessage);
            }
        }
    }
}
