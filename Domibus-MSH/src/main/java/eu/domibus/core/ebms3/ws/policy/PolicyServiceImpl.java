
package eu.domibus.core.ebms3.ws.policy;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.cxf.DomibusBus;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

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
     * @return the security policy
     * @throws ConfigurationException if the policy xml cannot be read or parsed from the file
     */
    @Override
    @Cacheable(value = "policyCache", sync = true)
    public Policy parsePolicy(final String location) throws ConfigurationException {
        final PolicyBuilder pb = domibusBus.getExtension(PolicyBuilder.class);
        try (InputStream inputStream = new FileInputStream(new File(domibusConfigurationService.getConfigLocation(), location))){
            return pb.getPolicy(inputStream);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ConfigurationException(e);
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
        return parsePolicy(POLICIES + File.separator + legConfiguration.getSecurity().getPolicy());
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
