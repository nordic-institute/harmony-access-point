package eu.domibus.api.pki;

import java.util.Optional;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public interface KeystorePersistenceInfo {

    String getName();

    Optional<String> getFilePath();

    boolean isOptional();

    String getType();

    String getPassword();

}
