package eu.domibus.web.rest.validators;

import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

public class ItemsFieldBlacklistValidatorTest {
    @Tested
    ItemsBlacklistValidator blacklistValidator;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Test
    public void testIsValid() {
        new Expectations(blacklistValidator) {{
            domibusPropertyProvider.getProperty(FieldBlacklistValidator.BLACKLIST_PROPERTY);
            returns("%'\\/");
        }};

        blacklistValidator.init();

        String[] validValue = new String[]{"", "valid value", "also invalid value"};
        String[] invalidValue = new String[]{"", "valid value", "invalid value%"};
        String[] emptyValue = new String[]{};

        boolean actualValid = blacklistValidator.isValid(validValue);
        boolean actualInvalid = blacklistValidator.isValid(invalidValue);
        boolean emptyIsValid = blacklistValidator.isValid(emptyValue);

        Assert.assertEquals(true, actualValid);
        Assert.assertEquals(false, actualInvalid);
        Assert.assertEquals(true, emptyIsValid);
    }

    @Test
    public void testGetErrorMessage() {
        String actual = blacklistValidator.getErrorMessage();
        Assert.assertEquals(ItemsWhiteListed.MESSAGE, actual);
    }

}