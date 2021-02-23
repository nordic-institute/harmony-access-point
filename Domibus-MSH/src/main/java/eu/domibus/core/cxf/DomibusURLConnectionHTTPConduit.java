package eu.domibus.core.cxf;

import eu.domibus.core.ssl.offload.SslOffloadService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.Address;
import org.apache.cxf.transport.http.URLConnectionHTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

import java.io.IOException;

/**
 * A Domibus custom {@code URLConnectionHttpConduit} that support SSL offloading by replacing the default HTTPS
 * {@code URL} handlers for HTTPS endpoints in order to prevent the SSL handshake from happening at the Domibus level.
 *
 * @author Sestian-Ion TINCU
 * @since 5.0
 */
public class DomibusURLConnectionHTTPConduit extends URLConnectionHTTPConduit {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusURLConnectionHTTPConduit.class);

    public static final String PROTOCOL_HTTP = "http";

    public static final String PROTOCOL_HTTPS = "https";

    private final SslOffloadService sslOffloadService;

    public DomibusURLConnectionHTTPConduit(DomibusHttpsURLConnectionFactory domibusHttpsURLConnectionFactory,
                                           SslOffloadService sslOffloadService,
                                           DomibusBus bus,
                                           EndpointInfo endpointInfo,
                                           EndpointReferenceType target) throws IOException {
        super(bus, endpointInfo, target);
        this.connectionFactory = domibusHttpsURLConnectionFactory;
        this.sslOffloadService = sslOffloadService;
    }

    @Override
    protected void setupConnection(Message message, Address address, HTTPClientPolicy csPolicy) throws IOException {
        if(sslOffloadService.isSslOffloadEnabled(address.getURL())) {
            address = sslOffloadService.offload(address);
        }
        super.setupConnection(message, address, csPolicy);
    }
}
