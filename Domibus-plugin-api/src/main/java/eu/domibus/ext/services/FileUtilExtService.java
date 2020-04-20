package eu.domibus.ext.services;

/**
 * @since 4.1.4
 * @author Catalin Enache
 */
public interface FileUtilExtService {

    /**
     * Sanitizes file name by removing any references (prefixes) to full or relative path
     *
     * @return sanitized value of the file name, null if sanitization fails
     */
    String sanitizeFileName(String fileName);
}
