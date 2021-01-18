package eu.domibus.api.cxf;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.TLSClientParametersType;

/**
 * @author François Gautier
 * @author Ion Perpegel
 * @since 5.0
 */
public interface TLSReaderService {
    TLSClientParameters getTlsClientParameters(String domainCode);
    TLSClientParametersType getTlsClientParametersType(String domainCode);

    void reset(String domainCode);
}
