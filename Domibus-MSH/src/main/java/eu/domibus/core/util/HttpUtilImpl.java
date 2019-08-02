package eu.domibus.core.util;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.HttpUtil;
import eu.domibus.common.util.ProxyUtil;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.proxy.DomibusProxyService;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by Cosmin Baciu on 12-Jul-16.
 */
@Service
public class HttpUtilImpl implements HttpUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(HttpUtilImpl.class);

    public static final String DEFAULT_SSL_PROTOCOL = "TLSv1.2";

    @Autowired
    DomibusProxyService domibusProxyService;

    @Autowired
    ProxyUtil proxyUtil;

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    protected DomainContextProvider domainProvider;


    @Override
    public ByteArrayInputStream downloadURL(String url) throws IOException {
        if (domibusProxyService.useProxy()) {
            return downloadURLViaProxy(url);
        }
        return downloadURLDirect(url);
    }

    @Override
    public ByteArrayInputStream downloadURLDirect(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(getSSLConnectionSocketFactory())
                .build();
        HttpGet httpGet = new HttpGet(url);


        try {
            LOG.debug("Executing request " + httpGet.getRequestLine() + " directly");
            return getByteArrayInputStream(httpclient, httpGet);
        } finally {
            httpclient.close();
        }
    }

    @Override
    public ByteArrayInputStream downloadURLViaProxy(String url) throws IOException {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        CredentialsProvider credentialsProvider = proxyUtil.getConfiguredCredentialsProvider();
        if (credentialsProvider != null) {
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        try (CloseableHttpClient httpClient = httpClientBuilder.setSSLSocketFactory(getSSLConnectionSocketFactory()).build()) {
            HttpHost proxy = proxyUtil.getConfiguredProxy();

            RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(config);

            LOG.debug("Executing request " + httpGet.getRequestLine() + " via " + proxy);
            return getByteArrayInputStream(httpClient, httpGet);
        }
    }

    private ByteArrayInputStream getByteArrayInputStream(CloseableHttpClient httpclient, HttpGet httpGet) throws IOException {
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            return new ByteArrayInputStream(IOUtils.toByteArray(response.getEntity().getContent()));
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    protected SSLConnectionSocketFactory getSSLConnectionSocketFactory() {
        SSLContext sslContext = null;
        final X509TrustManager defaultTm = getX509TrustManager(true);
        final X509TrustManager domibusTm = getX509TrustManager(false);

        X509TrustManager customTm = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return defaultTm.getAcceptedIssuers();
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
                try {
                    domibusTm.checkServerTrusted(chain, authType);
                } catch (CertificateException e) {
                    // This will throw another CertificateException if this fails too.
                    defaultTm.checkServerTrusted(chain, authType);
                }
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
                defaultTm.checkClientTrusted(chain, authType);
            }
        };
        try {
            sslContext = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);
            sslContext.init(null, new TrustManager[]{customTm}, null);
        } catch (NoSuchAlgorithmException | KeyManagementException exc) {
            LOG.warn("Could not instanciate sslContext", exc);
            return null;
        }
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext,
                null,
                null,
                NoopHostnameVerifier.INSTANCE);

        return sslsf;
    }

    protected X509TrustManager getX509TrustManager(boolean getDefault) {
        KeyStore trustStore = null;
        if (getDefault) {
            LOG.debug("Get default certificates (cacerts)");
        }
        if (!getDefault) {
            LOG.debug("Get custom certificates (truststore)");
            trustStore = multiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());
        }
        X509TrustManager trustManager = null;
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            // Using null here initialises the TMF with the default trust store (cacerts).
            tmf.init(trustStore);

            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    trustManager = (X509TrustManager) tm;
                    break;
                }
            }
        } catch (NoSuchAlgorithmException | KeyStoreException exc) {
            LOG.warn("Could not load trustManager for ssl exchange. ", exc);
            return null;
        }

        return trustManager;
    }
}