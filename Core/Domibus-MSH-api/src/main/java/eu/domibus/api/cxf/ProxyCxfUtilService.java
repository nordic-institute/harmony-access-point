package eu.domibus.api.cxf;

import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface ProxyCxfUtilService {

    void configureProxy(HTTPClientPolicy httpClientPolicy, HTTPConduit httpConduit);

}
