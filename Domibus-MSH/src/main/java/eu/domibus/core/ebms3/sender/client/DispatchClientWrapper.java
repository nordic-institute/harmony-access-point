package eu.domibus.core.ebms3.sender.client;

import net.sf.ehcache.pool.sizeof.annotations.IgnoreSizeOf;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;

/**
 * @author Catalin Enache
 * @since 4.2
 */
public class DispatchClientWrapper {

    @IgnoreSizeOf
    private Dispatch<SOAPMessage> client;

    public DispatchClientWrapper() {};

    public DispatchClientWrapper(Dispatch<SOAPMessage> client) {
        this.client = client;
    }


    public Dispatch<SOAPMessage> getClient() {
        return client;
    }

    public void setClient(Dispatch<SOAPMessage> client) {
        this.client = client;
    }
}
