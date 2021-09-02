package eu.domibus.core.earchive;

import eu.domibus.core.earchive.storage.EArchiveFileStorage;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

/**
 * @author François Gautier
 * @since 5.0
 */
public class FileSystemEArchivePersistenceTest {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FileSystemEArchivePersistenceTest.class);

    @Injectable
    protected EArchiveFileStorageProvider storageProvider;

    @Tested
    private FileSystemEArchivePersistence fileSystemEArchivePersistence;
    private File temp;

    @Before
    public void setUp() throws Exception {
        temp = Files.createTempDirectory("tmpDirPrefix").toFile();
        LOG.info("temp folder created: [{}]", temp.getAbsolutePath());
    }

    @After
    public void tearDown() {
        LOG.info("temp folder deleted: [{}]", temp.getAbsolutePath());
        deleteDirectory(temp);
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    @Test
    public void createEArkSipStructure2(@Injectable EArchiveFileStorage eArchiveFileStorage) {
        new Expectations() {{
            storageProvider.getCurrentStorage();
            result = eArchiveFileStorage;

            eArchiveFileStorage.getStorageDirectory();
            result = temp;
        }};

        fileSystemEArchivePersistence.createEArkSipStructure(UUID.randomUUID().toString());
    }
}