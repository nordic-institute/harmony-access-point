package eu.domibus.core.earchive.storage;

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

import java.nio.file.Files;
import java.nio.file.Path;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STORAGE_LOCATION;
import static org.junit.Assert.fail;

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
    public void init(@Injectable Domain domain, @Injectable Path path) {

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
    public void init_error2(@Injectable Domain domain) {


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
    public void createLocationWithRelativePath(@Injectable Domain domain) {

        final String location = "..\\domibus_blue\\domibus\\earchiving_storage";
        try {
            eArchiveFileStorage.createLocation(location);
            fail();
        } catch (DomibusCoreException ex) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, ex.getError());
            Assert.assertEquals("[DOM_001]:Could not find file with URI \"..\\domibus_blue\\domibus\\earchiving_storage\" because it is a relative path, and no base URI was provided.", ex.getMessage());
        }
    }

    @Test
    public void createLocationWithAbsolutePath(@Injectable Domain domain) {
        final String location = System.getProperty("java.io.tmpdir");
        Path path = eArchiveFileStorage.createLocation(location);
        Assert.assertNotNull(path);
        Assert.assertTrue(Files.exists(path));
    }

    @Test
    public void getStorageDirectory(@Injectable Domain domain) {
        Assert.assertNull(eArchiveFileStorage.getStorageDirectory());
    }

}