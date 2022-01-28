package eu.domibus.api.pki;

import eu.domibus.api.crypto.CryptoException;
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
 * @author Cosmin Baciu
 * @since 3.2
 */
public interface CertificateService {

    boolean isCertificateValid(X509Certificate cert) throws DomibusCertificateException;

    boolean isCertificateChainValid(KeyStore trustStore, String alias);

    /**
     * Verifies every certificate in the chain if valid and not revoked
     *
     * @param certificateChain the chain of certificates
     * @return true if every certificate in the chain is valid and not revoked
     */
    boolean isCertificateChainValid(List<? extends Certificate> certificateChain);

    String extractCommonName(final X509Certificate certificate) throws InvalidNameException;

    /**
     * Save certificate data in the database, and use this data to display a revocation warning when needed.
     *
     * @param trustStore trustStore entries
     * @param keyStore   keyStore entries
     */
    void saveCertificateAndLogRevocation(final KeyStore trustStore, final KeyStore keyStore);

    /**
     * Check if alerts need to be send for expired or soon expired certificate. Send if true.
     */
    void sendCertificateAlerts();

    /**
     * Returns the certificate deserialized from a base64 string
     *
     * @param content the certificate serialized as a base64 string
     * @return a certificate
     * @throws CertificateException if the base64 string cannot be deserialized to a certificate
     */
    X509Certificate loadCertificateFromString(String content);

    /**
     * Returns the certificate entry from the trust store given a certificate and an alias
     *
     * @param cert  the certificate itself
     * @param alias the certificate alias
     * @return a certificate entry
     * @throws KeyStoreException if the trust store was not initialized
     */
    TrustStoreEntry createTrustStoreEntry(X509Certificate cert, String alias) throws KeyStoreException;

    /**
     * Given a list of certificates, returns a string containing the certificates in a 64 base encoded format and
     * separated in the Pem style.
     *
     * @param certificates the list of certificates.
     * @return the pem formatted string.
     */
    String serializeCertificateChainIntoPemFormat(List<? extends Certificate> certificates);

    /**
     * Given a pem formatted string containing a list of certificates, the method returns a list of X509 certificates.
     *
     * @param chain the pem formatted string.
     * @return the list of certificates.
     */
    List<X509Certificate> deserializeCertificateChainFromPemFormat(String chain);

    /**
     * Given a chain of signing certificates (Trust chain + leaf), extract the leaf one.
     *
     * @param certificates list containing the trust chain and the leaf.
     * @return the leaf certificate.
     */
    Certificate extractLeafCertificateFromChain(List<? extends Certificate> certificates);

    /**
     * Returns a certificate entry converted from a base64 string
     *
     * @param certificateContent the certificate serialized as a base64 string
     * @return a certificate entry
     * @throws CertificateException if the base64 string cannot be converted to a certificate entry
     */
    TrustStoreEntry convertCertificateContent(String certificateContent);

    /**
     * Replaces the truststore pointed by the name with the one provided by location/password parameters
     *
     * @param fileLocation the file location representing the trust
     * @param filePassword the password of the trust
     * @param trustName    the name of the trust in hte DB
     * @throws CryptoException
     */
    void replaceTrustStore(String fileLocation, String filePassword, String trustName);

    /**
     * Replaces the truststore pointed by the name with the one provided by content/password parameters
     *
     * @param fileName     the file name representing the trust
     * @param fileContent  the trust content
     * @param filePassword the password of the trust
     * @param trustName    the name of the trust in hte DB
     * @throws CryptoException
     */
    void replaceTrustStore(String fileName, byte[] fileContent, String filePassword, String trustName) throws CryptoException;

    /**
     * Replaces the truststore pointed by the name with the one provided by content/password parameters
     *
     * @param fileContent  the trust content
     * @param filePassword the password of the trust
     * @param trustName    the name of the trust in hte DB
     * @throws CryptoException
     */
    void replaceTrustStore(byte[] fileContent, String filePassword, String trustName) throws CryptoException;

    /**
     * Returns the truststore pointed by the location/password parameters
     *
     * @param trustName the location of the trust on disc
     * @return the truststore object
     */
    KeyStore getTrustStore(String trustName);

    /**
     * Returns the truststore pointed by the location/password parameters as a list of certificate entries
     *
     * @param trustName the name of the trust in DB
     * @return the list of cewrtificates and their names
     */
    List<TrustStoreEntry> getTrustStoreEntries(String trustName);

    TruststoreInfo getTruststoreInfo(String trustName);

    /**
     * Adds the specified certificate to the truststore pointed by the parameters
     *
     * @param trustName          the location of the trust on disc
     * @param certificateContent the content of the certificate
     * @param alias              the name of the certificate
     * @param overwrite          if overwrite an existing certificate
     * @return
     */
    boolean addCertificate(String trustName, byte[] certificateContent, String alias, boolean overwrite);

    /**
     * Adds the specified certificates to the truststore pointed by the parameters
     *
     * @param trustName    the location of the trust on disc
     * @param certificates the list of certificate entries( name and value)
     * @param overwrite    if overwrite an existing certificate
     * @return true if at least one was added
     */
    boolean addCertificates(String trustName, List<CertificateEntry> certificates, boolean overwrite);

    /**
     * Removes the specified certificate from the truststore pointed by the parameters
     *
     * @param trustName the location of the trust on disc
     * @param alias     the certificate name
     * @return true is at least one was deleted
     */
    boolean removeCertificate(String trustName, String alias);

    /**
     * Removes the specified certificates from the truststore pointed by the parameters
     *
     * @param trustName the location of the trust on disc
     * @param aliases   the list of certificate names
     * @return true is at least one was deleted
     */
    boolean removeCertificates(String trustName, List<String> aliases);

    /**
     * Retrieves the content of the specified truststore
     * @param trustName the name of the trust in the db
     * @return
     */
    byte[] getTruststoreContent(String trustName);

    /**
     * Loads a truststore pointed by the file location and persists it in the DB (with the given name) if not already there. This happens at bootstrap time
     *
     * @param name the name of the truststore(can be domibus truststore and keystore and TLS trsustore)
     * @param optional permits the location to be null without raising any exception
     * @param filePathSupplier a supplier method that returns the file path on disc of the trust
     * @param typeSupplier a supplier method that returns the type of the trust
     * @param passwordSupplier a supplier method that returns the password of the trust
     * @param domains in MT env it specifies for which domain to persist
     */
    void persistTruststoresIfApplicable(final String name, boolean optional,
                                        Supplier<Optional<String>> filePathSupplier, Supplier<String> typeSupplier, Supplier<String> passwordSupplier, List<Domain> domains);
}
