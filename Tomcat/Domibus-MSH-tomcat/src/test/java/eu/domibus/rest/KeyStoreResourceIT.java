package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.certificate.CertificateServiceImpl;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.core.crypto.TruststoreEntity;
import eu.domibus.web.rest.KeystoreResource;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_KEYSTORE_NAME;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

public class KeyStoreResourceIT extends AbstractIT {

    @Autowired
    private KeystoreResource storeResource;

    @Autowired
    private TruststoreDao truststoreDao;

    @Autowired
    private CertificateServiceImpl certificateService;

    @Before
    public void before() {
        cleanStores();
    }

    @After
    public void after() {
        cleanStores();
    }

    @Test
    public void testTruststoreEntries_ok() throws IOException {
        createTrustStore();
        createKeyStore();

        List<TrustStoreEntry> entries = certificateService.getTrustStoreEntries(DOMIBUS_KEYSTORE_NAME);

        storeResource.reset();

        List<TrustStoreEntry> newEntries = certificateService.getTrustStoreEntries(DOMIBUS_KEYSTORE_NAME);

        Assert.assertTrue(entries.size() != newEntries.size());
    }

    private void cleanStores() {
        removeStore(DOMIBUS_KEYSTORE_NAME);
        removeStore(DOMIBUS_TRUSTSTORE_NAME);
    }

    private void createKeyStore() throws IOException {
        createStore(DOMIBUS_KEYSTORE_NAME, "keystores/gateway_keystore2.jks");
    }

    private void createTrustStore() throws IOException {
        createStore(DOMIBUS_TRUSTSTORE_NAME, "keystores/gateway_truststore.jks");
    }

    private void createStore(String domibusKeystoreName, String filePath) throws IOException {
        TruststoreEntity domibusTruststoreEntity = new TruststoreEntity();
        domibusTruststoreEntity.setName(domibusKeystoreName);
        domibusTruststoreEntity.setType("JKS");
        domibusTruststoreEntity.setPassword("test123");
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
        byte[] trustStoreBytes = IOUtils.toByteArray(resourceAsStream);
        domibusTruststoreEntity.setContent(trustStoreBytes);
        truststoreDao.create(domibusTruststoreEntity);
    }

    private void removeStore(String domibusKeystoreName) {
        if (truststoreDao.existsWithName(domibusKeystoreName)) {
            TruststoreEntity trust = truststoreDao.findByName(domibusKeystoreName);
            truststoreDao.delete(trust);
        }
    }
}
