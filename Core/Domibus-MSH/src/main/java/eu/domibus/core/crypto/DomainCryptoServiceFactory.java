package eu.domibus.core.crypto;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.KeystorePersistenceService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Component
public class DomainCryptoServiceFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCryptoServiceFactory.class);

    protected final Provider<List<DomainCryptoServiceSpi>> domainCryptoServiceSpiListProvider;

    protected final DomibusPropertyProvider domibusPropertyProvider;

    protected final CertificateService certificateService;

    protected final KeystorePersistenceService keystorePersistenceService;

    protected final DomibusCoreMapper coreMapper;

    public DomainCryptoServiceFactory(Provider<List<DomainCryptoServiceSpi>> domainCryptoServiceSpiListProvider,
                                      DomibusPropertyProvider domibusPropertyProvider,
                                      CertificateService certificateService,
                                      KeystorePersistenceService keystorePersistenceService, DomibusCoreMapper coreMapper) {
        this.domainCryptoServiceSpiListProvider = domainCryptoServiceSpiListProvider;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.certificateService = certificateService;
        this.keystorePersistenceService = keystorePersistenceService;
        this.coreMapper = coreMapper;
    }

    public DomainCryptoServiceImpl domainCryptoService(Domain domain) {
        LOG.debug("Instantiating the certificate provider for domain [{}]", domain);

        final DomainCryptoServiceImpl bean = new DomainCryptoServiceImpl(domain, domainCryptoServiceSpiListProvider.get(),
                domibusPropertyProvider, certificateService, keystorePersistenceService, coreMapper);
        bean.init();
        return bean;
    }

}
