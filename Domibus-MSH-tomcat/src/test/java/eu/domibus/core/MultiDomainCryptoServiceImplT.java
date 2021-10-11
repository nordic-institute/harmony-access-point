package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.core.spring.lock.LockEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;
import static eu.domibus.core.spring.DomibusContextRefreshedListener.SYNC_LOCK_KEY;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class MultiDomainCryptoServiceImplT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(MultiDomainCryptoServiceImplT.class);

    @Autowired
    private MultiDomainCryptoServiceImpl multiDomainCryptoService;

    @Autowired
    TruststoreDao truststoreDao;

    @Test
    @Transactional
    public void findSyncLock() {
        boolean isPersisted = truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertFalse(isPersisted);
        multiDomainCryptoService.persistTruststoresIfApplicable();
        isPersisted = truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(isPersisted);
    }

}
