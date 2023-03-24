package eu.domibus.core.payload.persistence.filesystem;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public class PayloadFileStorageTest {

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Tested
    PayloadFileStorage payloadFileStorage;

    @Test
    public void init(@Injectable Domain domain, @Injectable Path path) {

        final String location = "D:\\domibus_tomcat\\domibus_blue\\domibus\\payload_storage";
        final String ATTACHMENT_STORAGE_LOCATION = "domibus.attachment.storage.location";

        new Expectations(payloadFileStorage) {{
            domibusPropertyProvider.getProperty(domain, ATTACHMENT_STORAGE_LOCATION);
            result = location;

            payloadFileStorage.createLocation(location);
            result = path;
        }};

        payloadFileStorage.init();

        new Verifications() {{
            path.toFile();
            times = 1;
        }};

    }

    @Test
    public void createLocationWithRelativePath(@Injectable Domain domain) {

        final String location = "..\\domibus_blue\\domibus\\payload_storage";
        try {
            payloadFileStorage.createLocation(location);
            Assert.fail();
        } catch (DomibusCoreException ex) {
            Assert.assertEquals(ex.getError(), DomibusCoreErrorCode.DOM_001);
            Assert.assertEquals(ex.getMessage(), "[DOM_001]:Relative path [..\\domibus_blue\\domibus\\payload_storage] is forbidden. Please provide absolute path for payload storage");
        }
    }

    @Test
    public void createLocationWithAbsolutePath(@Injectable Path path, @Injectable Files files, @Injectable Domain domain) {
        final String location = System.getProperty("java.io.tmpdir");
        path = payloadFileStorage.createLocation(location);
        Assert.assertNotNull(path);
        Assert.assertTrue(files.exists(path));
    }

    @Test
    public void getStorageDirectory(@Injectable Domain domain) {
        Assert.assertNull(payloadFileStorage.getStorageDirectory());
    }

}