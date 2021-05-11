package eu.domibus.core.cxf;

import eu.domibus.core.ssl.offload.SslOffloadService;
import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPConduitFactory;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * A Domibus factory providing the creation of custom {@code DomibusURLConnectionHTTPConduit} objects.
 *
 * @author Sestian-Ion TINCU
 * @since 5.0
 */
@Component
public class DomibusHTTPConduitFactory implements HTTPConduitFactory {

    private final DomibusHttpsURLConnectionFactory domibusHttpsURLConnectionFactory;

    private final ObjectProvider<DomibusURLConnectionHTTPConduit> domibusURLConnectionHTTPConduitProvider;

    private final SslOffloadService sslOffloadService;

    public DomibusHTTPConduitFactory(DomibusHttpsURLConnectionFactory domibusHttpsURLConnectionFactory,
                                     ObjectProvider<DomibusURLConnectionHTTPConduit> domibusURLConnectionHTTPConduitProvider, SslOffloadService sslOffloadService) {
        this.domibusHttpsURLConnectionFactory = domibusHttpsURLConnectionFactory;
        this.domibusURLConnectionHTTPConduitProvider = domibusURLConnectionHTTPConduitProvider;
        this.sslOffloadService = sslOffloadService;
    }

    @Override
    public HTTPConduit createConduit(HTTPTransportFactory httpTransportFactory,
                                     @Qualifier(Bus.DEFAULT_BUS_ID) Bus bus, // Pointing to the DomibusBus but keeping it as Bus for the method overriding
                                     EndpointInfo endpointInfo,
                                     EndpointReferenceType target) {
         return domibusURLConnectionHTTPConduitProvider.getObject(domibusHttpsURLConnectionFactory, sslOffloadService, bus, endpointInfo, target);
    }
}
