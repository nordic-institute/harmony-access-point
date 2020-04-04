package eu.domibus.web.rest.validators;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.rest.validators.BlacklistValidatorDelegate;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.ValidationException;
import java.lang.annotation.Annotation;

public class BlacklistValidatorTest {

    @Tested
    BlacklistValidator blacklistValidator;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    BlacklistValidatorDelegate blacklistValidatorDelegate;

    @Test
    public void shouldValidateWhenBlacklistIsDefined() {
        new Expectations(blacklistValidator) {{
            domibusPropertyProvider.getProperty(BlacklistValidator.BLACKLIST_PROPERTY);
            returns("%'\\/");
        }};

        blacklistValidator.initialize(null);

        String validValue = "abc.";
        String invalidValue = "abc%";
        String emptyValue = "";

        boolean actualValid = blacklistValidator.isValid(validValue);
        boolean actualInvalid = blacklistValidator.isValid(invalidValue);
        boolean emptyIsValid = blacklistValidator.isValid(emptyValue);

        Assert.assertEquals(true, actualValid);
        Assert.assertEquals(false, actualInvalid);
        Assert.assertEquals(true, emptyIsValid);
    }

    @Test
    public void shouldValidateWhenBlacklistIsEmpty() {
        new Expectations(blacklistValidator) {{
            domibusPropertyProvider.getProperty(BlacklistValidator.BLACKLIST_PROPERTY);
            returns("");
        }};

        blacklistValidator.initialize(null);

        String invalidValue = "abc%";
        boolean result = blacklistValidator.isValid(invalidValue, (CustomWhiteListed) null);

        Assert.assertEquals(true, result);
    }

    @Test
    public void shouldThrowWhenInvalid() {
        new Expectations(blacklistValidator) {{
            domibusPropertyProvider.getProperty(BlacklistValidator.BLACKLIST_PROPERTY);
            returns("%'\\/");
        }};

        blacklistValidator.init();

        String validValue = "abc.";
        String invalidValue = "abc%";

        try {
            blacklistValidator.validate(validValue);
        } catch (IllegalArgumentException ex) {
            Assert.fail("Should not throw for valid values");
        }
        try {
            blacklistValidator.validate(invalidValue);
            Assert.fail("Should throw for invalid values");
        } catch (ValidationException ex) {
        }
    }

    @Test
    public void isWhiteListValid() {
        new Expectations(blacklistValidator) {{
            domibusPropertyProvider.getProperty(BlacklistValidator.WHITELIST_PROPERTY);
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

        blacklistValidator.initialize(null);

        String validValue = "abc.";
        String invalidValue = "abc%";
        String emptyValue = "";

        boolean actualValid = blacklistValidator.isWhiteListValid(validValue, null);
        boolean actualInvalid = blacklistValidator.isWhiteListValid(invalidValue, customChars);
        boolean emptyIsValid = blacklistValidator.isWhiteListValid(emptyValue, null);

        Assert.assertEquals(true, actualValid);
        Assert.assertEquals(true, actualInvalid);
        Assert.assertEquals(true, emptyIsValid);
    }
}