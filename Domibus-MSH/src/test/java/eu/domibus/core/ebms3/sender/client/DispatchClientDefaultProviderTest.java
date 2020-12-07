package eu.domibus.core.ebms3.sender.client;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cxf.DomibusHTTPConduitFactory;
import eu.domibus.core.proxy.DomibusProxy;
import eu.domibus.core.proxy.DomibusProxyService;
import eu.domibus.core.proxy.ProxyCxfUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_CONNECTION_KEEP_ALIVE;
import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class DispatchClientDefaultProviderTest {

    @Injectable
    private TLSReaderServiceImpl tlsReader;

    @Injectable
    private Executor taskExecutor;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected DomibusProxyService domibusProxyService;

    @Injectable
    protected ProxyCxfUtil proxyUtil;

    @Injectable
    protected final DomibusHTTPConduitFactory domibusHTTPConduitFactory;

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
    public void testGetClient(@Mocked org.apache.neethi.Policy policy,
                              @Mocked TLSClientParameters tlsClientParameters,
                              @Mocked DispatchImpl<SOAPMessage> dispatch,
                              @Mocked Client client,
                              @Mocked HTTPConduit httpConduit,
                              @Mocked HTTPClientPolicy httpClientPolicy) {

        Map<String, Object> requestContext = new HashMap<>();

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

        final String domain = "default";

        new Expectations(dispatchClientDefaultProvider) {{
            dispatchClientDefaultProvider.createWSServiceDispatcher(endpoint);
            times = 1;
            result = dispatch;

            dispatch.getRequestContext();
            times = 3;
            result = requestContext;

            dispatch.getClient();
            times = 1;
            result = client;

            client.getConduit();
            times = 1;
            result = httpConduit;

            httpConduit.getClient();
            times = 1;
            result = httpClientPolicy;

            dispatchClientDefaultProvider.setHttpClientPolicy(httpClientPolicy);
            times = 1;

            tlsReader.getTlsClientParameters(domain);
            times = 1;
            result = tlsClientParameters;

        }};

        Dispatch<SOAPMessage> result = dispatchClientDefaultProvider.getClient(domain, endpoint, algorithm, policy, pModeKey, false).get();

        new FullVerifications() {{

            httpConduit.setClient(httpClientPolicy);
            times = 1;

            httpConduit.setTlsClientParameters(tlsClientParameters);
            times = 1;

            proxyUtil.configureProxy(((HTTPClientPolicy) any), ((HTTPConduit) any));
            times = 1;
        }};

        assertEquals(dispatch, result);

        assertEquals(requestContext.get(PolicyConstants.POLICY_OVERRIDE), policy);
        assertEquals(requestContext.get(DispatchClientDefaultProvider.ASYMMETRIC_SIG_ALGO_PROPERTY), algorithm);
        assertEquals(requestContext.get(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY), pModeKey);
    }
}
