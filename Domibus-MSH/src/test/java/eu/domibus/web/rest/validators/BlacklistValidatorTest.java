package eu.domibus.web.rest.validators;

import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;

public class BlacklistValidatorTest {

    @Tested
    BlacklistValidator blacklistValidator;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

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
        boolean result = blacklistValidator.isValid(invalidValue, null);

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
        } catch(IllegalArgumentException ex) {
            Assert.fail("Should not throw for valid values");
        }
        try {
            blacklistValidator.validate(invalidValue);
            Assert.fail("Should throw for invalid values");
        } catch(IllegalArgumentException ex) {
        }
    }
}