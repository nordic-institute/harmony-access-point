package eu.domibus.api.cxf.http;

import org.apache.http.client.CredentialsProvider;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.http.HttpHost;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface ProxyUtilService {

    HttpHost getConfiguredProxy();

    CredentialsProvider getConfiguredCredentialsProvider();

    void configureProxy(HTTPClientPolicy httpClientPolicy, HTTPConduit httpConduit);
}
