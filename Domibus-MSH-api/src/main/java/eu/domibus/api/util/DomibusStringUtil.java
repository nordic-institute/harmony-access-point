package eu.domibus.api.util;

import org.apache.commons.lang3.StringUtils;

public final class DomibusStringUtil {
    public static final int DEFAULT_MAX_STRING_LENGTH = 255;
    public static final String ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH = " is too long (over 255 characters).";
    public static final boolean TRIM = true;
    public static final boolean DO_NOT_TRIM = false;

    public static String uncamelcase(String str) {
        String result = str.replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
        return result.substring(0, 1).toUpperCase() + result.substring(1);
    }

    public static boolean isStringLengthGreaterThanDefaultMaxLength(String testString, boolean trimDecision) {
        return ((testString != null) && (StringUtils.length((trimDecision) ? testString.trim() : testString) > DEFAULT_MAX_STRING_LENGTH));
    }
}
