package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.core.crypto.TruststoreEntity;
import eu.domibus.web.rest.TruststoreResource;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_TRUSTSTORE_LOCATION;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_KEYSTORE_NAME;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class TruststoreResourceIT extends AbstractIT {

    @Autowired
    private TruststoreResource truststoreResource;

    @Autowired
    private TruststoreDao truststoreDao;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Before
    public void before() {
        removeStore(DOMIBUS_KEYSTORE_NAME);
        removeStore(DOMIBUS_TRUSTSTORE_NAME);
    }

    @After
    public void after() {
        removeStore(DOMIBUS_KEYSTORE_NAME);
        removeStore(DOMIBUS_TRUSTSTORE_NAME);
    }

    @Test
    public void testTruststoreEntries_ok() throws IOException {

        createTrustStore();

        List<TrustStoreRO> trustStoreROS = truststoreResource.trustStoreEntries();
        for (TrustStoreRO trustStoreRO : trustStoreROS) {
            Assert.assertNotNull("Certificate name should be populated in TrustStoreRO:", trustStoreRO.getName());
            Assert.assertNotNull("Certificate subject should be populated in TrustStoreRO:", trustStoreRO.getSubject());
            Assert.assertNotNull("Certificate issuer should be populated in TrustStoreRO:", trustStoreRO.getIssuer());
            Assert.assertNotNull("Certificate validity from should be populated in TrustStoreRO:", trustStoreRO.getValidFrom());
            Assert.assertNotNull("Certificate validity until should be populated in TrustStoreRO:", trustStoreRO.getValidUntil());
            Assert.assertNotNull("Certificate fingerprints should be populated in TrustStoreRO:", trustStoreRO.getFingerprints());
            Assert.assertNotNull("Certificate imminent expiry alert days should be populated in TrustStoreRO:", trustStoreRO.getCertificateExpiryAlertDays());
            Assert.assertEquals("Certificate imminent expiry alert days should be populated in TrustStoreRO:", 60, trustStoreRO.getCertificateExpiryAlertDays());
        }
    }

    @Test
    public void replaceTrustStore() throws IOException {
        createKeyStore();
        createTrustStore();

        String location = domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
        String back = location.replace("gateway_truststore.jks", "gateway_truststore_back.jks");
        Files.copy(Paths.get(location), Paths.get(back), REPLACE_EXISTING);

        List<TrustStoreRO> entries = truststoreResource.trustStoreEntries();

        try(InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("keystores/gateway_truststore2.jks")) {
            MultipartFile multiPartFile = new MockMultipartFile("gateway_truststore2.jks", "gateway_truststore2.jks",
                    "octetstream", IOUtils.toByteArray(resourceAsStream));

            truststoreResource.uploadTruststoreFile(multiPartFile, "test123");

            List<TrustStoreRO> newEntries = truststoreResource.trustStoreEntries();

            Assert.assertTrue(entries.size() != newEntries.size());

            Files.copy(Paths.get(back), Paths.get(location), REPLACE_EXISTING);
            Files.delete(Paths.get(back));
        }
    }

    @Test
    public void replaceKeyStoreWithDifferentType() throws IOException {
        createKeyStore();
        createTrustStore();

        String location = domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
        String back = location.replace("gateway_truststore.jks", "gateway_truststore_back.jks");
        Files.copy(Paths.get(location), Paths.get(back), REPLACE_EXISTING);

        List<TrustStoreRO> entries = truststoreResource.trustStoreEntries();

        try(InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("keystores/gateway_truststore.p12")) {
            MultipartFile multiPartFile = new MockMultipartFile("gateway_truststore.p12", "gateway_truststore.p12",
                    "octetstream", IOUtils.toByteArray(resourceAsStream));

            truststoreResource.uploadTruststoreFile(multiPartFile, "test123");

            List<TrustStoreRO> newEntries = truststoreResource.trustStoreEntries();

            Assert.assertTrue(entries.size() == newEntries.size());

            Files.copy(Paths.get(back), Paths.get(location), REPLACE_EXISTING);
            Files.delete(Paths.get(back));
        }
    }

    @Test
    @Ignore
    public void isChangedOnDisk() throws IOException {
        createTrustStore();

        boolean changedOnDisk = truststoreResource.isChangedOnDisk();
        Assert.assertFalse(changedOnDisk);

        String location = domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
        String back = location.replace("gateway_truststore.jks", "gateway_truststore_back.jks");
        String newLoc = location.replace("gateway_truststore.jks", "gateway_truststore2.jks");
        Files.copy(Paths.get(location), Paths.get(back), REPLACE_EXISTING);
        Files.copy(Paths.get(newLoc), Paths.get(location), REPLACE_EXISTING);
        Files.setLastModifiedTime(Paths.get(location), FileTime.from(new Date().toInstant()));

        changedOnDisk = truststoreResource.isChangedOnDisk();
        Assert.assertTrue(changedOnDisk);

        Files.copy(Paths.get(back), Paths.get(location), REPLACE_EXISTING);
        changedOnDisk = truststoreResource.isChangedOnDisk();
        Assert.assertFalse(changedOnDisk);

        Files.delete(Paths.get(back));
    }

    private void removeStore(String domibusKeystoreName) {
        if (truststoreDao.existsWithName(domibusKeystoreName)) {
            TruststoreEntity trust = truststoreDao.findByName(domibusKeystoreName);
            truststoreDao.delete(trust);
        }
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
        try(InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            byte[] trustStoreBytes = IOUtils.toByteArray(resourceAsStream);
            domibusTruststoreEntity.setContent(trustStoreBytes);
            truststoreDao.create(domibusTruststoreEntity);
        }
    }

}
