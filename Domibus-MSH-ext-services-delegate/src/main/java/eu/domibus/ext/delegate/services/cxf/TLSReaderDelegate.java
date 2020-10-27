package eu.domibus.ext.delegate.services.cxf;

import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.ext.services.TLSReaderExtService;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.springframework.stereotype.Service;

/**
 * Delegate class allowing Domibus extension/plugin to use TLS configuration of Domibus.
 *
 * @author François Gautier
 * @since 5.0
 *
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
