package eu.domibus.core.util;

import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
@RunWith(JMockit.class)
public class DomibusStringUtilImplTest {

    @Tested
    private DomibusStringUtilImpl domibusStringUtil;

    @Test
    public void sanitizeFileName() {
        final String fileName = "/test-String&123@97.txt";
        String result = domibusStringUtil.sanitizeFileName(fileName);
        assertEquals(result, "_test-String_123@97.txt");

    }

    @Test
    public void isTrimmedStringLengthLongerThanDefaultMaxLength() {
        String messageId = StringUtils.repeat("X", 256);

        Assert.assertTrue(domibusStringUtil.isTrimmedStringLengthLongerThanDefaultMaxLength(messageId));
    }

    @Test
    public void isTrimmedStringLengthLongerThanDefaultMaxLength_255() {
        String messageId = StringUtils.repeat("X", 255);

        Assert.assertFalse(domibusStringUtil.isTrimmedStringLengthLongerThanDefaultMaxLength(messageId));
    }

    @Test
    public void isStringLengthLongerThan1024Chars_Valid() {
        String messageId = StringUtils.repeat("X", 1024);

        Assert.assertFalse(domibusStringUtil.isStringLengthLongerThan1024Chars(messageId));
    }

    @Test
    public void isStringLengthLongerThan1024Chars() {
        String messageId = StringUtils.repeat("X", 1025);

        Assert.assertTrue(domibusStringUtil.isStringLengthLongerThan1024Chars(messageId));
    }

    @Test
    public void unCamelCase() {
        String camelCaseString = "messageId";

        String unCamelCaseString = domibusStringUtil.unCamelCase(camelCaseString);
        Assert.assertNotEquals(unCamelCaseString, camelCaseString);
        Assert.assertEquals(unCamelCaseString, "Message Id");
    }
}