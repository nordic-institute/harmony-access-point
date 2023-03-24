package eu.domibus.ext.delegate.services.util;

import eu.domibus.api.util.FileServiceUtil;
import eu.domibus.ext.services.FileUtilExtService;
import org.springframework.stereotype.Service;

/**
 * Implementation for {@link FileUtilExtService}
 * @since 4.1.4
 * @author Catalin Enache
 */
@Service
public class FileUtilServiceDelegate implements FileUtilExtService {

    protected final FileServiceUtil fileServiceUtil;

    public FileUtilServiceDelegate(FileServiceUtil fileServiceUtil) {
        this.fileServiceUtil = fileServiceUtil;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sanitizeFileName(String fileName) {
        return fileServiceUtil.sanitizeFileName(fileName);
    }
}
