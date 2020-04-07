package eu.domibus.core.ebms3.sender.client;

import org.apache.neethi.Policy;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface DispatchClientProvider {

    DispatchClientWrapper getClient(String domain, String endpoint, String algorithm, Policy policy, final String pModeKey, boolean cacheable);

    Dispatch<SOAPMessage> getLocalClient(String domain, String endpoint);
}
