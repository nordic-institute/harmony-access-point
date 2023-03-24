package eu.domibus.api.util;

import eu.domibus.api.exceptions.RequestValidationException;
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
    byte[] validateAndGetFileContent(MultipartFile file, List<MimeType> allowedTypes) throws RequestValidationException;

    byte[] validateAndGetFileContent(MultipartFile file) throws RequestValidationException;
}