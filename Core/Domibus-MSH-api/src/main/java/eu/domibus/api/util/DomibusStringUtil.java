package eu.domibus.api.util;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
public interface DomibusStringUtil {


    String unCamelCase(String str);

    boolean isStringLengthLongerThanDefaultMaxLength(String testString);

    boolean isTrimmedStringLengthLongerThanDefaultMaxLength(String testString);

    boolean isStringLengthLongerThan1024Chars(String testString);

    /**
     * replacing all special characters except [a-zA-Z0-9] and [@.-] with _ from any string
     *
     * @param fileName string to be sanitized by removing special characters
     * @return sanitized fileName
     */
    String sanitizeFileName(String fileName);

}
