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
    public void testDomibusPropertyExceptionIsRaised() {
        DomibusPropertyException exception = Assert.assertThrows(DomibusPropertyException.class,
                () -> callPasswordPropertiesValidation(true));
        Assert.assertTrue(exception.getMessage().contains("all property passwords must match"));
    }

    @Test
    public void testDomibusPropertyExceptionIsNotRaised() {
         callPasswordPropertiesValidation(false);
    }

    private void callPasswordPropertiesValidation(boolean enforcePasswordPolicy) {
        String enforcePreviousPropValue = domibusPropertyProvider.getProperty(DOMIBUS_PROPERTIES_PASSWORD_POLICY_ENFORCE);
        String patternPreviousPropValue = domibusPropertyProvider.getProperty(DOMIBUS_PROPERTIES_PASSWORD_POLICY_PATTERN);
        String passwordPreviousPropValue = domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD);

        try {
            domibusPropertyProvider.setProperty(DOMIBUS_PROPERTIES_PASSWORD_POLICY_ENFORCE, String.valueOf(enforcePasswordPolicy));
            domibusPropertyProvider.setProperty(DOMIBUS_PROPERTIES_PASSWORD_POLICY_PATTERN, "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&+=\\\\-_<>.,?:;*/()|\\\\[\\\\]{}'\"\\\\\\\\]).{16,32}$");
            domibusPropertyProvider.setProperty(DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD, "test123");
            domibusPropertyValidatorService.validatePropertiesPasswordPolicy();
        }
        finally {
            domibusPropertyProvider.setProperty(DOMIBUS_PROPERTIES_PASSWORD_POLICY_ENFORCE, enforcePreviousPropValue);
            domibusPropertyProvider.setProperty(DOMIBUS_PROPERTIES_PASSWORD_POLICY_PATTERN, patternPreviousPropValue);
            domibusPropertyProvider.setProperty(DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD, passwordPreviousPropValue);
        }
    }
}
