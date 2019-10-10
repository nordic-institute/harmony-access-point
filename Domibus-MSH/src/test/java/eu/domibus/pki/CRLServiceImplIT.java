package eu.domibus.pki;

import eu.domibus.SpringTestConfiguration;
import eu.domibus.core.cache.DomibusCacheConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.math.BigInteger;
import java.security.Security;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * Integration test for {@code CRLServiceImpl} class
 *
 * @author Catalin Enache
 * @since 4.1.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
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
            return new CRLUtil();
        }
    }

    @Autowired
    private CRLService crlService;

    private PKIUtil pkiUtil = new PKIUtil();

    private X509Certificate certificate;

    private String crlURLStr;

    private File crlFile;

    @Before
    public void setUp() throws Exception {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        //create crl file
        final X509CRL x509CRL = pkiUtil.createCRL(Arrays.asList(new BigInteger("0400000000011E44A5E405", 16), new BigInteger("0400000000011E44A5E404", 16)));
        crlFile = File.createTempFile("test_", ".crl");
        crlURLStr = crlFile.toURI().toURL().toString();
        FileUtils.writeByteArrayToFile(crlFile, x509CRL.getEncoded());

        //create the certificates
        BigInteger serial = new BigInteger("0400000000011E44A5E404", 16);
        certificate = pkiUtil.createCertificate(serial, Arrays.asList(crlURLStr));
        LOG.debug("Certificate created: {}", certificate);
    }


    @Test
    public void test_isCertificateRevoked_withCache() {

        long startTime = System.currentTimeMillis();

        //first call
        boolean result = crlService.isCertificateRevoked(certificate, crlURLStr);
        long endTime1 = System.currentTimeMillis();
        LOG.debug("isCertificateRevoked execution time in millis: [{}]", endTime1 - startTime);
        assertTrue(result);

        //second call
        result = crlService.isCertificateRevoked(certificate, crlURLStr);
        long endTime2 = System.currentTimeMillis();
        LOG.debug("isCertificateRevoked execution time in millis: [{}]", (endTime2 - endTime1));
        assertTrue(result);

        Assert.assertTrue("second execution time should be at least 4 times faster",
                (endTime1 - startTime) / 4 > (endTime2 - endTime1));
    }

    @After
    public void tearDown() throws Exception {
        if (crlFile != null) {
            crlFile.delete();
        }
    }
}