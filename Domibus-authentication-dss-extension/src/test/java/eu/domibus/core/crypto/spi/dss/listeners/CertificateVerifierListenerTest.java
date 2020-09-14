package eu.domibus.core.crypto.spi.dss.listeners;

import eu.domibus.core.crypto.spi.dss.DssCache;
import mockit.Mock;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager.*;
import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
@RunWith(JMockit.class)
public class CertificateVerifierListenerTest {

    @Test
    public void handlesProperty(@Mocked final DssCache dssCache) {
        CertificateVerifierListener certificateVerifierListener = new CertificateVerifierListener(dssCache);
        assertTrue(certificateVerifierListener.handlesProperty(DSS_PERFORM_CRL_CHECK));
        assertTrue(certificateVerifierListener.handlesProperty(AUTHENTICATION_DSS_CHECK_REVOCATION_FOR_UNTRUSTED_CHAINS));
        assertTrue(certificateVerifierListener.handlesProperty(AUTHENTICATION_DSS_EXCEPTION_ON_MISSING_REVOCATION_DATA));
        assertFalse(certificateVerifierListener.handlesProperty("any other property"));
    }

    @Test
    public void propertyValueChanged(@Mocked final DssCache dssCache) {
        CertificateVerifierListener certificateVerifierListener = new CertificateVerifierListener(dssCache);
        certificateVerifierListener.propertyValueChanged(null,null,null);
        new Verifications(){{
           dssCache.clear();
        }};
    }
}