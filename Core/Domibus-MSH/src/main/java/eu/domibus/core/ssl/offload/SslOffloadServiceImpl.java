package eu.domibus.core.ssl.offload;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.cxf.transport.http.Address;
import org.springframework.stereotype.Service;

import java.net.URL;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_CONNECTION_CXF_SSL_OFFLOAD_ENABLE;

/**
 * {@inheritDoc}
 */
@Service
public class SslOffloadServiceImpl implements SslOffloadService {

    /**
     * Value for the HTTP protocol in case of {@code URL}s (scheme in case of {@code URI}s).
     */
    public static final String PROTOCOL_HTTP = "http";

    /**
     * Value for the HTTPS protocol in case of {@code URL}s (scheme in case of {@code URI}s).
     */
    public static final String PROTOCOL_HTTPS = "https";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SslOffloadServiceImpl.class);

    private final DomibusPropertyProvider domibusPropertyProvider;

    public SslOffloadServiceImpl(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Address offload(Address address) {
        Address transformed = address;
        String result = switchStringRepresentationToHttp(address.getString());
        try {
            transformed = new Address(result);
            revertProtocol(transformed.getURL());
        } catch (Exception e) {
            LOG.error("An error occurred when switching the connection to HTTP for SSL offloading", e);
        }
        return transformed;
    }

    private void revertProtocol(URL url) {
        LOG.debug("Revert the protocol part of the HTTP URL back to HTTPS");
        try {
            FieldUtils.writeField(url, "protocol", PROTOCOL_HTTPS, true);
        } catch (IllegalAccessException e) {
            LOG.error("An error occurred when reverting the protocol part of the HTTP URL back to HTTPS", e);
        }
    }

    private String switchStringRepresentationToHttp(String spec) {
        LOG.debug("Switch [{}] to an HTTP connection for SSL offloading", spec);
        return StringUtils.replaceOnce(spec, PROTOCOL_HTTPS + ":", PROTOCOL_HTTP + ":");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSslOffloadEnabled(URL url) {
        if (url == null) {
            LOG.debug("URL is null so no need to switch the connection to HTTP for SSL offloading");
            return false;
        }

        if (!StringUtils.equalsIgnoreCase(url.getProtocol(), PROTOCOL_HTTPS)) {
            LOG.debug("Protocol for [{}] is not secure so no need to switch the connection to HTTP for SSL offloading", url);
            return false;
        }

        Boolean sslOffload = domibusPropertyProvider.getBooleanProperty(DOMIBUS_CONNECTION_CXF_SSL_OFFLOAD_ENABLE);
        if(BooleanUtils.isFalse(sslOffload)) {
            LOG.debug("SSL offload is not set so no need to switch the connection to HTTP for SSL offloading", url);
            return false;
        }

        LOG.debug("Switching the connection to HTTP for SSL offloading for [{}] is enabled", url);
        return true;
    }
}
