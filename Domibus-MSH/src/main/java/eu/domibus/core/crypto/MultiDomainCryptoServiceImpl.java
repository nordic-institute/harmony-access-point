package eu.domibus.core.crypto;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.crypto.api.DomainCryptoService;
import eu.domibus.core.crypto.api.DomainCryptoServiceFactory;
import eu.domibus.api.pki.MultiDomainCryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
@Primary
public class MultiDomainCryptoServiceImpl extends BaseMultiDomainCryptoServiceImpl implements MultiDomainCryptoService {

    @Autowired
    private DomainCryptoServiceFactory domainCertificateProviderFactory;

    @Autowired
    private DomibusCacheService domibusCacheService;

    @Override
    public void replaceTrustStore(Domain domain, String storeFileName, byte[] store, String password) throws CryptoException {
        super.replaceTrustStore(domain, storeFileName, store, password);

        domibusCacheService.clearCache("certValidationByAlias");
    }

    @Override
    @Cacheable(value = "certValidationByAlias", key = "#domain.code + #alias")
    public boolean isCertificateChainValid(Domain domain, String alias) throws DomibusCertificateException {
        return super.isCertificateChainValid(domain, alias);
    }

    @Override
    protected DomainCryptoService createForDomain(Domain domain) {
        return domainCertificateProviderFactory.createDomainCryptoService(domain);
    }
}
