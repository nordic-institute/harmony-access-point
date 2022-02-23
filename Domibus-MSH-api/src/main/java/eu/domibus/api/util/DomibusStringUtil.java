package eu.domibus.api.util;

import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.trim;

public final class DomibusStringUtil {
    public static final int DEFAULT_MAX_STRING_LENGTH = 255;
    public static final int MAX_STRING_LENGTH_1024 = 1024;
    public static final String ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH = " is too long (over 255 characters).";
    public static final String ERROR_MSG_STRING_LONGER_THAN_STRING_LENGTH_1024 = " is too long (over 1024 characters).";
    public static final String STRING_SANITIZE_REGEX = "[^\\w@.-]";

    public static String uncamelcase(String str) {
        String result = str.replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
        return result.substring(0, 1).toUpperCase() + result.substring(1);
    }

    public static boolean isStringLengthLongerThanDefaultMaxLength(String testString) {
        return ((testString != null) && (StringUtils.length(testString) > DEFAULT_MAX_STRING_LENGTH));
    }

    public static boolean isTrimmedStringLengthLongerThanDefaultMaxLength(String testString) {
        return isStringLengthLongerThanDefaultMaxLength(trim(testString));
    }

    public static boolean isStringLengthLongerThan1024Chars(String testString) {
        return (StringUtils.length(testString) > MAX_STRING_LENGTH_1024);
    }

    /**
     * replacing all special characters except [a-zA-Z0-9] and [@.-] with _ from any string
     *
     * @param fileName string to be sanitized by removing special characters
     * @return sanitized fileName
     */
    public static String sanitizeFileName(String fileName) {
        return fileName.replaceAll(STRING_SANITIZE_REGEX, "_");
    }

}
