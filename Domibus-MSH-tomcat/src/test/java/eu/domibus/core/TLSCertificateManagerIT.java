package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.crypto.TLSCertificateManagerImpl;
import eu.domibus.core.crypto.TruststoreDao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static eu.domibus.core.crypto.TLSCertificateManagerImpl.TLS_TRUSTSTORE_NAME;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class TLSCertificateManagerIT extends AbstractIT {

    @Autowired
    private TLSCertificateManagerImpl tlsCertificateManager;

    @Autowired
    TruststoreDao truststoreDao;

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    @Test
    @Transactional
    public void persistTruststoresIfApplicable() {
        boolean isPersisted = truststoreDao.existsWithName(TLS_TRUSTSTORE_NAME);
        Assert.assertFalse(isPersisted);
        tlsCertificateManager.persistTruststoresIfApplicable();
        isPersisted = truststoreDao.existsWithName(TLS_TRUSTSTORE_NAME);
        Assert.assertTrue(isPersisted);
    }

    @Test
    @Transactional
    public void getTrustStoreEntries() {
        tlsCertificateManager.persistTruststoresIfApplicable();
        List<TrustStoreEntry> trustStoreEntries = tlsCertificateManager.getTrustStoreEntries();
        Assert.assertTrue(trustStoreEntries.size() == 2);
    }

    @Test
    @Transactional
    public void addCertificate() throws IOException {
        tlsCertificateManager.persistTruststoresIfApplicable();

        List<TrustStoreEntry> trustStoreEntries = tlsCertificateManager.getTrustStoreEntries();
        Assert.assertTrue(trustStoreEntries.size() == 2);

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(),"keystores", "green_gw.cer");
        byte[] content = Files.readAllBytes(path);
        tlsCertificateManager.addCertificate(content, "green_gw");

        trustStoreEntries = tlsCertificateManager.getTrustStoreEntries();
        Assert.assertTrue(trustStoreEntries.size() == 3);
    }
}
