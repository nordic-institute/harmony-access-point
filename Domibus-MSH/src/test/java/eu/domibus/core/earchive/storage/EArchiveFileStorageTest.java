package eu.domibus.core.earchive.storage;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STORAGE_LOCATION;
import static org.junit.Assert.*;

/**
 * @author François Gautier
 * @since 5.0
 */
public class EArchiveFileStorageTest {

    public static final String LOCATION = "D:\\domibus_tomcat\\domibus_blue\\domibus\\payload_storage";
    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Tested
    EArchiveFileStorage eArchiveFileStorage;

    @Test
    public void init(@Injectable Domain domain, @Injectable Path path) throws FileSystemException {

        new Expectations(eArchiveFileStorage) {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_ACTIVE);
            result = true;

            domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_STORAGE_LOCATION);
            result = LOCATION;

            eArchiveFileStorage.createLocation(LOCATION);
            result = path;
        }};

        eArchiveFileStorage.init();

        new Verifications() {{
            path.toFile();
            times = 1;
        }};
    }

    @Test
    public void init_error(@Injectable Domain domain) {

        new Expectations(eArchiveFileStorage) {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_ACTIVE);
            result = true;

            domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_STORAGE_LOCATION);
            result = null;

        }};

        try {
            eArchiveFileStorage.init();
            fail();
        } catch (Exception e) {
            //OK
        }
    }

    @Test
    public void init_error2(@Injectable Domain domain) throws FileSystemException {


        new Expectations(eArchiveFileStorage) {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_ACTIVE);
            result = true;

            domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_STORAGE_LOCATION);
            result = LOCATION;

            eArchiveFileStorage.createLocation(LOCATION);
            result = null;

        }};

        try {
            eArchiveFileStorage.init();
            fail();
        } catch (Exception e) {
            //OK
        }
    }

    @Test
    public void createLocationWithRelativePath_returnTempFile(@Injectable Domain domain) throws FileSystemException {

        final String location = "..\\domibus_blue\\domibus\\earchiving_storage";
        Path result = eArchiveFileStorage.createLocation(location);
        String property = System.getProperty("java.io.tmpdir");
        assertTrue(StringUtils.containsAny(property, result.getFileName().toString()));
    }

    @Test
    public void createLocationWithAbsolutePath(@Injectable Domain domain) throws FileSystemException {
        final String location = System.getProperty("java.io.tmpdir");
        Path path = eArchiveFileStorage.createLocation(location);
        Assert.assertNotNull(path);
        assertTrue(Files.exists(path));
    }

    @Test
    public void getStorageDirectory(@Injectable Domain domain) {
        Assert.assertNull(eArchiveFileStorage.getStorageDirectory());
    }

}