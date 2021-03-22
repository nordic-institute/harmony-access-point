package eu.domibus.web.security;

import eu.domibus.web.security.DomibusCookieClearingLogoutHandler;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.Authentication;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sebastian-Ion TINCU
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class DomibusCookieClearingLogoutHandlerTest {

    private DomibusCookieClearingLogoutHandler handler;

    @Injectable
    private HttpServletRequest request;

    @Injectable
    private HttpServletResponse response;

    @Injectable
    private Authentication authentication;

    @Test
    public void removesCookiesNotHavingTheirPathsEndingWithForwardSlashInAdditionToTheOnesEndingWithIt() {
        givenCookieClearingLogoutHandler("JSESSIONID", "XSRF-TOKEN");
        givenContextPath("");

        whenLoggingOut();

        thenCookiesHavingTheirPathsBothEndingAndNotEndingWithSlashAddedToResponseToBeRemoved();
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenCookiesToRemoveIsNull() {
        givenCookieClearingLogoutHandler((String[])null);
    }


    private void givenContextPath(String contextPath) {
        new Expectations() {{
           request.getContextPath(); result = "domibus";
        }};
    }

    private void givenCookieClearingLogoutHandler(String... cookiesToClear) {
        handler = new DomibusCookieClearingLogoutHandler(cookiesToClear);
    }

    private void whenLoggingOut() {
        handler.logout(request, response, authentication);
    }

    private void thenCookiesHavingTheirPathsBothEndingAndNotEndingWithSlashAddedToResponseToBeRemoved() {
        new Verifications() {{
            List<Cookie> cookies = new ArrayList<>();
            response.addCookie(withCapture(cookies));

            Assert.assertTrue("Should have removed the cookies having their paths ending with a forwards slash",
                    cookies.stream()
                            .filter(cookie -> StringUtils.endsWith(cookie.getPath(), "/"))
                            .allMatch(cookie -> StringUtils.equalsAny(cookie.getName(),"JSESSIONID", "XSRF-TOKEN")
                                    && StringUtils.equals(cookie.getPath(), "domibus/")));
            Assert.assertTrue("Should have also removed the cookies having their paths not ending with a forwards slash",
                    cookies.stream()
                            .filter(cookie -> !StringUtils.endsWith(cookie.getPath(), "/"))
                            .allMatch(cookie -> StringUtils.equalsAny(cookie.getName(),"JSESSIONID", "XSRF-TOKEN")
                                    && StringUtils.equals(cookie.getPath(), "domibus")));
        }};
    }
}