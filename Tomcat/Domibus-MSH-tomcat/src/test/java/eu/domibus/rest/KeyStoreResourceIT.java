package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.certificate.CertificateServiceImpl;
import eu.domibus.web.rest.KeystoreResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_KEYSTORE_NAME;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

public class KeyStoreResourceIT extends AbstractIT {

    @Autowired
    private KeystoreResource storeResource;

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

//    @Test
//    public void testTruststoreEntries_ok() throws IOException {
//        createTrustStore();
//        createKeyStore();
//
//        List<TrustStoreEntry> entries = certificateService.getStoreEntries(DOMIBUS_KEYSTORE_NAME);
//
//        storeResource.reset();
//
//        List<TrustStoreEntry> newEntries = certificateService.getStoreEntries(DOMIBUS_KEYSTORE_NAME);
//
//        Assert.assertNotEquals(entries.size(), newEntries.size());
//    }

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
}
