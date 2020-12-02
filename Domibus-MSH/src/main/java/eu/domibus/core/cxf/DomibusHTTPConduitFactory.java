package eu.domibus.core.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPConduitFactory;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * A Domibus factory providing the creation of custom {@code DomibusURLConnectionHTTPConduit} objects.
 *
 * <p>Note: this factory should only be used when offloading the SSL to another responsible application (e.g. a forward
 * SSL proxy).</p>
 *
 * @author Sestian-Ion TINCU
 * @since 5.0
 */
@Component
public class DomibusHTTPConduitFactory implements HTTPConduitFactory {

    private final DomibusHttpsURLConnectionFactory domibusHttpsURLConnectionFactory;

    private final ObjectProvider<DomibusURLConnectionHTTPConduit> domibusURLConnectionHTTPConduitProvider;

    public DomibusHTTPConduitFactory(DomibusHttpsURLConnectionFactory domibusHttpsURLConnectionFactory,
                                     ObjectProvider<DomibusURLConnectionHTTPConduit> domibusURLConnectionHTTPConduitProvider) {
        this.domibusHttpsURLConnectionFactory = domibusHttpsURLConnectionFactory;
        this.domibusURLConnectionHTTPConduitProvider = domibusURLConnectionHTTPConduitProvider;
    }

    @Override
    public HTTPConduit createConduit(HTTPTransportFactory httpTransportFactory, Bus bus,
                                     EndpointInfo endpointInfo, EndpointReferenceType target) {
         return domibusURLConnectionHTTPConduitProvider.getObject(domibusHttpsURLConnectionFactory, bus, endpointInfo, target);
    }
}
