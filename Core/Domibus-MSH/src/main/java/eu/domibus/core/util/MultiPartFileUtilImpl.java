package eu.domibus.core.util;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_FILE_UPLOAD_MAX_SIZE;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Helper methods for file upload through REST interface
 */
@Service
public class MultiPartFileUtilImpl implements MultiPartFileUtil {
    protected final DomibusPropertyProvider domibusPropertyProvider;
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MultiPartFileUtilImpl.class);

    public MultiPartFileUtilImpl(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Override
    public byte[] validateAndGetFileContent(MultipartFile file, List<MimeType> allowedTypes) throws RequestValidationException {
        if (file.isEmpty()) {
            throw new RequestValidationException(String.format("Failed to upload the %s since it was empty.", file.getName()));
        }

        if (allowedTypes != null && !allowedTypes.contains(MimeType.valueOf(file.getContentType()))) {
            throw new RequestValidationException(String.format("Failed to upload the %s since it has the wrong mime type.", file.getName()));
        }

        //validate maxSize per domain
        int maxSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_FILE_UPLOAD_MAX_SIZE);
        if (file.getSize() > maxSize) {
            throw new RequestValidationException(
                String.format("Failed to upload the %s since the size %d exceeds the maximum size limit of %d.",
                file.getName(), file.getSize(), maxSize));
        }

        byte[] fileContent;
        try {
            fileContent = file.getBytes();
        } catch (IOException e) {
            throw new RequestValidationException(String.format("Failed to upload the %s since could not read the content.", file.getName()), e);
        }
        return fileContent;
    }

    @Override
    public byte[] validateAndGetFileContent(MultipartFile file) throws RequestValidationException {
        return validateAndGetFileContent(file, null);
    }
}