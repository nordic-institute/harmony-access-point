package eu.domibus.plugin;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Interface implemented by external modules (backend connectors) to signal that they can be disabled
 */
public interface EnableAware {
    String getName();

    boolean isEnabled(final String domainCode);

    void setEnabled(final String domainCode, final boolean enabled);
}
