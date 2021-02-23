package eu.domibus.core.ssl.offload;

import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.transport.http.Address;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_CONNECTION_CXF_SSL_OFFLOAD_ENABLE;

@RunWith(JMockit.class)
public class SslOffloadServiceImplTest {

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Tested
    private SslOffloadServiceImpl sslOffloadService;

    @Test
    public void testIsSslOffloadEnabled_nullUrl() {
        // WHEN
        boolean sslOffloadEnabled = sslOffloadService.isSslOffloadEnabled(null);

        // THEN
        Assert.assertFalse("Should have returned false if passing null when checking whether the SSL offload is enabled or not", sslOffloadEnabled);
    }

    @Test
    public void testIsSslOffloadEnabled_httpUrl() throws Exception {
        // GIVEN
        URL unsecureUrl = new URL("http://ec.europa.eu");

        // WHEN
        boolean sslOffloadEnabled = sslOffloadService.isSslOffloadEnabled(unsecureUrl);

        // THEN
        Assert.assertFalse("Should have returned false if passing an HTTP URL when checking whether the SSL offload is enabled or not", sslOffloadEnabled);
    }

    @Test
    public void testIsSslOffloadEnabled_sslDomibusPropertyOff() throws Exception {
        // GIVEN
        URL secureUrl = new URL("https://ec.europa.eu");
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_CONNECTION_CXF_SSL_OFFLOAD_ENABLE);
            result = Boolean.FALSE;
        }};

        // WHEN
        boolean sslOffloadEnabled = sslOffloadService.isSslOffloadEnabled(secureUrl);

        // THEN
        Assert.assertFalse("Should have returned false if the SSL Domibus property is off (even if passing an HTTPS URL) when checking whether the SSL offload is enabled or not", sslOffloadEnabled);
    }

    @Test
    public void testIsSslOffloadEnabled() throws Exception {
        // GIVEN
        URL secureUrl = new URL("https://ec.europa.eu");
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_CONNECTION_CXF_SSL_OFFLOAD_ENABLE);
            result = Boolean.TRUE;
        }};

        // WHEN
        boolean sslOffloadEnabled = sslOffloadService.isSslOffloadEnabled(secureUrl);

        // THEN
        Assert.assertTrue("Should have returned true if the SSL Domibus property is on and if passing an HTTPS URL when checking whether the SSL offload is enabled or not", sslOffloadEnabled);
    }

    @Test
    public void offloadAddress_replacesAddress() throws Exception {
        // GIVEN
        Address secureAddress = new Address("https://ec.europa.eu");

        // WHEN
        Address result = sslOffloadService.offload(secureAddress);

        // THEN
        Assert.assertNotSame("Should have replaced the address when offloading", secureAddress, result);
    }

    @Test
    public void offloadAddress_switchesAddressToHttp() throws Exception {
        // GIVEN
        Address secureAddress = new Address("https://ec.europa.eu");

        // WHEN
        Address result = sslOffloadService.offload(secureAddress);

        // THEN
        Assert.assertEquals("Should have switched the address URL to HTTP when offloading", "http://ec.europa.eu", result.getString());
    }

    @Test
    public void offloadAddress_revertsProtocolToHttps() throws Exception {
        // GIVEN
        Address secureAddress = new Address("https://ec.europa.eu");

        // WHEN
        Address result = sslOffloadService.offload(secureAddress);

        // THEN
        Assert.assertEquals("Should have reverted the address URL protocol back to HTTPS", "https", result.getURL().getProtocol());
    }

}