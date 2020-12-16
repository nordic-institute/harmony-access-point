package eu.domibus.core.crypto;

import eu.domibus.api.crypto.CryptoException;
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

//    @Autowired
//    private DomibusCacheService domibusCacheService;

//    @Autowired
//    TLSDomainCryptoServiceFactoryImpl tlsDomainCryptoServiceFactory;

    @Override
    protected DomainCryptoService createForDomain(Domain domain) {
        DomainCryptoService res = objectProvider.getObject(domain);
        return res;
//        return tlsDomainCryptoServiceFactory.createDomainCryptoService(domain);
    }

    @Override
    public void replaceTrustStore(Domain domain, String storeFileName, byte[] store, String password) throws CryptoException {
        super.replaceTrustStore(domain, storeFileName, store, password);

//        domibusCacheService.clearCache("certValidationByAlias"); //which/any cache to clear??
    }
}
