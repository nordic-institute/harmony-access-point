package eu.domibus.api.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

    byte[] getContentFromFile(String location) throws IOException;

    /**
     * Translates a string into application/x-www-form-urlencoded format using a specific encoding scheme.
     *
     * @param s String to be translated
     * @return the translated String.
     */
    String URLEncode(String s);

    /**
     * Copy the contents of the given InputStream to the given File. Closes the inputs stream when done.
     * @return the number of bytes copied
     * @throws IOException - in case of I/O errors
     */
    int copyToFile(InputStream in, File out) throws IOException;
}
