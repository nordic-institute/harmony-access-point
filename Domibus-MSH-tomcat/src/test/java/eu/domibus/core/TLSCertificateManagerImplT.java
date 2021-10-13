package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.core.crypto.TLSCertificateManagerImpl;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static eu.domibus.core.crypto.TLSCertificateManagerImpl.TLS_TRUSTSTORE_NAME;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class TLSCertificateManagerImplT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(TLSCertificateManagerImplT.class);

    @Autowired
    private TLSCertificateManagerImpl tlsCertificateManager;

    @Autowired
    TruststoreDao truststoreDao;

    @Test
    @Transactional
    public void persistTruststoresIfApplicable() {
        boolean isPersisted = truststoreDao.existsWithName(TLS_TRUSTSTORE_NAME);
        Assert.assertFalse(isPersisted);
        tlsCertificateManager.persistTruststoresIfApplicable();
        isPersisted = truststoreDao.existsWithName(TLS_TRUSTSTORE_NAME);
        Assert.assertTrue(isPersisted);
    }

}
