package eu.domibus.property;

/**
 * @author Ionut Breaz
 * @since 5.1.5
 */

import org.junit.Assert;
import org.junit.Test;
import eu.domibus.test.AbstractIT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import eu.domibus.core.property.DomibusPropertyValidatorService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.DomibusPropertyException;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;


public class DomibusPropertyValidatorServiceIT extends AbstractIT {

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    DomibusPropertyValidatorService domibusPropertyValidatorService;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyValidatorServiceIT.class);


    @Test
    public void testDomibusPropertyExceptionIsRaised() throws IOException {
        String previousPropValue = domibusPropertyProvider.getProperty(DOMIBUS_PROPERTIES_PASSWORD_POLICY_ENFORCE);

        try {
            domibusPropertyProvider.setProperty(DOMIBUS_PROPERTIES_PASSWORD_POLICY_ENFORCE, "true");
            DomibusPropertyException exception = Assert.assertThrows(DomibusPropertyException.class,
                    () -> domibusPropertyValidatorService.validatePropertiesPasswordPolicy());
            Assert.assertTrue(exception.getMessage().contains("all property passwords must match"));
        }
        finally {
            domibusPropertyProvider.setProperty(DOMIBUS_PROPERTIES_PASSWORD_POLICY_ENFORCE, previousPropValue);
        }
    }
}
