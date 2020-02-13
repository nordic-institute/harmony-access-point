package eu.domibus.api.util;

import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Helper methods for file upload through REST interface
 */
@Service
public interface MultiPartFileUtil {
    byte[] validateAndGetFileContent(MultipartFile file, List<MimeType> allowedTypes) throws IllegalArgumentException;

    byte[] validateAndGetFileContent(MultipartFile file) throws IllegalArgumentException;
}