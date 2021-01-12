package eu.domibus.api.cxf;

import org.apache.cxf.configuration.jsse.TLSClientParameters;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface TLSReaderService {
    TLSClientParameters getTlsClientParameters(String domainCode);
}
