
package eu.domibus.core.ebms3.ws.policy;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.SecurityProfile;
import eu.domibus.common.DomibusCacheConstants;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.cxf.DomibusBus;
import eu.domibus.core.ebms3.ws.algorithm.DomibusAlgorithmSuite;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Arun Raj
 * @since 3.3
 *
 * JIRA: EDELIVERY-6671 showed the {@link PolicyServiceImpl} has a runtime dependency to the bean algorithmSuiteLoader
 * ({@link eu.domibus.core.ebms3.ws.algorithm.DomibusAlgorithmSuiteLoader}).
 */
@Service
@DependsOn("algorithmSuiteLoader")
public class PolicyServiceImpl implements PolicyService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PolicyServiceImpl.class);
    public static final String POLICIES = "policies";
    public static final String ENCRYPTEDPARTS = "EncryptedParts";

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Autowired
    private DomibusBus domibusBus;

    /**
     * To retrieve the domibus security policy xml from the specified location and create the Security Policy object.
     *
     * @param location the policy xml file location
     * @param securityProfile the current security profile
     * @return the security policy
     * @throws ConfigurationException if the policy xml cannot be read or parsed from the file
     */
    @Override
    @Cacheable(cacheManager = DomibusCacheConstants.CACHE_MANAGER, value = "policyCache", key = "#location + #securityProfile", sync = true)
    public Policy parsePolicy(final String location, final SecurityProfile securityProfile) throws ConfigurationException {
        final PolicyBuilder pb = domibusBus.getExtension(PolicyBuilder.class);
        try (InputStream inputStream = Files.newInputStream(new File(domibusConfigurationService.getConfigLocation(), location).toPath());
             InputStream inputStreamWithUpdatedPolicy = getAlgorithmSuiteInPolicy(inputStream, securityProfile)) {
            return pb.getPolicy(inputStreamWithUpdatedPolicy);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ConfigurationException(e);
        }
    }

    private InputStream getAlgorithmSuiteInPolicy(InputStream inputStream, final SecurityProfile securityProfile) {
        String modifiedPolicyString;
        String algoName = getAlgorithmName(securityProfile);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            modifiedPolicyString = reader.lines()
                    .map(line -> line.replace("${algorithmSuitePlaceholder}", algoName))
                    .collect(Collectors.joining());
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
        return IOUtils.toInputStream(modifiedPolicyString, Charset.defaultCharset());
    }

    private String getAlgorithmName(final SecurityProfile securityProfile) {
        if (securityProfile == null) {
            //legacy single keystore alias
            LOG.debug("Using [{}] algorithm for legacy single keystore alias", DomibusAlgorithmSuite.BASIC_128_GCM_SHA_256_MGF_SHA_256_RSA);
            return DomibusAlgorithmSuite.BASIC_128_GCM_SHA_256_MGF_SHA_256_RSA;
        }
        switch (securityProfile) {
            case ECC:
                LOG.debug("Using [{}] algorithm for [{}] profile", DomibusAlgorithmSuite.BASIC_128_GCM_SHA_256_MGF_SHA_256_ECC, securityProfile);
                return DomibusAlgorithmSuite.BASIC_128_GCM_SHA_256_MGF_SHA_256_ECC;
            case RSA:
            default:
                LOG.debug("Using [{}] algorithm for [{}] profile", DomibusAlgorithmSuite.BASIC_128_GCM_SHA_256_MGF_SHA_256_RSA, securityProfile);
                return DomibusAlgorithmSuite.BASIC_128_GCM_SHA_256_MGF_SHA_256_RSA;
        }
    }

    /**
     * To retrieve the domibus security policy xml based on the leg configuration and create the Security Policy object.
     *
     * @param legConfiguration the leg containing the security policy as configured in the pMode
     * @return the security policy
     * @throws ConfigurationException if the policy xml cannot be read or parsed from the file
     */
    @Override
    public Policy getPolicy(LegConfiguration legConfiguration) throws ConfigurationException {
        return parsePolicy(POLICIES + File.separator + legConfiguration.getSecurity().getPolicy(), legConfiguration.getSecurity().getProfile());
    }

    /**
     * Checks whether the security policy specified is a No Signature - No security policy.
     * If null is provided, a no security policy is assumed.
     * A no security policy would be used to avoid certificate validation.
     *
     * @param policy the security policy
     * @return boolean
     */
    @Override
    public boolean isNoSecurityPolicy(Policy policy) {

        if (null == policy) {
            LOG.securityWarn(DomibusMessageCode.SEC_NO_SECURITY_POLICY_USED, "Security policy provided is null! Assuming no security policy - no signature is specified!");
            return true;
        } else if (policy.isEmpty()) {
            LOG.securityWarn(DomibusMessageCode.SEC_NO_SECURITY_POLICY_USED, "Policy components are empty! No security policy specified!");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether the security policy specified is a No Encryption policy (EncryptedParts is not present in the policy)
     * If null is provided, a No Encryption policy is assumed.
     *
     * @param policy the security policy
     * @return boolean
     */
    @Override
    public boolean isNoEncryptionPolicy(Policy policy) {
        if (null == policy || policy.isEmpty()) {
            LOG.securityWarn(DomibusMessageCode.SEC_NO_SECURITY_POLICY_USED, "Empty or null security policy! Assuming no encryption is specified!");
            return true;
        }

        Iterator<List<Assertion>> alternatives = policy.getAlternatives();
        while (alternatives.hasNext()) {
            List<Assertion> assertions = alternatives.next();
            if (assertions.stream().anyMatch(as -> ENCRYPTEDPARTS.equals(as.getName().getLocalPart()))) {
                LOG.debug("Security policy [{}] includes encryptedParts.", policy.getName());
                return false;
            }
        }

        LOG.debug("There are no encryptedParts in the security policy [{}]", policy.getName());
        return true;
    }
}
