package eu.domibus.api.pki;

import java.util.Optional;

/**
 * @author Ion Perpegel
 * @since 5.1
 */

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
public interface KeystorePersistenceInfo {

    String getName();

    String getFileLocation();

    boolean isOptional();

    String getType();

    String getPassword();

}
