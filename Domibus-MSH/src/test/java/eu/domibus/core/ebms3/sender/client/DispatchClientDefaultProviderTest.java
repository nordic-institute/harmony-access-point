package eu.domibus.core.ebms3.sender.client;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.proxy.DomibusProxy;
import eu.domibus.core.proxy.DomibusProxyService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.transport.http.URLConnectionHTTPConduit;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import java.util.concurrent.Executor;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_CONNECTION_KEEP_ALIVE;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class DispatchClientDefaultProviderTest {

    @Injectable
    private TLSReader tlsReader;

    @Injectable
    private Executor taskExecutor;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected DomibusProxyService domibusProxyService;

    @Tested
    DispatchClientDefaultProvider dispatchClientDefaultProvider;

    String connectionTimeout = "10";
    String receiveTimeout = "60";
    String allowChunking = "true";
    String keepAlive = "true";
    String chunkingThreshold = "100";

    private void prepareHTTPClientPolicyExpectations() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DispatchClientDefaultProvider.DOMIBUS_DISPATCHER_CONNECTIONTIMEOUT);
            result = connectionTimeout;

            domibusPropertyProvider.getProperty(DispatchClientDefaultProvider.DOMIBUS_DISPATCHER_RECEIVETIMEOUT);
            result = receiveTimeout;

            domibusPropertyProvider.getProperty(DispatchClientDefaultProvider.DOMIBUS_DISPATCHER_ALLOWCHUNKING);
            result = allowChunking;

            domibusPropertyProvider.getProperty(DispatchClientDefaultProvider.DOMIBUS_DISPATCHER_CHUNKINGTHRESHOLD);
            result = chunkingThreshold;

            domibusPropertyProvider.getProperty(DOMIBUS_DISPATCHER_CONNECTION_KEEP_ALIVE);
            result = keepAlive;
        }};
    }

    @Test
    public void testSetHttpClientPolicy(@Injectable HTTPClientPolicy httpClientPolicy) {

        prepareHTTPClientPolicyExpectations();

        dispatchClientDefaultProvider.setHttpClientPolicy(httpClientPolicy);

        new Verifications() {{
            httpClientPolicy.setConnectionTimeout(Integer.parseInt(connectionTimeout));
            httpClientPolicy.setReceiveTimeout(Integer.parseInt(receiveTimeout));
            httpClientPolicy.setAllowChunking(Boolean.valueOf(allowChunking));
            httpClientPolicy.setConnection(ConnectionType.KEEP_ALIVE);
            httpClientPolicy.setChunkingThreshold(Integer.parseInt(chunkingThreshold));
        }};
    }

    @Test
    public void testGetClient(@Injectable org.apache.neethi.Policy policy) {
        String endpoint = "https://tbd";
        String algorithm = "algorithm";
        String pModeKey = "pModeKey";
        DomibusProxy domibusProxy = new DomibusProxy();
        domibusProxy.setEnabled(true);
        domibusProxy.setHttpProxyHost("localhost");
        domibusProxy.setHttpProxyPort(8090);
        domibusProxy.setHttpProxyUser("proxyuser");
        domibusProxy.setHttpProxyPassword("proxypassword");
        domibusProxy.setNonProxyHosts("localhost,127.0.0.1");

        prepareHTTPClientPolicyExpectations();

        new Expectations() {{
            domibusProxyService.useProxy();
            result = true;

            domibusProxyService.isProxyUserSet();
            result = true;

            domibusProxyService.getDomibusProxy();
            result = domibusProxy;
        }};

        Dispatch<SOAPMessage> dispatch = dispatchClientDefaultProvider.getClient("default", endpoint, algorithm, policy, pModeKey, false).get();

        ProxyAuthorizationPolicy proxyAuthorizationPolicy = ((URLConnectionHTTPConduit) ((DispatchImpl) dispatch).getClient().getConduit()).getProxyAuthorization();
        Assert.assertEquals(domibusProxy.getHttpProxyUser(), proxyAuthorizationPolicy.getUserName());
        Assert.assertEquals(domibusProxy.getHttpProxyPassword(), proxyAuthorizationPolicy.getPassword());
    }
}
