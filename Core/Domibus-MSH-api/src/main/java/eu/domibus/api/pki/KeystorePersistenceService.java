package eu.domibus.api.pki;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public interface KeystorePersistenceService {
    KeystorePersistenceInfo getTrustStorePersistenceInfo();

    KeystorePersistenceInfo getKeyStorePersistenceInfo();

    KeyStoreInfo loadStoreContentFromDisk(KeystorePersistenceInfo keystorePersistenceInfo);

    void saveStoreFromDBToDisk(KeystorePersistenceInfo keystorePersistenceInfo);
}
