package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.core.crypto.TruststoreEntity;
import eu.domibus.web.rest.TruststoreResource;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

@Transactional
public class TruststoreResourceIT extends AbstractIT {

    @Autowired
    private TruststoreResource truststoreResource;

    @Autowired
    private TruststoreDao truststoreDao;

    @Before
    public void clean() {
        if (truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_NAME)) {
            TruststoreEntity trust = truststoreDao.findByName(DOMIBUS_TRUSTSTORE_NAME);
            truststoreDao.delete(trust);
        }
    }

    @Ignore
    @Test
    public void testTruststoreEntries_ok() throws IOException {
        eu.domibus.core.crypto.TruststoreEntity domibusTruststoreEntity = new TruststoreEntity();
        domibusTruststoreEntity.setName(DOMIBUS_TRUSTSTORE_NAME);
        domibusTruststoreEntity.setType("JKS");
        domibusTruststoreEntity.setPassword("test123");
        byte[] trustStoreBytes = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("keystores/gateway_truststore.jks"));
        domibusTruststoreEntity.setContent(trustStoreBytes);

        truststoreDao.create(domibusTruststoreEntity);

        final String originalFilename = "truststore.jks";
        MultipartFile tlsTruststore = new MockMultipartFile(originalFilename, originalFilename, "application/octet-stream", trustStoreBytes);
        truststoreResource.uploadTruststoreFile(tlsTruststore, "test123");

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
}
