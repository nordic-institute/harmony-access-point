package eu.domibus.core.security;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.UserSessionsService;
import eu.domibus.api.user.UserBase;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.security.UserDetail;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation for {@link UserSessionsService}
 * It is located in core package(and not web) because it is referred from classes created in root application context
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class UserSessionsServiceImpl implements UserSessionsService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserSessionsServiceImpl.class);

    SessionRegistry sessionRegistry;

    SignalService signalService;

    public UserSessionsServiceImpl(SessionRegistry sessionRegistry, SignalService signalService) {
        this.sessionRegistry = sessionRegistry;
        this.signalService = signalService;
    }

    @Override
    public void invalidateSessions(UserBase user) {
        String userName = user.getUserName();

        doInvalidateSessions(userName);

        notifyClusterNodes(userName);
    }

    @Override
    public void invalidateSessions(String userName) {
        doInvalidateSessions(userName);
    }

    protected void doInvalidateSessions(String userName) {
        LOG.debug("Invalidate sessions called for user [{}]", userName);
        List<UserDetail> principals = sessionRegistry.getAllPrincipals().stream()
                .map(p -> ((UserDetail) p))
                .filter(u -> u.getUsername().equals(userName))
                .collect(Collectors.toList());
        principals.forEach(principal -> {
            LOG.info("Found principal [{}] in session registry", principal.getUsername());
            List<SessionInformation> sess = sessionRegistry.getAllSessions(principal, false);
            sess.forEach(session -> {
                LOG.info("Expire session [{}] for user [{}]", session, userName);
                session.expireNow();
            });
        });
    }

    protected void notifyClusterNodes(String userName) {
        //notify other nodes in the cluster
        LOG.trace("Broadcasting session invalidation event for user [{}]", userName);
        try {
            signalService.signalSessionInvalidation(userName);
        } catch (Exception ex) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Exception signaling session invalidation event for user " + userName, ex);
        }
    }
}
