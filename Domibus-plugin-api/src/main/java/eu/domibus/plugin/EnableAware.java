package eu.domibus.plugin;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Interface implemented by external modules that want to react to adding and removing of domains at runtime
 */
public interface EnableAware {
    String getName();

    boolean isEnabled(final String domainCode);

    void setEnabled(final String domainCode, final boolean enabled);
}
