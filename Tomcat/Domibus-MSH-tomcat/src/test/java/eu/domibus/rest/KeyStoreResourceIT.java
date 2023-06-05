package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.KeystoreResource;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyStoreException;
import java.util.List;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

public class KeyStoreResourceIT extends AbstractIT {
    public static final String KEYSTORES = "keystores";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TruststoreResourceIT.class);

    @Autowired
    private KeystoreResource storeResource;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    @Autowired
    private MultiDomainCryptoServiceImpl multiDomainCryptoService;

    @Autowired
    CertificateHelper certificateHelper;

    @Before
    public void before() {
        resetInitialStore();
    }

    @Test
    public void replaceTrustStore() throws IOException {
        List<TrustStoreRO> entries = storeResource.listEntries();

        String fileName = "gateway_keystore2.jks";
        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, fileName);
        byte[] content = Files.readAllBytes(path);
        MultipartFile multiPartFile = new MockMultipartFile(fileName, fileName, "octetstream", content);

        storeResource.uploadKeystoreFile(multiPartFile, "test123");

        List<TrustStoreRO> newEntries = storeResource.listEntries();

        Assert.assertNotEquals(entries.size(), newEntries.size());

    }

    @Test
    public void isChangedObDisk() throws KeyStoreException, IOException {
        List<TrustStoreRO> trustStore = storeResource.listEntries();
        Assert.assertEquals(2, trustStore.size());

        boolean isChangedOnDisk = storeResource.isChangedOnDisk();
        Assert.assertFalse(isChangedOnDisk);

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, "gateway_keystore2.jks");
        byte[] content = Files.readAllBytes(path);
        Path currentPath = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, "gateway_keystore.jks");
        Files.write(currentPath, content, StandardOpenOption.WRITE);

        isChangedOnDisk = storeResource.isChangedOnDisk();
        Assert.assertTrue(isChangedOnDisk);

        trustStore = storeResource.listEntries();
        Assert.assertEquals(2, trustStore.size());

        storeResource.reset();
        trustStore = storeResource.listEntries();
        Assert.assertEquals(9, trustStore.size());
    }

    private void resetInitialStore() {
        try {
            String storePassword = "test123";
            Domain domain = DomainService.DEFAULT_DOMAIN;
            Path path = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, "gateway_keystore_original.jks");
            byte[] content = Files.readAllBytes(path);
            KeyStoreContentInfo storeInfo = certificateHelper.createStoreContentInfo(DOMIBUS_TRUSTSTORE_NAME, "gateway_keystore.jks", content, storePassword);
            multiDomainCryptoService.replaceKeyStore(domain, storeInfo);
        } catch (Exception e) {
            LOG.info("Error restoring initial keystore", e);
        }
    }
}
