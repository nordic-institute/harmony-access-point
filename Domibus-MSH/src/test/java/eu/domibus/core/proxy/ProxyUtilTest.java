package eu.domibus.core.proxy;

import eu.domibus.core.proxy.DomibusProxy;
import eu.domibus.core.proxy.DomibusProxyService;
import eu.domibus.core.proxy.ProxyUtil;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
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

    @Before
    public void testData() {
        new NonStrictExpectations() {
            {
                DomibusProxy domibusProxy = new DomibusProxy();
                domibusProxy.setEnabled(true);
                domibusProxy.setHttpProxyHost("somehost");
                domibusProxy.setHttpProxyPort(8280);
                domibusProxy.setHttpProxyUser("someuser");
                domibusProxy.setHttpProxyPassword("somepassword");

                domibusProxyService.getDomibusProxy();
                result = domibusProxy;

                domibusProxyService.useProxy();
                result = true;

                domibusProxyService.isProxyUserSet();
                result = true;

                domibusProxyService.isNonProxyHostsSet();
                result = false;
            }};
    }

    @Test
    public void testGetConfiguredProxy() {
        HttpHost httpHost = proxyUtil.getConfiguredProxy();
        Assert.assertEquals(httpHost.getPort(), 8280);
        CredentialsProvider credentialsProvider = proxyUtil.getConfiguredCredentialsProvider();
        Assert.assertEquals("someuser", credentialsProvider.getCredentials(AuthScope.ANY).getUserPrincipal().getName());
    }
}

