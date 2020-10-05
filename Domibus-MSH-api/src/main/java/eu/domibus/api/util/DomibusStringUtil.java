package eu.domibus.api.util;

import org.apache.commons.lang3.StringUtils;

public final class DomibusStringUtil {
    public static final int DEFAULT_MAX_STRING_LENGTH = 255;

    public static String uncamelcase(String str) {
        String result = str.replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
        return result.substring(0, 1).toUpperCase() + result.substring(1);
    }

    public static boolean isStringLengthGreaterThanDefaultMaxLength(String testString, TRIM_DECISION trimDecision) {
        return ((testString != null) && (StringUtils.length((trimDecision.getValue()) ? testString.trim() : testString) > DEFAULT_MAX_STRING_LENGTH));
    }

    public enum TRIM_DECISION {
        TRIM(true),
        DO_NOT_TRIM(false);

        private final boolean trimDecision;

        TRIM_DECISION(boolean trimDecision) {
            this.trimDecision = trimDecision;
        }

        public boolean getValue() {
            return trimDecision;
        }
    }
}
