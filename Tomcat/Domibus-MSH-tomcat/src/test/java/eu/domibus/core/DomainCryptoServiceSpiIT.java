package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.core.crypto.spi.DomainSpi;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author François Gautier
 * @since 5.1
 */

public class DomainCryptoServiceSpiIT extends AbstractIT {

    @Autowired
    private DomainCryptoServiceSpi domainCryptoServiceSpi;

    @Test
    public void domainCryptoServiceSpi_init() {

        Assert.assertNull(domainCryptoServiceSpi.getKeyStore());
        Assert.assertNull(domainCryptoServiceSpi.getTrustStore());
        domainCryptoServiceSpi.setDomain(new DomainSpi("default", "default"));
        domainCryptoServiceSpi.init();
        Assert.assertNotNull(domainCryptoServiceSpi.getKeyStore());
        Assert.assertNotNull(domainCryptoServiceSpi.getTrustStore());
    }
}
