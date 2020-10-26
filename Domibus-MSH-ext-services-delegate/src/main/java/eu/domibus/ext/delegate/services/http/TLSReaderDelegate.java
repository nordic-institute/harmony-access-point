package eu.domibus.ext.delegate.services.http;

import eu.domibus.api.http.TLSReaderService;
import eu.domibus.ext.services.TLSReaderExtService;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.springframework.stereotype.Service;

/**
 * @author François Gautier
 * @since 5.0
 *
 * Delegate class allowing Domibus extension/plugin to use TLS configuration of Domibus.
 */
@Service
public class TLSReaderDelegate implements TLSReaderExtService {
    private final TLSReaderService tlsReaderService;

    public TLSReaderDelegate(TLSReaderService tlsReaderService) {
        this.tlsReaderService = tlsReaderService;
    }

    public TLSClientParameters getTlsClientParameters(String domainCode){
        return tlsReaderService.getTlsClientParameters(domainCode);
    }

}
