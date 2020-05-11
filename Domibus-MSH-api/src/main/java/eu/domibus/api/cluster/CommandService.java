package eu.domibus.api.cluster;

import eu.domibus.api.multitenancy.Domain;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
public interface CommandService {

    void executeCommand(String command, Domain domain, Map<String, String> commandProperties);

}
