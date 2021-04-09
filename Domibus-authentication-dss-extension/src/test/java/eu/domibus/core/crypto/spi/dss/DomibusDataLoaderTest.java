package eu.domibus.core.crypto.spi.dss;

import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyStore;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DomibusDataLoaderTest {

    @Test
    public void getSSLTrustStore(@Mocked KeyStore trustStore) {
        DomibusDataLoader domibusDataLoader = new DomibusDataLoader(trustStore);
        assertEquals(trustStore, domibusDataLoader.getSSLTrustStore());
    }


}