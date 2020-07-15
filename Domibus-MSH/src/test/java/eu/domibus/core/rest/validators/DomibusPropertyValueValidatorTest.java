package eu.domibus.core.rest.validators;

import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.validators.DomibusPropertyValidator;
import eu.domibus.api.validators.CustomWhiteListed;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PROPERTY_VALIDATION_ENABLED;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DomibusPropertyBlacklistValidatorTest {

    @Tested
    DomibusPropertyBlacklistValidator domibusPropertyBlacklistValidator;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Test
    public void isValid_blackListValidation(@Mocked DomibusProperty property, @Mocked CustomWhiteListed customAnnotation) {
        boolean isValid = true;
        new Expectations(domibusPropertyBlacklistValidator) {{
            property.getMetadata().getTypeAsEnum().getValidator();
            result = null;
            domibusPropertyBlacklistValidator.isValidValue(property.getValue());
            result = isValid;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_PROPERTY_VALIDATION_ENABLED);
            result = true;
        }};

        boolean result = domibusPropertyBlacklistValidator.isValid(property, customAnnotation);

        new Verifications() {{
            domibusPropertyBlacklistValidator.isValidValue(property.getValue());
        }};

        Assert.assertEquals(isValid, result);
    }

    @Test
    public void isValid_typeValidation(@Mocked DomibusProperty property,
                                       @Mocked CustomWhiteListed customAnnotation,
                                       @Mocked DomibusPropertyValidator domibusPropertyValidator) {
        new Expectations() {{
            property.getMetadata().getTypeAsEnum().getValidator();
            result = domibusPropertyValidator;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_PROPERTY_VALIDATION_ENABLED);
            result = true;
        }};

        boolean result = domibusPropertyBlacklistValidator.isValid(property, customAnnotation);

        new Verifications() {{
            domibusPropertyBlacklistValidator.isValidValue(property.getValue());
            times = 0;
        }};

        Assert.assertEquals(true, result);
    }
}