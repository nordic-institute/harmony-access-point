package eu.domibus.core.ebms3.sender.client;

import eu.domibus.core.ehcache.IgnoreSizeOfWrapper;
import org.apache.neethi.Policy;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface DispatchClientProvider {

    IgnoreSizeOfWrapper<Dispatch<SOAPMessage>> getClient(String domain, String endpoint, String algorithm, Policy policy, final String pModeKey, boolean cacheable);

    Dispatch<SOAPMessage> getLocalClient(String domain, String endpoint);
}
