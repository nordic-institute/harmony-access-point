package eu.domibus.core.util;

import eu.domibus.api.exceptions.DomibusCoreException;
import mockit.Tested;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/**
 * @author François Gautier
 * @since 5.0
 */
public class FileSystemUtilTest {

    @Tested
    private FileSystemUtil fileSystemUtil;

    @Test(expected = DomibusCoreException.class)
    public void createLocationWithRelativePath_returnTempFile() throws FileSystemException {
        final String location = "..\\domibus_blue\\domibus\\earchiving_storage";
        fileSystemUtil.createLocation(location);
    }

    @Test
    public void createLocationWithAbsolutePath() throws FileSystemException {
        final String location = System.getProperty("java.io.tmpdir");
        Path path = fileSystemUtil.createLocation(location);
        Assert.assertNotNull(path);
        assertTrue(Files.exists(path));
    }

}