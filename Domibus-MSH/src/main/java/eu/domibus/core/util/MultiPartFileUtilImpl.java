package eu.domibus.core.util;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Helper methods for file upload through REST interface
 */
@Service
public class MultiPartFileUtilImpl implements MultiPartFileUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MultiPartFileUtilImpl.class);

    @Override
    public byte[] validateAndGetFileContent(MultipartFile file, MimeType type) throws RequestValidationException {
        if (file.isEmpty()) {
            throw new RequestValidationException(String.format("Failed to upload the %s since it was empty.", file.getName()));
        }

        if (type != null && !type.equals(file.getContentType())) {
            throw new RequestValidationException(String.format("Failed to upload the %s since it has the wrong mime type.", file.getName()));
        }

        byte[] pModeContent;
        try {
            pModeContent = file.getBytes();
        } catch (IOException e) {
            throw new RequestValidationException(String.format("Failed to upload the %s since could not read the content.", file.getName()), e);
        }
        return pModeContent;
    }

    @Override
    public byte[] validateAndGetFileContent(MultipartFile file) throws RequestValidationException {
        return validateAndGetFileContent(file, null);
    }
}