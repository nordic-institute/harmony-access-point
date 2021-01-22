package eu.domibus.api.cxf;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.TLSClientParametersType;

/**
 * Retrieves tls truststore itself and its parameters from clientauthentication.xml gived a domain
 *
 * @author François Gautier
 * @author Ion Perpegel
 * @since 5.0
 */
public interface TLSReaderService {
    /**
     * Retrieves the TLS truststore parameters needed for operational flow
     * @param domainCode
     * @return
     */
    TLSClientParameters getTlsClientParameters(String domainCode);

    /**
     * Retrieves the parameters like type, location, password needed for configuration flow
     * @param domainCode
     * @return
     */
    TLSClientParametersType getTlsClientParametersType(String domainCode);

    /**
     * Dismisses the TLS data from the cache forcing a fresh reload on the next use
     * @param domainCode
     */
    void reset(String domainCode);
}
