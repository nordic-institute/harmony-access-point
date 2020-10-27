package eu.domibus.ext.services;

import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

/**
 * Interface allowing Domibus extension/plugin to use cxf proxy configuration of Domibus.
 *
 * @author François Gautier
 * @since 5.0
 */
public interface ProxyCxfUtilExtService {

    /**
     * Configure the {@param httpClientPolicy} and {@param httpConduit} with the proxy configuration found in domibus.properties
     */
    void configureProxy(HTTPClientPolicy httpClientPolicy, HTTPConduit httpConduit) ;
}
