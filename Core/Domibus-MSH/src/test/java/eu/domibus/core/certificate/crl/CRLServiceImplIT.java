package eu.domibus.core.certificate.crl;

import eu.domibus.SpringTestConfiguration;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.util.ClassUtil;
import eu.domibus.core.cache.DomibusCacheConfiguration;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.pki.PKIUtil;
import eu.domibus.core.property.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.mockito.Mockito.*;

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
            return mock(CRLUtil.class);
        }

        @Bean
        public GlobalPropertyMetadataManager domibusPropertyMetadataManager() {
            return mock(GlobalPropertyMetadataManagerImpl.class);
        }

        @Bean
        public List<DomibusPropertyMetadataManagerSPI> propertyMetadataManagers() {
            return Arrays.asList(mock(DomibusPropertyMetadataManagerSPI.class));
        }

        @Bean
        public PropertyProviderDispatcher domibusPropertyProviderDispatcher() {
            return mock(PropertyProviderDispatcher.class);
        }

        @Bean
        public ClassUtil classUtil() {
            return mock(ClassUtil.class);
        }

        @Bean
        public PropertyChangeManager domibusPropertyChangeManager() {
            return mock(PropertyChangeManager.class);
        }

        @Bean
        public PrimitivePropertyTypesManager primitivePropertyTypesManager() {
            return mock(PrimitivePropertyTypesManager.class);
        }

        @Bean
        public NestedPropertiesManager domibusNestedPropertiesManager() {
            return mock(NestedPropertiesManager.class);
        }

        @Bean
        public PropertyProviderHelper domibusPropertyProviderHelper() {
            return mock(PropertyProviderHelper.class);
        }

        @Bean
        public DomibusRawPropertyProvider domibusRawPropertyProvider() {
            return mock(DomibusRawPropertyProvider.class);
        }

        @Bean
        public CertificateHelper certificateHelper(){
            return mock(CertificateHelper.class);
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
    @Ignore("The Spel condition that enables the cache doesn't see property value set for the tests in domibus.properties (EDELIVERY-10977)")
    public void test_isCertificateRevoked_withCache() {

        X509CRL x509CRLMock = mock(X509CRL.class);
        Principal principalMock = mock((Principal.class));

        when(crlUtil.downloadCRL(any(String.class), eq(false))).thenReturn(x509CRLMock);
        when(x509CRLMock.getIssuerDN()).thenReturn(principalMock);
        when(principalMock.getName()).thenReturn(any(String.class));

        //first call
        boolean result = crlService.isCertificateRevoked(certificate);

        //second call
        result = crlService.isCertificateRevoked(certificate);

        // verify that the getCrlDistributionPoints is called only once
        verify(crlUtil, times(1)).getCrlDistributionPoints(any(X509Certificate.class));
    }

}
