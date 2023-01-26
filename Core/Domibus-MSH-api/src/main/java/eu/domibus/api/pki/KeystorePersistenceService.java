package eu.domibus.api.pki;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public interface KeystorePersistenceService {
    void saveStoreFromDBToDisk(KeystorePersistenceInfo keystorePersistenceInfo);
}
