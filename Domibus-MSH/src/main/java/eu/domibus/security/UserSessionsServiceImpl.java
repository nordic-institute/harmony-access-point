package eu.domibus.security;

import eu.domibus.api.multitenancy.UserSessionsService;
import eu.domibus.api.user.UserBase;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.model.security.UserLoginErrorReason;
import eu.domibus.common.services.UserService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation for {@link UserSessionsService}
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class UserSessionsServiceImpl implements UserSessionsService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserSessionsServiceImpl.class);

    @Autowired
    SessionRegistry sessionRegistry;

    @Override
    public void invalidateSessions(UserBase user) {
        Optional<UserDetail> ud = sessionRegistry.getAllPrincipals().stream().map(p -> ((UserDetail) p)).filter(u -> u.getUsername().equals(user.getUserName())).findFirst();
        if (ud.isPresent()) {
            List<SessionInformation> sess = sessionRegistry.getAllSessions(ud.get(), false);
            sess.forEach(session -> {
                session.expireNow();
            });
        }
    }
}
