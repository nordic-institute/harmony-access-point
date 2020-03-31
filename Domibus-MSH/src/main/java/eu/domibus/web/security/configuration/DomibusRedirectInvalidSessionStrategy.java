package eu.domibus.web.security.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import javax.servlet.ServletException;
import java.io.IOException;

public final class DomibusRedirectInvalidSessionStrategy implements SessionInformationExpiredStrategy {

    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
        event.getResponse().sendError(HttpStatus.UNAUTHORIZED.value(), "The session has expired, maybe due to inactivity or user being deleted or disabled.");
    }

}