package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.core.crypto.spi.DomainSpi;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Ion Perpegel
 * @since 5.0
 */

public class DomainCryptoServiceSpiIT extends AbstractIT {

    @Autowired
    private DomainCryptoServiceSpi domainCryptoServiceSpi;

    @Test
    public void domainCryptoServiceSpi_init() {
        domainCryptoServiceSpi.setDomain(new DomainSpi("default", "default"));
        domainCryptoServiceSpi.init();
    }
}
