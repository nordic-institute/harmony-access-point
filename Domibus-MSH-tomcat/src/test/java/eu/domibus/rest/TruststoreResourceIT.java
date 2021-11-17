package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.core.crypto.TruststoreEntity;
import eu.domibus.core.util.MultiPartFileUtilImpl;
import eu.domibus.web.rest.TruststoreResource;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class TruststoreResourceIT extends AbstractIT {

    @Autowired
    private MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    private DomainContextProvider domainProvider;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private MultiPartFileUtilImpl multiPartFileUtil;

    @Autowired
    private TruststoreResource truststoreResource;

    @Autowired
    private TruststoreDao truststoreDao;

    @Test
    public void testTruststoreEntries_ok() throws IOException, URISyntaxException {

        eu.domibus.core.crypto.TruststoreEntity domibusTruststoreEntity = new TruststoreEntity();
        domibusTruststoreEntity.setName("domibus.truststore");
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
