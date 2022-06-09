package eu.domibus.core.security;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.UserSessionsService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class SessionInvalidationCommandTask implements CommandTask {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(SessionInvalidationCommandTask.class);

    private UserSessionsService userSessionsService;

    public SessionInvalidationCommandTask(UserSessionsService userSessionsService) {
        this.userSessionsService = userSessionsService;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.USER_SESSION_INVALIDATION, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("User session invalidation command");

        final String userName = properties.get(CommandProperty.USER_NAME);

        try {
            LOGGER.trace("Invalidating user session for use [{}]", userName);
            userSessionsService.invalidateSessions(userName);
        } catch (Exception ex) {
            LOGGER.error("Error trying to invalidate user sessions for user [{}]", userName, ex);
        }
    }
}
