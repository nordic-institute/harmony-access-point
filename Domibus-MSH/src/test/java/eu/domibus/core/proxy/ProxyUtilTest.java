package eu.domibus.core.proxy;

import mockit.*;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author idragusa
 * @since 4.0
 */
public class ProxyUtilTest {

    @Tested
    ProxyUtil proxyUtil;

    @Injectable
    protected DomibusProxyService domibusProxyService;
    private DomibusProxy domibusProxy;

    @Before
    public void setUp() {
        domibusProxy = new DomibusProxy();
        domibusProxy.setEnabled(true);
        domibusProxy.setHttpProxyHost("somehost");
        domibusProxy.setHttpProxyPort(8280);
        domibusProxy.setHttpProxyUser("someuser");
        domibusProxy.setHttpProxyPassword("somepassword");
        domibusProxy.setNonProxyHosts("NonProxyHosts");
    }

    @Test
    public void getConfiguredCredentialsProvider() {
        new Expectations() {{
            domibusProxyService.getDomibusProxy();
            result = domibusProxy;

            domibusProxyService.useProxy();
            result = true;

            domibusProxyService.isProxyUserSet();
            result = true;
        }};

        CredentialsProvider credentialsProvider = proxyUtil.getConfiguredCredentialsProvider();
        Assert.assertEquals("someuser",credentialsProvider.getCredentials(AuthScope.ANY).getUserPrincipal().getName());

        new FullVerifications() {
        };
    }

    @Test
    public void getConfiguredCredentialsProvider_noProxy() {
        new Expectations() {{
            domibusProxyService.useProxy();
            result = false;
        }};

        CredentialsProvider credentialsProvider = proxyUtil.getConfiguredCredentialsProvider();
        Assert.assertNull(credentialsProvider);

        new FullVerifications() {
        };
    }

    @Test
    public void getConfiguredCredentialsProvider_noProxyUserSet() {
        new Expectations() {{
            domibusProxyService.useProxy();
            result = true;
            domibusProxyService.isProxyUserSet();
            result = false;
        }};

        CredentialsProvider credentialsProvider = proxyUtil.getConfiguredCredentialsProvider();
        Assert.assertNull(credentialsProvider);

        new FullVerifications() {
        };
    }

    @Test
    public void getConfiguredProxy() {
        new Expectations() {{
            domibusProxyService.getDomibusProxy();
            result = domibusProxy;

            domibusProxyService.useProxy();
            result = true;
        }};

        HttpHost httpHost = proxyUtil.getConfiguredProxy();
        Assert.assertEquals(8280, httpHost.getPort());
        Assert.assertEquals("somehost", httpHost.getHostName());

        new FullVerifications() {
        };
    }

    @Test
    public void getConfiguredProxy_null() {
        new Expectations() {{
            domibusProxyService.useProxy();
            result = false;
        }};

        HttpHost httpHost = proxyUtil.getConfiguredProxy();
        Assert.assertNull(httpHost);

        new FullVerifications() {
        };
    }

    @Test
    public void configureProxy(@Mocked HTTPClientPolicy httpClientPolicy,
                               @Mocked HTTPConduit httpConduit) {
        new Expectations() {{
            domibusProxyService.useProxy();
            result = true;

            domibusProxyService.isProxyUserSet();
            result = true;

            domibusProxyService.getDomibusProxy();
            result = domibusProxy;
        }};

        proxyUtil.configureProxy(httpClientPolicy, httpConduit);

        new FullVerifications() {{
            httpClientPolicy.setProxyServer(domibusProxy.getHttpProxyHost());
            httpClientPolicy.setProxyServerPort(domibusProxy.getHttpProxyPort());
            httpClientPolicy.setProxyServerType(org.apache.cxf.transports.http.configuration.ProxyServerType.HTTP);

            httpClientPolicy.setNonProxyHosts(domibusProxy.getNonProxyHosts());

            ProxyAuthorizationPolicy proxyAuthorizationPolicy;
            httpConduit.setProxyAuthorization(proxyAuthorizationPolicy = withCapture());

            Assert.assertEquals(domibusProxy.getHttpProxyPassword(), proxyAuthorizationPolicy.getPassword());
            Assert.assertEquals(domibusProxy.getHttpProxyUser(), proxyAuthorizationPolicy.getUserName());
        }};
    }

    @Test
    public void configureProxy_noProxy(@Mocked HTTPClientPolicy httpClientPolicy,
                               @Mocked HTTPConduit httpConduit) {
        new Expectations() {{
            domibusProxyService.useProxy();
            result = false;
        }};

        proxyUtil.configureProxy(httpClientPolicy, httpConduit);

        new FullVerifications() {{
        }};
    }

}

