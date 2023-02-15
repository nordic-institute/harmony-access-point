package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.certificate.CertificateServiceImpl;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.KeystoreResource;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

public class KeyStoreResourceIT extends AbstractIT {
    public static final String KEYSTORES = "keystores";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TruststoreResourceIT.class);

    @Autowired
    private KeystoreResource storeResource;

    @Autowired
    private CertificateServiceImpl certificateService;

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
        resetInitialStstore();
    }

    @Test
    public void replaceTrustStore() throws IOException {
        List<TrustStoreRO> entries = storeResource.listEntries();

        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("keystores/gateway_keystore2.jks")) {
            MultipartFile multiPartFile = new MockMultipartFile("gateway_keystore2.jks", "gateway_keystore2.jks",
                    "octetstream", IOUtils.toByteArray(resourceAsStream));

            storeResource.uploadKeystoreFile(multiPartFile, "test123");

            List<TrustStoreRO> newEntries = storeResource.listEntries();

            Assert.assertNotEquals(entries.size(), newEntries.size());
        }
    }


    private void resetInitialStstore() {
        try {
            String storePassword = "test123";
            Domain domain = DomainService.DEFAULT_DOMAIN;
            Path path = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, "gateway_keystore_original.jks");
            byte[] content = Files.readAllBytes(path);
            KeyStoreContentInfo storeInfo = certificateHelper.createStoreContentInfo(DOMIBUS_TRUSTSTORE_NAME, "gateway_keystore.jks", content, storePassword);
            multiDomainCryptoService.replaceTrustStore(domain, storeInfo);
        } catch (Exception e) {
            LOG.info("Error restoring initial keystore", e);
        }
    }
}
