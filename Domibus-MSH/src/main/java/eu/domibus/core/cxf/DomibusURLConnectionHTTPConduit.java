package eu.domibus.core.cxf;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.Address;
import org.apache.cxf.transport.http.URLConnectionHTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

import java.io.IOException;
import java.net.URL;

/**
 * A Domibus custom {@code URLConnectionHttpConduit} that replaces the default HTTPS {@code URL} handlers for HTTPS
 * endpoints in order to prevent the SSL handshake from happening at the Domibus level.
 *
 * <p>Note: this should only be used when offloading the SSL to another responsible application (e.g. a forward SSL
 * proxy).</p>
 *
 * @author Sestian-Ion TINCU
 */
public class DomibusURLConnectionHTTPConduit extends URLConnectionHTTPConduit {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusURLConnectionHTTPConduit.class);

    public static final String PROTOCOL_HTTP = "http";

    public static final String PROTOCOL_HTTPS = "https";

    public DomibusURLConnectionHTTPConduit(DomibusHttpsURLConnectionFactory domibusHttpsURLConnectionFactory,
                                           Bus bus,
                                           EndpointInfo endpointInfo,
                                           EndpointReferenceType target) throws IOException {
        super(bus, endpointInfo, target);
        this.connectionFactory = domibusHttpsURLConnectionFactory;
    }

    @Override
    protected void setupConnection(Message message, Address address, HTTPClientPolicy csPolicy) throws IOException {
        URL url = address.getURL();
        if(StringUtils.equalsIgnoreCase(url.getProtocol(), PROTOCOL_HTTPS)) {
            try {
                LOG.info("Switch [{}] to an HTTP URL for SSL offloading", address.getString());
                String result = StringUtils.replaceOnce(address.getString(), PROTOCOL_HTTPS + ":", PROTOCOL_HTTP + ":");
                address = new Address(result);

                LOG.info("Revert the protocol part of the HTTP URL back to HTTPS");
                FieldUtils.writeField(address.getURL(), "protocol", PROTOCOL_HTTPS, true);
            } catch (Exception e) {
                LOG.error("An error occurred when switching the URL to HTTP for SSL offloading", e);
            }
        }

        super.setupConnection(message, address, csPolicy);
    }
}
