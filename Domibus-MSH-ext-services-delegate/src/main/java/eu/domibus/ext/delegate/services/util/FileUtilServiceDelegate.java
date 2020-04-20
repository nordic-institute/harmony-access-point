package eu.domibus.ext.delegate.services.util;

import eu.domibus.api.util.FileServiceUtil;
import eu.domibus.ext.services.FileUtilExtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @since 4.1.4
 * @author Catalin Enache
 */
@Service
public class FileUtilServiceDelegate implements FileUtilExtService {

    @Autowired
    protected FileServiceUtil fileServiceUtil;

    @Override
    public String sanitizeFileName(String fileName) {
        return fileServiceUtil.sanitizeFileName(fileName);
    }
}
