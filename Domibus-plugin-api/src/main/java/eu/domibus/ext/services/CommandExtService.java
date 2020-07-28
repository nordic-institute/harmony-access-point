package eu.domibus.ext.services;

import java.util.Map;

/**
 * Service used to manage commands execution in a cluster
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public interface CommandExtService {

    /**
     * Signals the execution of a command in a cluster.
     *
     * @param properties The command properties
     */
    void executeCommand(String commandName, Map<String, Object> properties);
}
