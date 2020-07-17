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
}
