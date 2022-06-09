package eu.domibus.core.crypto.spi.dss;

import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;

import java.security.KeyStore;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
public class DomibusDataLoader extends CommonsDataLoader {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusDataLoader.class);

    private KeyStore trustStore;

    public DomibusDataLoader(KeyStore trustStore) {
        this.trustStore = trustStore;
    }

    @Override
    protected KeyStore getSSLTrustStore() {
        return this.trustStore;
    }

   }
