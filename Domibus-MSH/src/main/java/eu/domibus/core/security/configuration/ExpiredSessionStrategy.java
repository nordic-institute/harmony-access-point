package eu.domibus.core.security.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom invalid session management strategy that sends a custom error message and 401 status
 * Unfortunately the spring default strategy sends 200 status
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Component
public final class ExpiredSessionStrategy
        implements SessionInformationExpiredStrategy {

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
        event.getResponse().sendError(HttpStatus.UNAUTHORIZED.value(),
                "The session has expired, maybe due to inactivity or the current user being deleted or disabled.");
    }

}