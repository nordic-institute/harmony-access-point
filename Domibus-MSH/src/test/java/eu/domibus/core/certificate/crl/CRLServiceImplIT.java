package eu.domibus.core.certificate.crl;

import eu.domibus.SpringTestConfiguration;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.util.ClassUtil;
import eu.domibus.core.cache.DomibusCacheConfiguration;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.pki.PKIUtil;
import eu.domibus.core.property.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
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
import java.security.Principal;
import java.security.Security;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

/**
 * Integration test for {@code CRLServiceImpl} class
 *
 * @author Catalin Enache
 * @since 4.1.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class CRLServiceImplIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CRLServiceImplIT.class);

    @Configuration
    @EnableCaching
    @Import({DomibusCacheConfiguration.class, SpringTestConfiguration.class})
    static class SpringConfig {

        @Bean
        public CRLService crlService() {
            return new CRLServiceImpl();
        }

        @Bean
        CRLUtil crlUtil() {
            return Mockito.mock(CRLUtil.class);
        }

        @Bean
        public GlobalPropertyMetadataManager domibusPropertyMetadataManager() {
            return Mockito.mock(GlobalPropertyMetadataManagerImpl.class);
        }

        @Bean
        public List<DomibusPropertyMetadataManagerSPI> propertyMetadataManagers() {
            return Arrays.asList(Mockito.mock(DomibusPropertyMetadataManagerSPI.class));
        }

        @Bean
        public DomainCoreConverter domainConverter() {
            return Mockito.mock(DomainCoreConverter.class);
        }

        @Bean
        public DomibusPropertyProviderDispatcher domibusPropertyProviderDispatcher() {
            return Mockito.mock(DomibusPropertyProviderDispatcher.class);
        }

        @Bean
        public ClassUtil classUtil() {
            return Mockito.mock(ClassUtil.class);
        }

        @Bean
        public DomibusPropertyChangeManager domibusPropertyChangeManager() {
            return Mockito.mock(DomibusPropertyChangeManager.class);
        }

        @Bean
        public PrimitivePropertyTypesManager primitivePropertyTypesManager() {
            return Mockito.mock(PrimitivePropertyTypesManager.class);
        }
    }

    @Autowired
    private CRLService crlService;

    @Autowired
    private CRLUtil crlUtil;

    private PKIUtil pkiUtil = new PKIUtil();

    private X509Certificate certificate;

    private String crlURLStr;

    @Before
    public void setUp() throws Exception {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        //create crl file
        final X509CRL x509CRL = pkiUtil.createCRL(Arrays.asList(new BigInteger("0400000000011E44A5E405", 16),
                new BigInteger("0400000000011E44A5E404", 16)));
        File crlFile = File.createTempFile("test_", ".crl");
        crlURLStr = crlFile.toURI().toURL().toString();
        FileUtils.writeByteArrayToFile(crlFile, x509CRL.getEncoded());

        //create the certificates
        BigInteger serial = new BigInteger("0400000000011E44A5E404", 16);
        certificate = pkiUtil.createCertificate(serial, Arrays.asList(crlURLStr));
        LOG.debug("Certificate created: {}", certificate);
    }

    @Test
    public void test_isCertificateRevoked_withCache() {

        X509CRL x509CRLMock = Mockito.mock(X509CRL.class);
        Principal principalMock = Mockito.mock((Principal.class));

        Mockito.when(crlUtil.downloadCRL(Mockito.any(String.class))).thenReturn(x509CRLMock);
        Mockito.when(x509CRLMock.getIssuerDN()).thenReturn(principalMock);
        Mockito.when(principalMock.getName()).thenReturn(Mockito.any(String.class));

        //first call
        boolean result = crlService.isCertificateRevoked(certificate, crlURLStr);

        //second call
        result = crlService.isCertificateRevoked(certificate, crlURLStr);

        // verify that the downloadCRL is called only once
        Mockito.verify(crlUtil, Mockito.times(1)).downloadCRL(Mockito.any(String.class));
    }

}