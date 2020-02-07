package eu.domibus.ebms3.receiver;

public enum BackEndPluginEnum {

    WS_PLUGIN("backendWebservice", 0),
    JMS_PLUGIN("Jms", 1),
    FS_PLUGIN("backendFSPlugin", 2);

    private final int priority;
    private final String pluginName;

    BackEndPluginEnum(String pluginName, int priority) {
        this.pluginName = pluginName;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public String getPluginName() {
        return pluginName;
    }

    public static BackEndPluginEnum findEnumByPluginName(final String pluginName) {
        for (final BackEndPluginEnum backEndPlugin : BackEndPluginEnum.values()) {
            if (pluginName.equals(backEndPlugin.getPluginName())) {
                return backEndPlugin;
            }
        }

        throw new IllegalArgumentException("No Default BackEnd Plugin found for pluginName: " + pluginName);
    }
}
