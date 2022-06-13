
package eu.domibus.core.ebms3.ws.policy;

import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.common.model.configuration.LegConfiguration;
import org.apache.neethi.Policy;

/**
 * @author Arun Raj
 * @since 3.3
 */
public interface PolicyService {

    boolean isNoSecurityPolicy(Policy policy);

    boolean isNoEncryptionPolicy(Policy policy);

    Policy parsePolicy(final String location) throws ConfigurationException;

    Policy getPolicy(final LegConfiguration legConfiguration) throws ConfigurationException;


}
