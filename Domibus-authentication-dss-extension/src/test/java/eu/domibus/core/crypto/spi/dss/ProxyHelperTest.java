package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.service.http.proxy.ProxyConfig;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.2
 */

@RunWith(JMockit.class)
public class ProxyHelperTest {

    @Test
    public void getProxyConfig(@Mocked final DssExtensionPropertyManager dssExtensionPropertyManager) {
        ProxyHelper proxyHelper = new ProxyHelper(dssExtensionPropertyManager);
        new Expectations(){{
            dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTP_HOST);
            returns("httplocalhost","");
            dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTPS_HOST);
            returns("httpslocalhost","");
            dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTP_PORT);
            result="8080";
            dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTPS_PORT);
            result="8443";
        }};
        ProxyConfig proxyConfig = proxyHelper.getProxyConfig();
        assertNotNull(proxyConfig.getHttpProperties());
        assertNotNull(proxyConfig.getHttpsProperties());
        proxyConfig = proxyHelper.getProxyConfig();
        assertNull(proxyConfig.getHttpProperties());
        assertNull(proxyConfig.getHttpsProperties());
    }
}