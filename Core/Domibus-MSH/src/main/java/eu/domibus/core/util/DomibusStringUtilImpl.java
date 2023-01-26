package eu.domibus.core.util;

import eu.domibus.api.util.DomibusStringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.trim;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
@Component
public class DomibusStringUtilImpl implements DomibusStringUtil {

    public static final String ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH = " is too long (over 255 characters).";
    public static final String ERROR_MSG_STRING_LONGER_THAN_STRING_LENGTH_1024 = " is too long (over 1024 characters).";
    public static final int DEFAULT_MAX_STRING_LENGTH = 255;
    public static final int MAX_STRING_LENGTH_1024 = 1024;
    public static final String STRING_SANITIZE_REGEX = "[^\\w@.-]";

    @Override
    public String unCamelCase(String str) {
        String result = str.replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
        return result.substring(0, 1).toUpperCase() + result.substring(1);
    }
    @Override
    public boolean isStringLengthLongerThanDefaultMaxLength(String testString) {
        return ((testString != null) && (StringUtils.length(testString) > DEFAULT_MAX_STRING_LENGTH));
    }

    @Override
    public boolean isTrimmedStringLengthLongerThanDefaultMaxLength(String testString) {
        return isStringLengthLongerThanDefaultMaxLength(trim(testString));
    }

    @Override
    public boolean isStringLengthLongerThan1024Chars(String testString) {
        return (StringUtils.length(testString) > MAX_STRING_LENGTH_1024);
    }

    @Override
    public String sanitizeFileName(String fileName) {
        return fileName.replaceAll(STRING_SANITIZE_REGEX, "_");
    }
}
