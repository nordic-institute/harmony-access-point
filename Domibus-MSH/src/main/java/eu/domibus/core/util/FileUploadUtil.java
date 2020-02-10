package eu.domibus.core.util;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS__FILE_UPLOAD_MAX_SIZE;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Helper methods for file upload through REST interface
 */
@Service
public class FileUploadUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FileUploadUtil.class);

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    public byte[] sanitiseFileUpload(MultipartFile file) throws IllegalArgumentException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException(String.format("Failed to upload the %s since it was empty.", file.getName()));
        }

        int maxFileSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS__FILE_UPLOAD_MAX_SIZE);
        if (maxFileSize != 0 && file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(String.format("Failed to upload the %s since the file size exceeds the acceptable limit.", file.getName()));
        }

        byte[] pModeContent;
        try {
            pModeContent = file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Failed to upload the %s since could not read the content.", file.getName()));
        }
        return pModeContent;
    }
}