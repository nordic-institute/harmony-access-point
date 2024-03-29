package eu.domibus.core.util;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


/**
 * @author idragusa
 * @since 4.1.1
 * <p>
 * X509TrustManager with the custom truststore configured in Domibus in addition to the cacerts </p>
 */
@Service
public class DomibusX509TrustManager implements X509TrustManager {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusX509TrustManager.class);

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    protected  DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        X509TrustManager defaultTm;
        try {
            defaultTm = getX509TrustManager(true);
        } catch (NoSuchAlgorithmException | KeyStoreException exc) {
            LOG.warn("Could not load default certificates.");
            return null;
        }
        return defaultTm.getAcceptedIssuers();
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain,
                                   String authType) throws CertificateException {
        X509TrustManager domibusTm;
        LOG.debug("Performing validation of server trust against custom certificates.");
        try {
            domibusTm = getX509TrustManager(false);
        } catch (NoSuchAlgorithmException | KeyStoreException exc) {
            throw new CertificateException("Could not load custom certificates.", exc);
        }

        try {
            domibusTm.checkServerTrusted(chain, authType);
            LOG.debug("Server trusted check against custom certificates was successful");
            return;
        } catch (CertificateException e) {
            LOG.debug("Could not check the server trusted against custom certificates", e);
        }

        if (domibusPropertyProvider.getBooleanProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_CACERTS_VALIDATION_ENABLED)) {
            LOG.debug("Validation of server trust against default system certificates enabled");
            X509TrustManager defaultTm;
            try {
                defaultTm = getX509TrustManager(true);
            } catch (NoSuchAlgorithmException | KeyStoreException exc) {
                throw new CertificateException("Could not load default system certificates.", exc);
            }
            defaultTm.checkServerTrusted(chain, authType);
            LOG.debug("Server trusted check against default system certificates was successful");
            return;
        }

        throw new CertificateException("Could not perform the validation of the server trust");
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain,
                                   String authType) throws CertificateException {
        X509TrustManager defaultTm;
        try {
            defaultTm = getX509TrustManager(true);
        } catch (NoSuchAlgorithmException | KeyStoreException exc) {
            LOG.warn("Could not load default certificates.");
            throw new CertificateException("Could not load default certificates.", exc);
        }
        defaultTm.checkClientTrusted(chain, authType);
    }

    protected X509TrustManager getX509TrustManager(boolean isDefaultTrust) throws NoSuchAlgorithmException, KeyStoreException {
        KeyStore trustStore = null;
        X509TrustManager trustManager = null;
        if (!isDefaultTrust) {
            LOG.debug("Getting custom certificates [{}]", trustStore);
            trustStore = multiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());
        } else {
            LOG.debug("Getting default certificates");
        }

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
            throw exc;
        }

        return trustManager;
    }
}
