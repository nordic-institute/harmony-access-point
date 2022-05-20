package eu.domibus.api.multitenancy;

import eu.domibus.api.user.UserBase;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public interface UserSessionsService {
    void invalidateSessions(UserBase user);

    void invalidateSessions(String userName);
}
