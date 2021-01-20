package eu.domibus.core.crypto;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateEntry;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.crypto.api.DomainCryptoService;
import eu.domibus.core.crypto.api.DomainCryptoServiceFactory;
import mockit.*;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.Test;

import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class MultiDomainCryptoEbms3ServiceImplTest {

    @Tested
    MultiDomainCryptoServiceImpl mdCryptoService;

    @Injectable
    DomainCryptoServiceFactory domainCertificateProviderFactory;

    @Injectable
    private DomibusCacheService domibusCacheService;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    CertificateService certificateService;

    @Test
    public void getX509Certificates() throws WSSecurityException {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        //CryptoType cryptoType = new CryptoType(CryptoType.TYPE.SUBJECT_DN);
        DomainCryptoServiceImpl cryptoService = new DomainCryptoServiceImpl(domain);
        //cryptoService.init();
        X509Certificate[] certs = null;

        new Expectations() {{
            domainCertificateProviderFactory.createDomainCryptoService(domain);
            result = cryptoService;
        }};

        DomainCryptoService res = mdCryptoService.getDomainCertificateProvider(domain);
        assertEquals(cryptoService, res);

        DomainCryptoService res2 = mdCryptoService.getDomainCertificateProvider(domain);
        assertEquals(cryptoService, res2);

        new Verifications() {{
        }};
    }

    @Test
    public void getPrivateKeyPassword(@Mocked DomainCryptoServiceImpl cryptoService) throws KeyStoreException {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        String privateKeyAlias = "blue_gw";

        new Expectations() {{
            domainCertificateProviderFactory.createDomainCryptoService(domain);
            result = cryptoService;
        }};

        mdCryptoService.getPrivateKeyPassword(domain, privateKeyAlias);

        new Verifications() {{
            cryptoService.getPrivateKeyPassword(privateKeyAlias);
        }};
    }

    @Test
    public void refreshTrustStore(@Mocked DomainCryptoServiceImpl cryptoService) throws KeyStoreException {
        Domain domain = DomainService.DEFAULT_DOMAIN;

        new Expectations() {{
            domainCertificateProviderFactory.createDomainCryptoService(domain);
            result = cryptoService;
        }};

        mdCryptoService.refreshTrustStore(domain);

        new Verifications() {{
            cryptoService.refreshTrustStore();
        }};
    }

    @Test
    public void replaceTrustStore(@Mocked DomainCryptoServiceImpl cryptoService) {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        String storeFileName = "storefile.jks";
        byte[] store = "cert content".getBytes();
        String password = "test123";

        new Expectations() {{
            domainCertificateProviderFactory.createDomainCryptoService(domain);
            result = cryptoService;
            cryptoService.getTrustStoreType();
            result = "jks";
        }};

        mdCryptoService.replaceTrustStore(domain, storeFileName, store, password);

        new Verifications() {{
            cryptoService.replaceTrustStore(store, password);
        }};
    }

    @Test
    public void getKeyStore(@Mocked DomainCryptoServiceImpl cryptoService) {
        Domain domain = DomainService.DEFAULT_DOMAIN;

        new Expectations() {{
            domainCertificateProviderFactory.createDomainCryptoService(domain);
            result = cryptoService;
        }};

        mdCryptoService.getKeyStore(domain);

        new Verifications() {{
            cryptoService.getKeyStore();
        }};
    }

    @Test
    public void getTrustStore(@Mocked DomainCryptoServiceImpl cryptoService) {
        Domain domain = DomainService.DEFAULT_DOMAIN;

        new Expectations() {{
            domainCertificateProviderFactory.createDomainCryptoService(domain);
            result = cryptoService;
        }};

        mdCryptoService.getTrustStore(domain);

        new Verifications() {{
            cryptoService.getTrustStore();
        }};
    }

    @Test
    public void isCertificateChainValid(@Mocked DomainCryptoServiceImpl cryptoService) {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        String alias = "blue_gw";

        new Expectations() {{
            domainCertificateProviderFactory.createDomainCryptoService(domain);
            result = cryptoService;
        }};

        mdCryptoService.isCertificateChainValid(domain, alias);

        new Verifications() {{
            cryptoService.isCertificateChainValid(alias);
        }};
    }

    @Test
    public void getCertificateFromKeystore(@Mocked DomainCryptoServiceImpl cryptoService) throws KeyStoreException {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        String alias = "blue_gw";

        new Expectations() {{
            domainCertificateProviderFactory.createDomainCryptoService(domain);
            result = cryptoService;
        }};

        mdCryptoService.getCertificateFromKeystore(domain, alias);

        new Verifications() {{
            cryptoService.getCertificateFromKeyStore(alias);
        }};
    }

    @Test
    public void addCertificate(@Mocked DomainCryptoServiceImpl cryptoService, @Mocked X509Certificate certificate) throws KeyStoreException {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        String alias = "blue_gw";
        boolean overwrite = true;

        new Expectations() {{
            domainCertificateProviderFactory.createDomainCryptoService(domain);
            result = cryptoService;
        }};

        mdCryptoService.addCertificate(domain, certificate, alias, overwrite);

        new Verifications() {{
            cryptoService.addCertificate(certificate, alias, overwrite);
        }};
    }

    @Test
    public void addCertificates(@Mocked DomainCryptoServiceImpl cryptoService, @Mocked List<CertificateEntry> certificates) throws KeyStoreException {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        boolean overwrite = true;

        new Expectations() {{
            domainCertificateProviderFactory.createDomainCryptoService(domain);
            result = cryptoService;
        }};

        mdCryptoService.addCertificate(domain, certificates, overwrite);

        new Verifications() {{
            cryptoService.addCertificate(certificates, overwrite);
        }};
    }

    @Test
    public void getCertificateFromTruststore(@Mocked DomainCryptoServiceImpl cryptoService) throws KeyStoreException {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        String alias = "blue_gw";

        new Expectations() {{
            domainCertificateProviderFactory.createDomainCryptoService(domain);
            result = cryptoService;
        }};

        mdCryptoService.getCertificateFromTruststore(domain, alias);

        new Verifications() {{
            cryptoService.getCertificateFromTrustStore(alias);
        }};
    }

    @Test
    public void removeCertificate(@Mocked DomainCryptoServiceImpl cryptoService) throws KeyStoreException {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        String alias = "blue_gw";

        new Expectations() {{
            domainCertificateProviderFactory.createDomainCryptoService(domain);
            result = cryptoService;
        }};

        mdCryptoService.removeCertificate(domain, alias);

        new Verifications() {{
            cryptoService.removeCertificate(alias);
        }};
    }

    @Test
    public void removeCertificates(@Mocked DomainCryptoServiceImpl cryptoService) throws KeyStoreException {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        List<String> aliases = Arrays.asList("blue_gw", "red_gw");

        new Expectations() {{
            domainCertificateProviderFactory.createDomainCryptoService(domain);
            result = cryptoService;
        }};

        mdCryptoService.removeCertificate(domain, aliases);

        new Verifications() {{
            cryptoService.removeCertificate(aliases);
        }};
    }
}