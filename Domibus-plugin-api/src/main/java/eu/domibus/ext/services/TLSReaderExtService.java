package eu.domibus.ext.services;

import org.apache.cxf.configuration.jsse.TLSClientParameters;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface TLSReaderExtService {
    TLSClientParameters getTlsClientParameters(String domainCode);
}
