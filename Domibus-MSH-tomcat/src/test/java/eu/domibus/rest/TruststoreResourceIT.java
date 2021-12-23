package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.core.crypto.TruststoreEntity;
import eu.domibus.web.rest.TruststoreResource;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class TruststoreResourceIT extends AbstractIT {

    @Autowired
    private TruststoreResource truststoreResource;

    @Autowired
    private TruststoreDao truststoreDao;

    @Test
    public void testTruststoreEntries_ok() throws IOException {

        eu.domibus.core.crypto.TruststoreEntity domibusTruststoreEntity = new TruststoreEntity();
        domibusTruststoreEntity.setName(new Random().nextLong() + "domibus.truststore");
        domibusTruststoreEntity.setType("JKS");
        domibusTruststoreEntity.setPassword("test123");
        byte[] trustStoreBytes = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("keystores/gateway_truststore.jks"));
        domibusTruststoreEntity.setContent(trustStoreBytes);

        truststoreDao.create(domibusTruststoreEntity);

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
