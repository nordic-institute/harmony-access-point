package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateInitValueType;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.core.crypto.DefaultDomainCryptoServiceSpiImpl;
import eu.domibus.core.crypto.DomainCryptoServiceFactory;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class MultiDomainCryptoServiceImplT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(MultiDomainCryptoServiceImplT.class);

    private final String DOMIBUS_TRUSTSTORE_FILE_NAME = "domibus.truststore.jks";

    @Autowired
    private MultiDomainCryptoServiceImpl multiDomainCryptoService;

    @Autowired
    TruststoreDao truststoreDao;

    @Autowired
    DomainCryptoServiceFactory domainCertificateProviderFactory1;

    @Autowired
    DefaultDomainCryptoServiceSpiImpl defaultDomainCryptoServiceSpi;

    @Autowired
    CertificateService certificateService;

    @Test
    @Transactional
    public void persistTruststoresIfApplicable() {
        boolean isPersisted = truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_FILE_NAME);
        Assert.assertFalse(isPersisted);
        multiDomainCryptoService.persistTruststoresIfApplicable();
        isPersisted = truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(isPersisted);
    }

    @Test
    @Transactional
    public void replaceTrustStore() {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        String password = "test123";
        multiDomainCryptoService.persistTruststoresIfApplicable();
        byte[] store = certificateService.getTruststoreContent(DOMIBUS_TRUSTSTORE_NAME);

        multiDomainCryptoService.replaceTrustStore(domain, DOMIBUS_TRUSTSTORE_FILE_NAME, store, password, Arrays.asList(CertificateInitValueType.TRUSTSTORE));
        boolean isPersisted = truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(isPersisted);
    }

}