package eu.domibus.security;

import eu.domibus.api.multitenancy.UserSessionsService;
import eu.domibus.api.user.UserBase;
import org.springframework.stereotype.Service;

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

    private UserSessionsService delegated;

    public void setDelegated(UserSessionsService delegated) {
        this.delegated = delegated;
    }

    @Override
    public void invalidateSessions(UserBase user) {
        delegated.invalidateSessions(user);
    }
}
