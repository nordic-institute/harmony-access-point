package eu.domibus.pki;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.api.util.HttpUtil;
import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.common.util.ProxyUtil;
import eu.domibus.configuration.DefaultDomibusConfigurationService;
import eu.domibus.core.cache.DomibusCacheConfiguration;
import eu.domibus.core.crypto.DomainCryptoServiceFactoryImpl;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
import eu.domibus.core.crypto.api.DomainCryptoServiceFactory;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.multitenancy.DomainContextProviderImpl;
import eu.domibus.core.multitenancy.DomainServiceImpl;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.core.multitenancy.dao.DomainDaoImpl;
import eu.domibus.core.property.DomibusPropertyProviderImpl;
import eu.domibus.core.property.PropertyResolver;
import eu.domibus.core.property.encryption.PasswordEncryptionContextFactory;
import eu.domibus.core.util.HttpUtilImpl;
import eu.domibus.proxy.DomibusProxyService;
import eu.domibus.proxy.DomibusProxyServiceImpl;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Stopwatch;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.math.BigInteger;
import java.security.Security;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class CRLServiceImplIT {

    @Rule
    public Stopwatch stopwatch = new Stopwatch() {
        @Override
        public long runtime(TimeUnit unit) {
            return super.runtime(unit);
        }
    };

    @Configuration
    @EnableCaching
//    @ComponentScan( basePackages = {"eu.domibus"})
//    @TestPropertySource("classpath:config/domibus.properties")
    @Import(DomibusCacheConfiguration.class)
    static class SpringConfig {

        @Bean
        public CRLService crlService() {
            return new CRLServiceImpl();
        }

        @Bean
        CRLUtil crlUtil() {
            return new CRLUtil();
        }

        @Bean
        HttpUtil httpUtil() {
            return Mockito.mock(HttpUtilImpl.class);
        }

        @Bean
        ProxyUtil proxyUtil() {
            return Mockito.mock(ProxyUtil.class);
        }

        @Bean
        DomibusProxyService domibusProxyService() {
            return Mockito.mock(DomibusProxyServiceImpl.class);
        }

        @Bean
        DomibusX509TrustManager domibusX509TrustManager() {
            return Mockito.mock(DomibusX509TrustManager.class);
        }

        @Bean
        MultiDomainCryptoService multiDomainCryptoService() {
            return Mockito.mock(MultiDomainCryptoServiceImpl.class);
        }

        @Bean
        DomainCryptoServiceFactory domainCertificateProviderFactory() {
            return Mockito.mock(DomainCryptoServiceFactoryImpl.class);
        }

        @Bean
        DomibusCacheService domibusCacheService() {
            return Mockito.mock(DomibusCacheService.class);

        }

        @Bean
        DomibusPropertyProvider domibusPropertyProvider() {
            return new DomibusPropertyProviderImpl();
        }

        @Bean
        public DomainService domainService() {
            return Mockito.mock(DomainServiceImpl.class);
        }

        @Bean
        public DomainDao domainDao() {
            return Mockito.mock(DomainDaoImpl.class);
        }

        @Bean
        public DomibusConfigurationService domibusConfigurationService() {
            return Mockito.mock(DefaultDomibusConfigurationService.class);
        }

        @Bean
        public PropertyResolver propertyResolver() {
            return Mockito.mock(PropertyResolver.class);
        }

        @Bean(name = "domibusDefaultProperties")
        public Properties domibusDefaultProperties() {
            return Mockito.mock(Properties.class);
        }

        @Bean(name = "domibusProperties")
        public Properties domibusProperties() {
            return Mockito.mock(Properties.class);
        }

        @Bean
        public DomainContextProvider domainContextProvider() {
            return Mockito.mock(DomainContextProviderImpl.class);
        }

        @Bean
        public PasswordEncryptionService passwordEncryptionService() {
            return Mockito.mock(PasswordEncryptionService.class);
        }

        @Bean
        public PasswordEncryptionContextFactory passwordEncryptionContextFactory() {
            return Mockito.mock(PasswordEncryptionContextFactory.class);
        }
    }

    @Autowired
    private CRLService crlService;

    private PKIUtil pkiUtil = new PKIUtil();

    private X509Certificate certificate;

    private String crlURLStr;

    @Before
    public void setUp() throws Exception {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());





//        File file = File.createTempFile("test_", ".cer");
//        crlURLStr = file.toURI().toURL().toString();
//        FileUtils.writeByteArrayToFile(file, certificate.getEncoded());


        //create crl file
        final X509CRL x509CRL = pkiUtil.createCRL(Arrays.asList(new BigInteger("0400000000011E44A5E405", 16), new BigInteger("0400000000011E44A5E404", 16)));
        File crlFile = File.createTempFile("test_", ".crl");
        crlURLStr = crlFile.toURI().toURL().toString();
        FileUtils.writeByteArrayToFile(crlFile, x509CRL.getEncoded());

        //create a certificate
        BigInteger serial = new BigInteger("0400000000011E44A5E404", 16);
        certificate = pkiUtil.createCertificate(serial, Arrays.asList(crlURLStr));
        System.out.println(certificate);
    }


    @Test
    public void test_isCertificateRevoked_withCache() {


        long delta = 30;

        //first call
        boolean result = crlService.isCertificateRevoked(certificate, crlURLStr);
        assertTrue(result);
        //assertEquals(300d, stopwatch.runtime(TimeUnit.MILLISECONDS), delta);

        //second call

        result = crlService.isCertificateRevoked(certificate, crlURLStr);
        //assertEquals(300d, stopwatch.runtime(TimeUnit.MILLISECONDS), delta);
    }
}