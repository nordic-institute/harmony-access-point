package eu.domibus.security;

import eu.domibus.api.multitenancy.UserSessionsService;
import eu.domibus.api.user.UserBase;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.security.UserSessionsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation for {@link UserSessionsService}
 *
 * @author Ion Perpegel
 * @since 4.2
 *
 * Delegate implementation for UserSessionsService(used for crossing the spring context boundary).
 * It is injected by spring instead of the actual UserSessionsService and delegates the impl to it
 */
@Service
public class UserSessionsServiceDelegate implements UserSessionsService {

    private UserSessionsServiceImpl userSessionsService;

    public void setUserSessionsService(UserSessionsServiceImpl userSessionsService) {
        this.userSessionsService = userSessionsService;
    }

    @Override
    public void invalidateSessions(UserBase user) {
        userSessionsService.invalidateSessions(user);
    }
}
