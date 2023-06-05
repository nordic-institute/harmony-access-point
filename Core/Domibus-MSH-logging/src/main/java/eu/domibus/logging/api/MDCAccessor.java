package eu.domibus.logging.api;

public interface MDCAccessor {
    void putMDC(String key, String val);

    void removeMDC(String key);

    String getMDC(String key);

    String getMDCKey(String key);

    void clearCustomKeys();

    void clearAll();
}
