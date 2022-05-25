package eu.domibus.core.crypto.spi.dss;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

   }
