package eu.domibus.core.ssl.offload;

import org.apache.cxf.transport.http.Address;

import java.net.URL;

/**
 * Service for SSL offloading operations.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
public interface SslOffloadService {

    /**
     * Value for the HTTP scheme (URIs) or protocol (URLs).
     */
    String PROTOCOL_HTTP = "http";

    /**
     * Value for the HTTPS scheme (URIs) or protocol (URLs).
     */
    String PROTOCOL_HTTPS = "https";

    /**
     * Offloads the provided address, returning a new address that will not initiate the SSL handshake.
     *
     * @param address the address for which to offload the SSL connection
     * @return a new address that will bypass the SSL handshake, allowing to offload the SSL connection to an external proxy.
     */
    Address offload(Address address);

    /**
     * Returns whether the {@code URL} should be prevented from creating the SSL connection or not.
     *
     * @param url the {@code URL} to check for SSL offload
     * @return {@code true}, if the {@code URL} should not start the SSL handshake itself; {@code false}, otherwise.
     */
    boolean isSslOffloadEnabled(URL url);
}
