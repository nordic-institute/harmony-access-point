package eu.domibus.web.security;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * Custom {@link CookieClearingLogoutHandler cookie clearing logout handler} that ensures cookies are removed
 * whether their cookie paths end with a trailing slash or not.
 *
 * @author Sebastian-Ion TINCU
 * @since 4.1.1
 */
public class DomibusCookieClearingLogoutHandler implements LogoutHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCookieClearingLogoutHandler.class);

    private final CookieClearingLogoutHandler defaultHandler;

    private final String[] cookiesToClear;

    public DomibusCookieClearingLogoutHandler(String... cookiesToClear) {
        if(cookiesToClear == null) {
            throw new IllegalArgumentException("List of cookies cannot be null");
        }

        this.defaultHandler = new CookieClearingLogoutHandler(cookiesToClear);
        this.cookiesToClear = cookiesToClear;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Reuse default cookie logic to remove cookies having their paths ending with a trailing slash (e.g. "sessionCookiePathUsesTrailingSlash" set to true on a Tomcat server)
        defaultHandler.logout(request, response, authentication);

        // Remove cookies not having their paths ending with a trailing slash
        final String cookiePath = request.getContextPath();
        Arrays.stream(cookiesToClear).forEach(cookieName -> removeCookie(cookieName, cookiePath, response));
    }

    private void removeCookie(String cookieName, String cookiePath, HttpServletResponse response) {
        LOG.debug("Removing cookie [{}] having path [{}]", cookieName, cookiePath);
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath(cookiePath);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
