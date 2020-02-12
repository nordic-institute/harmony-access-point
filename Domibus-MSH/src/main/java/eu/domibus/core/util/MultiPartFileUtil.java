package eu.domibus.core.util;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Helper methods for file upload through REST interface
 */
@Service
public class MultiPartFileUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MultiPartFileUtil.class);

    public byte[] validateAndGetFileContent(MultipartFile file) throws RequestValidationException {
        if (file.isEmpty()) {
            throw new RequestValidationException(String.format("Failed to upload the %s since it was empty.", file.getName()));
        }

        byte[] pModeContent;
        try {
            pModeContent = file.getBytes();
        } catch (IOException e) {
            throw new RequestValidationException(String.format("Failed to upload the %s since could not read the content.", file.getName()));
        }
        return pModeContent;
    }
}