package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.util.backup.BackupService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.configuration.security.TLSClientParametersType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class TLSDomainCryptoServiceSpiImplTest {

    @Tested
    private TLSDomainCryptoServiceSpiImpl tlsDomainCryptoServiceSpi;

    @Injectable
    private TLSReaderService tlsReader;

    @Injectable
    protected CertificateService certificateService;

    @Injectable
    protected SignalService signalService;

    @Injectable
    protected DomainCoreConverter domainCoreConverter;

    @Injectable
    protected BackupService backupService;

    @Injectable
    private Domain domain;

    @Injectable
    TLSClientParametersType params;

    @Test
    public void getTrustStoreLocation() {
        final String truststoreLocation = "TruststoreLocation";

        new Expectations(tlsDomainCryptoServiceSpi) {{
            params.getTrustManagers().getKeyStore().getFile();
            result = truststoreLocation;
        }};

        String result = tlsDomainCryptoServiceSpi.getTrustStoreLocation();

        Assert.assertEquals(truststoreLocation, result);
    }

    @Test
    public void getTrustStorePassword() {
        final String truststorePassword = "TruststoreLocation";

        new Expectations(tlsDomainCryptoServiceSpi) {{
            params.getTrustManagers().getKeyStore().getPassword();
            result = truststorePassword;
        }};

        String result = tlsDomainCryptoServiceSpi.getTrustStorePassword();

        Assert.assertEquals(truststorePassword, result);
    }

    @Test
    public void getTrustStoreType() {
        final String truststoreType = "TruststoreLocation";

        new Expectations(tlsDomainCryptoServiceSpi) {{
            params.getTrustManagers().getKeyStore().getType();
            result = truststoreType;
        }};

        String result = tlsDomainCryptoServiceSpi.getTrustStoreType();

        Assert.assertEquals(truststoreType, result);
    }
}