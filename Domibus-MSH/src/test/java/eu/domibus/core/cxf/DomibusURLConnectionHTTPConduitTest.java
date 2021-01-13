package eu.domibus.core.cxf;

import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.Address;
import org.apache.cxf.transport.http.URLConnectionHTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.junit.Assert;
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
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private DomibusBus bus;

    @Injectable
    private EndpointInfo endpointInfo;

    @Injectable
    private EndpointReferenceType target;

    @Tested
    private DomibusURLConnectionHTTPConduit domibusURLConnectionHTTPConduit;

    @Test
    public void setupConnection_HTTP() throws Exception{
        final Address address = new Address("http://host:8443");
        new MockUp<URLConnectionHTTPConduit>() {
            @Mock void setupConnection(Message message, Address connectionAddress, HTTPClientPolicy csPolicy) throws IOException {
                Assert.assertSame("Should have not changed the initial Address for HTTP endpoints",
                        address, connectionAddress);
                Assert.assertEquals("Should have not rewritten the URL for HTTP endpoints",
                        PROTOCOL_HTTP, connectionAddress.getURL().getProtocol());            }
        };

        domibusURLConnectionHTTPConduit.setupConnection(message, address, csPolicy);
    }

    @Test
    public void setupConnection_HTTPS_WITHOUT_SSL_OFFLOAD() throws Exception{
        final Address address = new Address("https://host:8443");
        new MockUp<URLConnectionHTTPConduit>() {
            @Mock void setupConnection(Message message, Address connectionAddress, HTTPClientPolicy csPolicy) throws IOException {
                Assert.assertSame("Should have not changed the initial Address for HTTPS endpoints when SSL OFFLOAD disabled",
                        address, connectionAddress);
                Assert.assertEquals("Should have not rewritten the URL for HTTPS endpoints when SSL OFFLOAD disabled",
                        PROTOCOL_HTTPS, connectionAddress.getURL().getProtocol());
            }
        };
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_CONNECTION_CXF_SSL_OFFLOAD_ENABLE); result = Boolean.FALSE;
        }};

        domibusURLConnectionHTTPConduit.setupConnection(message, address, csPolicy);
    }

    @Test
    public void setupConnection_HTTPS_WITH_SSL_OFFLOAD() throws Exception{
        final Address address = new Address("https://host:8443");
        new MockUp<URLConnectionHTTPConduit>() {
            @Mock void setupConnection(Message message, Address connectionAddress, HTTPClientPolicy csPolicy) throws IOException {
                Assert.assertEquals("Should have rewritten the address for HTTPS endpoints when SSL OFFLOAD enabled",
                        "http://host:8443", connectionAddress.getString());
                Assert.assertEquals("Should have kept the protocol of the address URL as HTTPS for HTTPS endpoints when SSL OFFLOAD enabled",
                        PROTOCOL_HTTPS, connectionAddress.getURL().getProtocol());
            }
        };
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_CONNECTION_CXF_SSL_OFFLOAD_ENABLE); result = Boolean.TRUE;
        }};

        domibusURLConnectionHTTPConduit.setupConnection(message, address, csPolicy);
    }
}