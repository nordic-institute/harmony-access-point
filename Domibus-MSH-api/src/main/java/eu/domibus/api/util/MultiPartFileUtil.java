package eu.domibus.api.util;

import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Helper methods for file upload through REST interface
 */
@Service
public interface MultiPartFileUtil {
    byte[] validateAndGetFileContent(MultipartFile file, MimeType type) throws IllegalArgumentException;

    byte[] validateAndGetFileContent(MultipartFile file) throws IllegalArgumentException;
}