package eu.domibus.api.pki;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.crypto.TrustStoreContentDTO;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.TrustStoreEntry;

import javax.naming.InvalidNameException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public interface KeystorePersistenceService {
    /**
     * Loads a truststore pointed by the file location and persists it in the DB (with the given name) if not already there. This happens at bootstrap time
     *
     * @param name the name of the truststore(can be domibus truststore and keystore and TLS trsustore)
     * @param optional permits the location to be null without raising any exception
     * @param filePathSupplier a supplier method that returns the file path on disc of the trust
     * @param typeSupplier a supplier method that returns the type of the trust
     * @param passwordSupplier a supplier method that returns the password of the trust
     */
    void persistStoreFromDB(final String name, boolean optional,
                             Supplier<Optional<String>> filePathSupplier, Supplier<String> typeSupplier, Supplier<String> passwordSupplier);

}
