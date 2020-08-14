package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_FILE_UPLOAD_MAX_SIZE;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Handles the change of domibus.file.upload.maxSize property
 */
@Service
public class FileUploadMaxSizeChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private CommonsMultipartResolver multipartResolver;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_FILE_UPLOAD_MAX_SIZE);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        int size = Integer.parseInt(propertyValue);
        multipartResolver.setMaxUploadSize(size);
    }
}
