package eu.domibus.core.util;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.HttpUtil;
import eu.domibus.core.proxy.DomibusProxyService;
import eu.domibus.core.proxy.ProxyUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_CERTIFICATE_CRL_HTTP_TIMEOUT;

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
    DomibusX509TrustManager domibusX509TrustManager;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    public final static int MILIS_TO_SECONDS = 1000;

    @Override
    public ByteArrayInputStream downloadURL(String url) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder httpClientBuilder = getHttpClientBuilder(url);
        CredentialsProvider credentialsProvider = proxyUtil.getConfiguredCredentialsProvider();
        if (credentialsProvider != null) {
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        try (CloseableHttpClient httpClient = httpClientBuilder.build()) {

            RequestConfig.Builder builder = RequestConfig.custom();
            int httpTimeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_CERTIFICATE_CRL_HTTP_TIMEOUT);
            if (httpTimeout > 0) {
                LOG.debug("Configure the http client with httpTimeout: [{}]", httpTimeout);
                builder.setConnectTimeout(httpTimeout * MILIS_TO_SECONDS)
                        .setConnectionRequestTimeout(httpTimeout * MILIS_TO_SECONDS)
                        .setSocketTimeout(httpTimeout * MILIS_TO_SECONDS);
            }

            HttpHost proxy = null;
            if (domibusProxyService.useProxy()) {
                LOG.debug("Configure http client with proxy: [{}]", proxy);
                proxy = proxyUtil.getConfiguredProxy();
                builder.setProxy(proxy);
            }

            RequestConfig config = builder.build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(config);

            LOG.debug("Executing request {} via {}", httpGet.getRequestLine(), proxy == null ? "no proxy" : proxy);
            return getByteArrayInputStream(httpClient, httpGet);
        }
    }

    private ByteArrayInputStream getByteArrayInputStream(CloseableHttpClient httpclient, HttpGet httpGet) throws IOException {
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            return new ByteArrayInputStream(IOUtils.toByteArray(response.getEntity().getContent()));
        }
    }

    protected HttpClientBuilder getHttpClientBuilder(String url) throws NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if (StringUtils.startsWith(url, "https")) {
            LOG.debug("Https client builder for [{}]", url);
            httpClientBuilder.setSSLSocketFactory(getSSLConnectionSocketFactory());
        }
        return httpClientBuilder;
    }

    protected SSLConnectionSocketFactory getSSLConnectionSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext;

        try {
            sslContext = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);
            sslContext.init(null, new TrustManager[]{domibusX509TrustManager}, null);
        } catch (NoSuchAlgorithmException | KeyManagementException exc) {
            LOG.warn("Could not instantiate sslContext", exc);
            throw exc;
        }
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext,
                null,
                null,
                NoopHostnameVerifier.INSTANCE);

        return sslsf;
    }

}