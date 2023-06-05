package eu.domibus.core.rest.validators;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.web.rest.validators.FieldWhiteListed;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_USER_INPUT_BLACK_LIST;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_USER_INPUT_WHITE_LIST;

public class BaseBlacklistValidatorTest {

    @Tested
    BaseBlacklistValidator<FieldWhiteListed, String> validator = new FieldBlacklistValidator();

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Test
    public void testFindNonMatchingCharactersWithoutAdditionalPermittedList() {
        String inputValue = "the Dot and the Space should not pass...";
        Pattern whitelistPattern = Pattern.compile("\\w*");

        List<Character> result = validator.findNonMatchingCharacters(inputValue, whitelistPattern, null);
        Assert.assertTrue(result.containsAll(Arrays.asList('.', ' ')));
    }

    @Test
    public void testFindNonMatchingCharactersWithAdditionalPermittedList() {
        String inputValue = "the Dot and the Space should now pass...";
        Pattern whitelistPattern = Pattern.compile("\\w*");
        String additionalWhitelist = " .";

        List<Character> result = validator.findNonMatchingCharacters(inputValue, whitelistPattern, additionalWhitelist);
        Assert.assertTrue(CollectionUtils.isEmpty(result));
    }

    @Test
    public void testFindNonMatchingCharactersWithEmptyInput() {
        String inputValue = "";
        Pattern whitelistPattern = Pattern.compile("\\w*");
        String additionalWhitelist = " .";

        List<Character> result = validator.findNonMatchingCharacters(inputValue, whitelistPattern, additionalWhitelist);
        Assert.assertTrue(CollectionUtils.isEmpty(result));
    }

    @Test
    public void testIsValidWhenValidationIsDisabled(@Mocked String testValue, @Mocked ConstraintValidatorContext mockContext) {
        boolean result = validator.isValid(testValue, mockContext);
        Assert.assertEquals(true, result);
    }

    @Test
    public void testIsValidWithInvalidValue(@Mocked ConstraintValidatorContext mockContext) {
        String testValue = "'";

        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_USER_INPUT_WHITE_LIST);
            this.result = "\\w*";
            domibusPropertyProvider.getProperty(DOMIBUS_USER_INPUT_BLACK_LIST);
            this.result = "'\\u0022(){}[];,+=%&*#<>/";
        }};
        validator.reset();

        boolean result = validator.isValid(testValue, mockContext);
        Assert.assertEquals(false, result);
    }
}
