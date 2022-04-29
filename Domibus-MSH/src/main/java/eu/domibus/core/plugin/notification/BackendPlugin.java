package eu.domibus.core.plugin.notification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static eu.domibus.core.plugin.notification.BackendPlugin.Name.*;

/**
 * A predefined backend plugin described by its available names and default priority.
 *
 * @author Soumya Chandran
 * @since 4.2
 */
public enum BackendPlugin {

    WS_PLUGIN(1, WS, WS_OLD),
    JMS_PLUGIN(2, JMS),
    FS_PLUGIN(3, FS);


    /**
     * The default priority of this plugin
     */
    private final int priority;

    /**
     * The names this plugin is known by
     */
    private final List<String> names;

    BackendPlugin(int priority, String... names) {
        this.priority = priority;
        this.names = Arrays.asList(names);
    }

    public int getPriority() {
        return priority;
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

    public static class Name {

        /**
         * The WS-plugin webservice plugin name.
         */
        public static final String WS = "backendWSPlugin";

        /**
         * The old WS-plugin webservice plugin name.
         */
        public static final String WS_OLD = "backendWebservice";

        /**
         * The JMS-plugin JMS plugin name.
         */
        public static final String JMS = "Jms";

        /**
         * The FS-plugin file system plugin name.
         */
        public static final String FS = "backendFSPlugin";

    }
}
