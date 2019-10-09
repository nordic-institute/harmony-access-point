package eu.domibus.pki;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class CRLServiceImpl2IT {

    @Configuration
    @EnableCaching
    static class ContextConfiguration {
    }

//    @Configuration
//    @EnableCaching
//    static class SpringConfig {
//
//        @Bean
//        public CRLService crlService() {
//            return new CRLServiceImpl();
//        }
//
//        @Bean
//        CRLUtil crlUtil() {
//            return new CRLUtil();
//        }
//
//        @Bean
//        HttpUtil httpUtil() {
//            return new HttpUtilImpl();
//        }
//
//        @Bean
//        DomibusProxyService domibusProxyService() {
//            return new DomibusProxyServiceImpl();
//        }
//
//        @Bean
//        DomibusPropertyProvider domibusPropertyProvider() {
//            return new DomibusPropertyProviderImpl();
//        }
//    }

    @Autowired
    private CRLService crlService;

    private PKIUtil pkiUtil = new PKIUtil();

    private X509Certificate certificate;

    private String crlURLStr;

    @Before
    public void setUp() throws Exception {
        //first create a certificate
        BigInteger serial = new BigInteger("0400000000011E44A5E404", 16);
        certificate = pkiUtil.createCertificate(serial, Arrays.asList("test.crl"));
        System.out.println(certificate);


        File file = File.createTempFile("test_", ".cer");
        crlURLStr = file.toURI().toURL().toString();
        FileUtils.writeByteArrayToFile(file, certificate.getEncoded());
    }


    @Test
    public void test_isCertificateRevoked_withCache() {
    }
}