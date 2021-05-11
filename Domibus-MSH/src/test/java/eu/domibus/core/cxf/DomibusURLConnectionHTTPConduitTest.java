package eu.domibus.core.cxf;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.ssl.offload.SslOffloadService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.Address;
import org.apache.cxf.transport.http.URLConnectionHTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_CONNECTION_CXF_SSL_OFFLOAD_ENABLE;
import static eu.domibus.core.cxf.DomibusURLConnectionHTTPConduit.PROTOCOL_HTTP;
import static eu.domibus.core.cxf.DomibusURLConnectionHTTPConduit.PROTOCOL_HTTPS;

@RunWith(JMockit.class)
public class DomibusURLConnectionHTTPConduitTest {

    @Injectable
    private Message message;

    @Injectable
    private HTTPClientPolicy csPolicy;

    @Injectable
    private DomibusHttpsURLConnectionFactory domibusHttpsURLConnectionFactory;

    @Injectable
    private SslOffloadService sslOffloadService;

    @Injectable
    private DomibusBus bus;

    @Injectable
    private EndpointInfo endpointInfo;

    @Injectable
    private EndpointReferenceType target;

    @Tested
    private DomibusURLConnectionHTTPConduit domibusURLConnectionHTTPConduit;

    @Before
    public void stubSuperCallToSetupConnection() {
        new MockUp<URLConnectionHTTPConduit>() {
            @Mock void setupConnection(Message message, Address connectionAddress, HTTPClientPolicy csPolicy) {
                // do nothing
            }
        };
    }

    @Test
    public void setupConnection_WithoutSslOffload() throws Exception{
        final Address address = new Address("http://host:8443");
        new Expectations() {{
           sslOffloadService.isSslOffloadEnabled(address.getURL());
           result = false;
        }};

        domibusURLConnectionHTTPConduit.setupConnection(message, address, csPolicy);

        new FullVerifications() {{
            sslOffloadService.offload(address); times = 0;
        }};
    }

    @Test
    public void setupConnection_WithSslOffload() throws Exception{
        final Address address = new Address("https://host:8443");
        new Expectations() {{
            sslOffloadService.isSslOffloadEnabled(address.getURL());
            result = true;
        }};

        domibusURLConnectionHTTPConduit.setupConnection(message, address, csPolicy);
        new FullVerifications() {{
            sslOffloadService.offload(address); times = 1;
        }};
    }
}