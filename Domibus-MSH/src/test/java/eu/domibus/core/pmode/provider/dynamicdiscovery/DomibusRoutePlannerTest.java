package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.core.ssl.offload.SslOffloadService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.net.URL;

/**
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
@RunWith(JMockit.class)
public class DomibusRoutePlannerTest {

    @Mocked
    private HttpRoute route;

    @Injectable
    private HttpHost host;

    @Injectable
    private HttpRequest request;

    @Injectable
    private HttpContext context;

    @Injectable
    private SslOffloadService sslOffloadService;

    @Tested
    private DomibusRoutePlanner domibusRoutePlanner;

    @Before
    public void stubSuperCallToDetermineRoute() {
        new MockUp<DefaultRoutePlanner>() {
            @Mock public HttpRoute determineRoute(final HttpHost host, final HttpRequest request, final HttpContext context) throws HttpException {
                return route;
            }
        };
    }

    @Test
    public void testDetermineRoute_sslOffloadDisabled() throws Exception {
        // GIVEN
        final String unsecuredTargetUri = "http://ec.europa.eu";
        new Expectations() {{
            host.toURI(); result = unsecuredTargetUri;
            sslOffloadService.isSslOffloadEnabled(new URL(unsecuredTargetUri)); result = Boolean.FALSE;
        }};

        // WHEN
        HttpRoute result = domibusRoutePlanner.determineRoute(host, request, context);

        // THEN
        Assert.assertSame("Should have not replaced the initial route when SSL offloading is disabled", route, result);
    }

    @Test
    public void testDetermineRoute_sslOffloadDisabledForMalformedUrl() throws Exception {
        // GIVEN
        new Expectations() {{
            host.toURI(); result = null;
            sslOffloadService.isSslOffloadEnabled(null); result = Boolean.FALSE;
        }};

        // WHEN
        HttpRoute result = domibusRoutePlanner.determineRoute(host, request, context);

        // THEN
        Assert.assertSame("Should have not replaced the initial route when SSL offloading is disabled because the target URI is malformed", route, result);
    }


    @Test
    public void testDetermineRoute_sslOffloadEnabled() throws Exception {
        // GIVEN
        final String unsecuredTargetUri = "https://ec.europa.eu";
        new Expectations() {{
            host.toURI(); result = unsecuredTargetUri;
            sslOffloadService.isSslOffloadEnabled(new URL(unsecuredTargetUri)); result = Boolean.TRUE;
        }};

        // WHEN
        HttpRoute result = domibusRoutePlanner.determineRoute(host, request, context);

        // THEN
        Assert.assertNotSame("Should have replaced the initial route when SSL offloading is enabled", route, result);
    }

    @Test
    public void testDetermineRoute_sslOffloadEnabled_routeWithoutProxy(@Injectable HttpHost initialTargetHost,
                                                                       @Injectable InetAddress initialLocalAddress) throws Exception {
        // GIVEN
        final String unsecuredTargetUri = "https://ec.europa.eu";
        new Expectations() {{
            host.toURI(); result = unsecuredTargetUri;
            sslOffloadService.isSslOffloadEnabled(new URL(unsecuredTargetUri)); result = Boolean.TRUE;
            route.getProxyHost(); result = null;
            route.getTargetHost(); result = initialTargetHost;
            route.getLocalAddress(); result = initialLocalAddress;
        }};

        // WHEN
        domibusRoutePlanner.determineRoute(host, request, context);

        // THEN
        new Verifications() {{
            new HttpRoute(initialTargetHost, initialLocalAddress, false); times = 1;
        }};
    }

    @Test
    public void testDetermineRoute_sslOffloadEnabled_routeWithProxy(@Injectable HttpHost initialTargetHost,
                                                                    @Injectable InetAddress initialLocalAddress,
                                                                    @Injectable HttpHost initialProxyHost) throws Exception {
        // GIVEN
        final String unsecuredTargetUri = "https://ec.europa.eu";
        new Expectations() {{
            host.toURI(); result = unsecuredTargetUri;
            sslOffloadService.isSslOffloadEnabled(new URL(unsecuredTargetUri)); result = Boolean.TRUE;
            route.getProxyHost(); result = initialProxyHost;
            route.getTargetHost(); result = initialTargetHost;
            route.getLocalAddress(); result = initialLocalAddress;
        }};

        // WHEN
        domibusRoutePlanner.determineRoute(host, request, context);

        new Verifications() {{
            new HttpRoute(initialTargetHost, initialLocalAddress, initialProxyHost, false); times = 1;
        }};
    }
}