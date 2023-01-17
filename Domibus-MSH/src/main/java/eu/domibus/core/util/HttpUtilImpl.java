package eu.domibus.core.util;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.HttpUtil;
import eu.domibus.core.proxy.DomibusProxyException;
import eu.domibus.core.proxy.DomibusProxyService;
import eu.domibus.core.proxy.ProxyUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private DomibusProxyService domibusProxyService;

    @Autowired
    private ProxyUtil proxyUtil;

    // we have a cyclic dependency: DomibusX509TrustManager->MultiDomainCryptoServiceImpl->DomainCryptoServiceFactory->CertificateServiceImpl->CRLServiceImpl->CRLUtil->HttpUtilImpl
    @Lazy
    @Autowired
    private DomibusX509TrustManager domibusX509TrustManager;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    public ByteArrayInputStream downloadURL(String url) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        RequestConfig.Builder requestConfig = RequestConfig.custom();
        SocketConfig.Builder socketConfig = SocketConfig.custom();
        int httpTimeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_CERTIFICATE_CRL_HTTP_TIMEOUT);
        if (httpTimeout > 0) {
            int httpTimeoutMilis = (int) (httpTimeout * DateUtils.MILLIS_PER_SECOND);
            LOG.debug("Configure the HTTP client with httpTimeout: [{}]", httpTimeout);
            requestConfig.setConnectTimeout(httpTimeoutMilis)
                    .setConnectionRequestTimeout(httpTimeoutMilis)
                    .setSocketTimeout(httpTimeoutMilis);
            socketConfig.setSoTimeout(httpTimeoutMilis);
        }

        HttpHost proxy = null;
        if (domibusProxyService.useProxy()) {
            LOG.debug("Configure HTTP client with proxy: [{}]", proxy);
            proxy = proxyUtil.getConfiguredProxy();
            requestConfig.setProxy(proxy);
        }
        RequestConfig config = requestConfig.build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(config);

        LOG.debug("Executing request {} via {}", httpGet.getRequestLine(), proxy == null ? "no proxy" : proxy);
        HttpClientBuilder httpClientBuilder = getHttpClientBuilder(url)
                .setDefaultSocketConfig(socketConfig.build())
                .setDefaultCredentialsProvider(proxyUtil.getConfiguredCredentialsProvider());
        try (CloseableHttpClient httpClient = httpClientBuilder.build()) {
            return getByteArrayInputStream(httpClient, httpGet);
        }
    }

    private ByteArrayInputStream getByteArrayInputStream(CloseableHttpClient httpclient, HttpGet httpGet) throws IOException {
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
                throw new DomibusProxyException("A proxy authentication error occurred when executing the request " + httpGet.getRequestLine());
            }
            if (statusCode >= 400 && statusCode < 600) {
                throw new DomibusCoreException("An HTTP client or server error occurred when executing the request " + httpGet.getRequestLine() + ": " + response.getStatusLine());
            }
            return new ByteArrayInputStream(IOUtils.toByteArray(response.getEntity().getContent()));
        }
    }

    protected HttpClientBuilder getHttpClientBuilder(String url) throws NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if (StringUtils.startsWithIgnoreCase(url, "https")) {
            LOG.debug("HTTPS client builder for [{}]", url);
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
            LOG.warn("Could not instantiate the SSL context", exc);
            throw exc;
        }
        return new SSLConnectionSocketFactory(
                sslContext,
                null,
                null,
                NoopHostnameVerifier.INSTANCE);
    }

}
