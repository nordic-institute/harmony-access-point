package eu.domibus.plugin;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Interface implemented by external modules (backend connectors) to signal that they can be disabled
 */
public interface EnableAware {
    /**
     * @return The name of the plugin
     */
    String getName();

    /**
     * If the plugin is enabled
     * @param domainCode On which domain
     * @return The enabled state
     */
    boolean isEnabled(final String domainCode);

    /**
     * Telling the plugin to become enabled/disabled
     * The plugin will do some internal stuff and then notify the domibus to crete/delete the resources managed by domibus
     * @param domainCode On which domain
     * @param enabled the requested state
     */
    void setEnabled(final String domainCode, final boolean enabled);

    /**
     * The names of the cron jobs that the plugin uses so that domibus can pause/resume then on a plugin bases;
     * Not very beautiful; a better solution is searched
     * @return
     */
    List<String> getJobNames();
}
