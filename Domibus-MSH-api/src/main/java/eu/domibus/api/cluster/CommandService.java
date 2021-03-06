package eu.domibus.api.cluster;

import eu.domibus.api.multitenancy.Domain;

import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
public interface CommandService {

    void createClusterCommand(String command, String server, Map<String, String> commandProperties);

    List<Command> findCommandsByServerName(String serverName);

    void deleteCommand(Long commandId);

}
