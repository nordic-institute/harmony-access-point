package eu.domibus.core.property.listeners;

import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_FILE_UPLOAD_MAX_SIZE;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@RunWith(JMockit.class)
public class FileUploadMaxSizeChangeListenerTest {

    @Injectable
    protected CommonsMultipartResolver multipartResolver;

    @Tested
    protected FileUploadMaxSizeChangeListener listener = new FileUploadMaxSizeChangeListener(multipartResolver);

    @Test
    public void testPropertyValueChanged() {
        listener.propertyValueChanged("default", DOMIBUS_FILE_UPLOAD_MAX_SIZE, "100");

        new Verifications() {{
            multipartResolver.setMaxUploadSize(100);
        }};
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPropertyValueChanged() {
        listener.propertyValueChanged("default", DOMIBUS_FILE_UPLOAD_MAX_SIZE, "invalid");
        Assert.fail("Invalid property value shouldn't have been accepted");
    }
}
