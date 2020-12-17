package eu.domibus.core.crypto;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.crypto.api.DomainCryptoService;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class TLSDomainCryptoServiceImpl extends BaseDomainCryptoServiceImpl implements DomainCryptoService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TLSDomainCryptoServiceImpl.class);

    @Autowired
    @Qualifier("TLSCryptoService") //better solution maybe??
    private DomainCryptoServiceSpi domainCryptoServiceSpi;

    public TLSDomainCryptoServiceImpl(Domain domain) {
        super(domain);
    }

    @PostConstruct
    protected void init() {
        super.init(domainCryptoServiceSpi);
        LOG.info("Create TLS provider identifier:[{}] for domain:[{}]", iamProvider.getIdentifier(), domain.getName());
    }

    @Override
    public String getTrustStoreType() {
        //todo: decide if property or what???
        return "jks";
    }

}
