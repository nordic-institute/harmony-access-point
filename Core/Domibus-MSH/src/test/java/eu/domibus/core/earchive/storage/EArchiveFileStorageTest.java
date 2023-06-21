package eu.domibus.core.earchive.storage;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.util.FileSystemUtil;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STORAGE_LOCATION;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

/**
 * @author François Gautier
 * @since 5.0
 */
public class EArchiveFileStorageTest {

    public static final String LOCATION = "D:\\domibus_tomcat\\domibus_blue\\domibus\\payload_storage";

    DomibusPropertyProvider domibusPropertyProvider;

    FileSystemUtil fileSystemUtil;

    EArchiveFileStorage eArchiveFileStorage;

    @Injectable Domain domain;

    @Before
    public void recordExpectationsForPostConstruct() {
        domibusPropertyProvider = mock(DomibusPropertyProvider.class);
        fileSystemUtil = mock(FileSystemUtil.class);

        eArchiveFileStorage = new EArchiveFileStorage(domain);
        ReflectionTestUtils.setField(eArchiveFileStorage, "domibusPropertyProvider", domibusPropertyProvider);
        ReflectionTestUtils.setField(eArchiveFileStorage, "fileSystemUtil", fileSystemUtil);
    }

    @Test
    public void init(@Injectable Path path) throws FileSystemException {
        Mockito.when(domibusPropertyProvider.getProperty(Mockito.any(Domain.class), eq(DOMIBUS_EARCHIVE_STORAGE_LOCATION))).thenReturn(LOCATION);
        Mockito.when(fileSystemUtil.createLocation(eq(LOCATION))).thenReturn(path);

        eArchiveFileStorage.init();

        new Verifications() {{
            path.toFile();
            times = 1;
        }};
    }

    @Test(expected = ConfigurationException.class)
    public void init_error(@Injectable Domain domain) {
        Mockito.when(domibusPropertyProvider.getProperty(Mockito.any(Domain.class), eq(DOMIBUS_EARCHIVE_STORAGE_LOCATION))).thenReturn(null);

        eArchiveFileStorage.init();
    }

    @Test(expected = ConfigurationException.class)
    public void init_error2(@Injectable Domain domain) throws FileSystemException {
        Mockito.when(domibusPropertyProvider.getProperty(Mockito.any(Domain.class), eq(DOMIBUS_EARCHIVE_STORAGE_LOCATION))).thenReturn(LOCATION);
        Mockito.when(fileSystemUtil.createLocation(eq(LOCATION))).thenReturn(null);

        eArchiveFileStorage.init();
    }

    @Test(expected = ConfigurationException.class)
    public void getStorageDirectory(@Injectable Domain domain) {
        eArchiveFileStorage.getStorageDirectory();
    }

}
