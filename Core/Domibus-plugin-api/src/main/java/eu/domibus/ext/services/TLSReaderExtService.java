package eu.domibus.ext.services;

import org.apache.cxf.configuration.jsse.TLSClientParameters;

/**
 * Interface allowing Domibus extension/plugin to use TLS configuration of Domibus.
 *
 * @author François Gautier
 * @since 5.0
 */
public interface TLSReaderExtService {

    /**
     * Read TLS properties in domibus.properties and generate a {@link TLSClientParameters}
     *
     * @return {@link TLSClientParameters} for a given {@param domainCode}
     */
    TLSClientParameters getTlsClientParameters(String domainCode);
}
