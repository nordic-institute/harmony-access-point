package eu.domibus.core.rest.validators;

import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.validators.DomibusPropertyValidator;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PROPERTY_VALIDATION_ENABLED;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DomibusPropertyValueValidatorTest {

    @Tested
    DomibusPropertyValueValidator domibusPropertyValueValidator;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    FieldBlacklistValidator fieldBlacklistValidator;

    @Test
    public void shouldUseBlacklistValidatorForStrings(@Mocked DomibusProperty property) {
        new Expectations(domibusPropertyValueValidator) {{
            domibusPropertyValueValidator.getValidator(property.getMetadata());
            result = null;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_PROPERTY_VALIDATION_ENABLED);
            result = true;
        }};

        domibusPropertyValueValidator.validate(property);

        new Verifications() {{
            fieldBlacklistValidator.validate(property.getValue());
        }};
    }

    @Test
    public void shouldUsePropertyValueValidatorWhenAvailable(@Mocked DomibusProperty property, @Mocked DomibusPropertyValidator validator) {
        new Expectations(domibusPropertyValueValidator) {{
            domibusPropertyValueValidator.getValidator(property.getMetadata());
            result = validator;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_PROPERTY_VALIDATION_ENABLED);
            result = true;
            validator.isValid(property.getValue());
            result = true;
        }};

        domibusPropertyValueValidator.validate(property);

        new Verifications() {{
            fieldBlacklistValidator.validate(property.getValue());
            times = 0;
        }};
    }

}