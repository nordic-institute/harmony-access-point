package eu.domibus.api.util;

/**
 * @author Catalin Enache
 * @since 4.1.4
 */
public interface FileServiceUtil {

    /**
     * Sanitizes file name by removing any references (prefixes) to full or relative path
     *
     * @return sanitized value of the file name, null if sanitization fails
     */
    String sanitizeFileName(final String fileName);

    /**
     * Returns the preferred file extension for the given MIME type, or an empty
     * string if no extensions are known.
     *
     * @param mime the MIME type
     * @return preferred file extension or empty string
     */
    String getExtension(String mime);
}
