package eu.domibus.api.pki;

import java.security.KeyStore;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public interface KeystorePersistenceService {
    KeystorePersistenceInfo getTrustStorePersistenceInfo();

    KeystorePersistenceInfo getKeyStorePersistenceInfo();

    KeyStoreContentInfo loadStore(KeystorePersistenceInfo keystorePersistenceInfo);

    void saveStoreFromDBToDisk(KeystorePersistenceInfo keystorePersistenceInfo);

    void saveStore(KeyStoreContentInfo contentInfo, KeystorePersistenceInfo persistenceInfo);

    void saveStore(KeyStore store, KeystorePersistenceInfo persistenceInfo);
}