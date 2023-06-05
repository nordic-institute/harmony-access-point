package eu.domibus.core.property.listeners;

import mockit.FullVerifications;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
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

    @Mocked
    protected CommonsMultipartResolver multipartResolver;

    protected FileUploadMaxSizeChangeListener listener ;

    @Before
    public void setUp() {
        listener = new FileUploadMaxSizeChangeListener(multipartResolver);
    }

    @Test
    public void handlesProperty_true() {
        Assert.assertTrue(listener.handlesProperty(DOMIBUS_FILE_UPLOAD_MAX_SIZE));
    }

    @Test
    public void handlesProperty_false() {
        Assert.assertFalse(listener.handlesProperty("OTHER"));
    }

    @Test
    public void testPropertyValueChanged() {
        listener.propertyValueChanged("default", DOMIBUS_FILE_UPLOAD_MAX_SIZE, "100");

        new FullVerifications() {{
            multipartResolver.setMaxUploadSize(100);
            times = 1;
        }};
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPropertyValueChanged() {
        listener.propertyValueChanged("default", DOMIBUS_FILE_UPLOAD_MAX_SIZE, "invalid");
        Assert.fail("Invalid property value shouldn't have been accepted");
    }
}
