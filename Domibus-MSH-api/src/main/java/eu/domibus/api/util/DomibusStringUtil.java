package eu.domibus.api.util;

import org.apache.commons.lang3.StringUtils;

public final class DomibusStringUtil {

    public static String uncamelcase(String str) {
        String result = str.replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
        return result.substring(0, 1).toUpperCase() + result.substring(1);
    }

    public static boolean isStringLengthGreaterThan(String testString, int maxLength) {
        return ((testString != null) && (StringUtils.length(testString) > maxLength));
    }
}
