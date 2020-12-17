package eu.domibus.core.message;

import eu.domibus.api.multitenancy.Domain;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public interface UserMessagePriorityService {

    Integer getPriority(String service, String action);

    List<UserMessagePriorityConfiguration> getConfiguredRulesWithConcurrency(Domain domain);


}
