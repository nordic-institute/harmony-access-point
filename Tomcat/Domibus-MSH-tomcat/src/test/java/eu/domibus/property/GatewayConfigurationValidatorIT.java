package eu.domibus.property;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.property.GatewayConfigurationValidator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author François Gautier
 * @since 5.1
 */
public class GatewayConfigurationValidatorIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(GatewayConfigurationValidatorIT.class);

    @Autowired
    GatewayConfigurationValidator gatewayConfigurationValidator;

    @Autowired
    DomainContextProvider domainContextProvider;

    @Test
    public void validateCertificates() {
        gatewayConfigurationValidator.validateCertificates();

        Assert.assertNull(LOG.getMDC(DomibusLogger.MDC_DOMAIN));
    }
}
