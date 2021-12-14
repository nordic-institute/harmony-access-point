package eu.domibus.core.crypto.spi.dss;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
public class DomibusDataLoader extends CommonsDataLoader {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusDataLoader.class);

    private KeyStore trustStore;

    public DomibusDataLoader(KeyStore trustStore) {
        this.trustStore = trustStore;
    }

    @Override
    protected KeyStore getSSLTrustStore() {
        return this.trustStore;
    }

    @Override
    protected byte[] httpGet(final String url) throws DSSException {

        HttpGet httpRequest = null;
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient client = null;

        try {
            httpRequest = getHttpRequest(url);
            client = getHttpClient(url);
            httpResponse = getHttpResponse(client, httpRequest);

            return readHttpResponse(httpResponse);

        } catch (URISyntaxException | IOException e) {
            throw new DSSExternalResourceException(String.format("Unable to process GET call for url [%s]. Reason : [%s]", url, DSSUtils.getExceptionMessage(e)), e);

        } finally {
            closeQuietly(httpRequest, httpResponse, client);

        }
    }

   }
