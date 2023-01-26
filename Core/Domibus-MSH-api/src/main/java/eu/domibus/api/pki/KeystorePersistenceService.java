package eu.domibus.api.pki;

import java.security.KeyStore;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public interface KeystorePersistenceService {
    KeystorePersistenceInfo getTrustStorePersistenceInfo();

    KeystorePersistenceInfo getKeyStorePersistenceInfo();

    KeyStoreInfo loadStoreContentFromDisk(KeystorePersistenceInfo keystorePersistenceInfo);

    void saveStoreFromDBToDisk(KeystorePersistenceInfo keystorePersistenceInfo);

    void saveToDisk(byte[] fileContent, String storeType, KeystorePersistenceInfo persistenceInfo);

    void saveToDisk(KeyStore trustStore, KeystorePersistenceInfo persistenceInfo);
}
