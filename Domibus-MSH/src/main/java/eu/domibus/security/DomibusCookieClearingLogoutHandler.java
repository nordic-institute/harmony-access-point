package eu.domibus.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.util.Assert;

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

    private final CookieClearingLogoutHandler defaultHandler;

    private final String[] cookiesToClear;

    public DomibusCookieClearingLogoutHandler(String... cookiesToClear) {
        this.defaultHandler = new CookieClearingLogoutHandler(cookiesToClear);

        Assert.notNull(cookiesToClear, "List of cookies cannot be null");
        this.cookiesToClear = cookiesToClear;
    }


    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Reuse default cookie logic to remove cookies having their paths ending with a trailing slash (e.g. "sessionCookiePathUsesTrailingSlash" set to true on a Tomcat server)
        defaultHandler.logout(request, response, authentication);

        // Remove cookies not having their paths ending with a trailing slash
        Arrays.stream(cookiesToClear).forEach(cookieName -> {
            Cookie cookie = new Cookie(cookieName, null);
            String cookiePath = request.getContextPath();
            cookie.setPath(cookiePath);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        });

    }
}
