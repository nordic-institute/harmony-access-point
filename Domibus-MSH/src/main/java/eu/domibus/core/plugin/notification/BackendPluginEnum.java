package eu.domibus.core.plugin.notification;

/**
 * Stores the plugin Names and its priority
 *
 * @author Soumya Chandran
 * @since 4.2
 */
public enum BackendPluginEnum {

    WS_PLUGIN("backendWebservice", 0),
    JMS_PLUGIN("Jms", 1),
    FS_PLUGIN("backendFSPlugin", 2);

    private final int priority;
    private final String pluginName;

    BackendPluginEnum(String pluginName, int priority) {
        this.pluginName = pluginName;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public String getPluginName() {
        return pluginName;
    }

    /**
     * Find the respective BackendPluginEnum based on the plugin name
     *
     * @param pluginName
     * @return BackEndPluginEnum
     */
    public static BackendPluginEnum getBackendPluginEnum(final String pluginName) {
        for (final BackendPluginEnum backEndPlugin : BackendPluginEnum.values()) {
            if (pluginName.equals(backEndPlugin.getPluginName())) {
                return backEndPlugin;
            }
        }
        throw new IllegalArgumentException("No Default BackEnd Plugin found for pluginName: " + pluginName);
    }
}
