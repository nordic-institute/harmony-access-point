package eu.domibus.core.earchive.storage;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.util.FileSystemUtil;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Assert;
import org.junit.Test;

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
    @Injectable
    FileSystemUtil fileSystemUtil;

    @Tested
    EArchiveFileStorage eArchiveFileStorage;

    @Test
    public void init(@Injectable Domain domain, @Injectable Path path) throws FileSystemException {

        new Expectations() {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_ACTIVE);
            result = true;

            domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_STORAGE_LOCATION);
            result = LOCATION;

            fileSystemUtil.createLocation(LOCATION);
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

        new Expectations() {{
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


        new Expectations() {{
            domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_ACTIVE);
            result = true;

            domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_STORAGE_LOCATION);
            result = LOCATION;

            fileSystemUtil.createLocation(LOCATION);
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
    public void getStorageDirectory(@Injectable Domain domain) {
        Assert.assertNull(eArchiveFileStorage.getStorageDirectory());
    }

}