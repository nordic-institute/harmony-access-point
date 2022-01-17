package eu.domibus.core.util;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.proxy.DomibusProxyService;
import eu.domibus.core.proxy.ProxyUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;

/**
 * @author idragusa
 * @since 4.1
 */
@RunWith(JMockit.class)
public class HttpUtilImplTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(HttpUtilImplTest.class);


    private static final String RESOURCE_PATH = "src/main/conf/domibus/keystore/";
    private static final String TEST_TRUSTSTORE = "gateway_truststore.jks";
    private static final String TEST_TRUSTSTORE_PASSWD = "test123";

    @Tested
    HttpUtilImpl httpUtil;

    @Injectable
    ProxyUtil proxyUtil;

    @Injectable
    DomibusProxyService domibusProxyService;

    @Injectable
    MultiDomainCryptoService multiDomainCertificateProvider;

    @Injectable
    DomainContextProvider domainProvider;

    @Injectable
    DomibusX509TrustManager domibusX509TrustManager;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Test
    @Ignore
    public void testDownloadCRLViaProxy() throws Exception {
        new NonStrictExpectations(proxyUtil) {{
            domibusProxyService.useProxy();
            result = true;

            proxyUtil.getConfiguredProxy();
            result = new HttpHost("158.169.9.13", 8012);

            proxyUtil.getConfiguredCredentialsProvider();
            result = getTestCredentialsProvider();

        }};
        String url = "http://onsitecrl.verisign.com/offlineca/NATIONALITANDTELECOMAGENCYPEPPOLRootCA.crl";
        ByteArrayInputStream inputStream = httpUtil.downloadURL(url);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509CRL x509CRL = (X509CRL) cf.generateCRL(inputStream);
        System.out.println(x509CRL);

        inputStream = httpUtil.downloadURL(url);
        System.out.println(inputStream);
        x509CRL = (X509CRL) cf.generateCRL(inputStream);
        System.out.println(x509CRL);

    }

    protected CredentialsProvider getTestCredentialsProvider() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope("158.169.9.13", 8012),
                new UsernamePasswordCredentials("baciuco", "pass"));

        return credsProvider;
    }

    @Test
    @Ignore("EDELIVERY-8825 testDownloadCRLHttps: fix local run")
    public void testDownloadCRLHttps() throws Exception {
        String url = "http://onsitecrl.verisign.com/offlineca/NATIONALITANDTELECOMAGENCYPEPPOLRootCA.crl";
        /* Added this crl file to git so we have it on https and use it for testing */
        String urlSSL = "https://ec.europa.eu/cefdigital/code/projects/EDELIVERY/repos/domibus/browse/Domibus-MSH-test/src/main/resources/crls/NATIONALITANDTELECOMAGENCYPEPPOLRootCA.crl?at=3c7eae7c00e7425593436a1f469ba269834ece90&raw";

        /* Local SoapUI mock endpoints for http and https, EDELIVERY-4830 */
        //String url = "https://localhost:8555/crltest";
        //String urlSSL = "http://localhost:8089/crltest";

        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (FileInputStream instream = new FileInputStream(RESOURCE_PATH + TEST_TRUSTSTORE)) {
            trustStore.load(instream, TEST_TRUSTSTORE_PASSWD.toCharArray());
        }

        new NonStrictExpectations(proxyUtil) {{
            domibusProxyService.useProxy();
            result = false;

            domainProvider.getCurrentDomain();
            result = DomainService.DEFAULT_DOMAIN;

            multiDomainCertificateProvider.getTrustStore(DomainService.DEFAULT_DOMAIN);
            result = trustStore;
        }};

        ByteArrayInputStream inputStream = httpUtil.downloadURL(url);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509CRL x509CRL = (X509CRL) cf.generateCRL(inputStream);
        LOG.info(x509CRL.toString());

        ByteArrayInputStream inputStreamSSL = httpUtil.downloadURL(urlSSL);
        CertificateFactory cfSSL = CertificateFactory.getInstance("X.509");
        X509CRL x509CRLSSL = (X509CRL) cfSSL.generateCRL(inputStreamSSL);
        LOG.info(x509CRLSSL.toString());

        Assert.assertEquals(x509CRLSSL, x509CRL);
    }
}