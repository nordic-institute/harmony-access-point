package eu.domibus.core.rest.validators;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.validators.CustomWhiteListed;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.ValidationException;
import java.lang.annotation.Annotation;

public class FieldBlacklistValidatorTest {

    @Tested
    FieldBlacklistValidator fieldBlacklistValidator;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Test
    public void shouldValidateWhenBlacklistIsDefined() {
        new Expectations(fieldBlacklistValidator) {{
            domibusPropertyProvider.getProperty(FieldBlacklistValidator.BLACKLIST_PROPERTY);
            returns("%'\\/");
        }};

        fieldBlacklistValidator.initialize(null);

        String validValue = "abc.";
        String invalidValue = "abc%";
        String emptyValue = "";

        boolean actualValid = fieldBlacklistValidator.isValid(validValue);
        boolean actualInvalid = fieldBlacklistValidator.isValid(invalidValue);
        boolean emptyIsValid = fieldBlacklistValidator.isValid(emptyValue);

        Assert.assertEquals(true, actualValid);
        Assert.assertEquals(false, actualInvalid);
        Assert.assertEquals(true, emptyIsValid);
    }

    @Test
    public void shouldValidateWhenBlacklistIsEmpty() {
        new Expectations(fieldBlacklistValidator) {{
            domibusPropertyProvider.getProperty(FieldBlacklistValidator.BLACKLIST_PROPERTY);
            returns("");
        }};

        fieldBlacklistValidator.initialize(null);

        String invalidValue = "abc%";
        boolean result = fieldBlacklistValidator.isValid(invalidValue, (CustomWhiteListed) null);

        Assert.assertEquals(true, result);
    }

    @Test
    public void shouldThrowWhenInvalid() {
        new Expectations(fieldBlacklistValidator) {{
            domibusPropertyProvider.getProperty(FieldBlacklistValidator.BLACKLIST_PROPERTY);
            returns("%'\\/");
        }};

        fieldBlacklistValidator.init();

        String validValue = "abc.";
        String invalidValue = "abc%";

        try {
            fieldBlacklistValidator.validate(validValue);
        } catch (IllegalArgumentException ex) {
            Assert.fail("Should not throw for valid values");
        }
        try {
            fieldBlacklistValidator.validate(invalidValue);
            Assert.fail("Should throw for invalid values");
        } catch (ValidationException ex) {
        }
    }

    @Test
    public void isWhiteListValid() {
        new Expectations(fieldBlacklistValidator) {{
            domibusPropertyProvider.getProperty(FieldBlacklistValidator.WHITELIST_PROPERTY);
            returns("^[\\w\\-\\.: @]*$");
        }};

        CustomWhiteListed customChars = new CustomWhiteListed() {
            @Override
            public String permitted() {
                return "%";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return CustomWhiteListed.class;
            }
        };

        fieldBlacklistValidator.initialize(null);

        String validValue = "abc.";
        String invalidValue = "abc%";
        String emptyValue = "";

        boolean actualValid = fieldBlacklistValidator.isWhiteListValid(validValue, null);
        boolean actualInvalid = fieldBlacklistValidator.isWhiteListValid(invalidValue, customChars);
        boolean emptyIsValid = fieldBlacklistValidator.isWhiteListValid(emptyValue, null);

        Assert.assertEquals(true, actualValid);
        Assert.assertEquals(true, actualInvalid);
        Assert.assertEquals(true, emptyIsValid);
    }
}