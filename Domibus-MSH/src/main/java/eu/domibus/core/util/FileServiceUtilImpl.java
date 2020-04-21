package eu.domibus.core.util;

import eu.domibus.api.util.FileServiceUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @since 4.1.4
 * @author Catalin Enache
 */
@Service
public class FileServiceUtilImpl implements FileServiceUtil {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FileServiceUtilImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return fileName;
        }
        final String sanitizedFileName = FilenameUtils.getName(fileName);
        if (StringUtils.isBlank(sanitizedFileName)) {
            LOG.warn("Unable to sanitize file name which has the value [{}]", fileName);
            return null;
        }
        if (!StringUtils.equals(fileName, sanitizedFileName)) {
            LOG.warn("file name value=[{}] will be sanitized to=[{}]", fileName, sanitizedFileName);
            return sanitizedFileName;
        }
        //file name is correct no need to sanitize
        return fileName;
    }
}
