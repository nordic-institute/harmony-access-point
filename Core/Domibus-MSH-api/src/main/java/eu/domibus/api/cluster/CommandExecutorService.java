package eu.domibus.api.cluster;

import eu.domibus.api.multitenancy.Domain;

import java.util.Map;
/**
 * @author idragusa
 * @since 4.2
 */
public interface CommandExecutorService {

    /**
     * Execute commands by a given server (find commands, then execute and delete each command)
     *
     * @param serverName
     */
    void executeCommands(String serverName);

    void executeCommand(String command, Map<String, String> commandProperties);
}
