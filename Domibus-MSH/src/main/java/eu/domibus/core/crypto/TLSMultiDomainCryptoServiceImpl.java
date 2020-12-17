package eu.domibus.core.crypto;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.crypto.api.DomainCryptoService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class TLSMultiDomainCryptoServiceImpl extends MultiDomainCryptoServiceImpl {

    @Autowired
    protected ObjectProvider<TLSDomainCryptoServiceImpl> objectProvider;

    @Override
    protected DomainCryptoService createForDomain(Domain domain) {
        return objectProvider.getObject(domain);
    }

}
