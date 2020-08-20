package eu.domibus.plugin.fs.worker;

import eu.domibus.plugin.fs.FSFileNameHelper;
import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static eu.domibus.plugin.fs.FSFileNameHelper.LOCK_SUFFIX;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@RunWith(JMockit.class)
public class FSPurgeLocksServiceTest {

    @Tested
    private FSPurgeLocksService instance;

    @Injectable
    private FSDomainService fsMultiTenancyService;

    @Injectable
    private FSFilesManager fsFilesManager;

    @Injectable
    private FSFileNameHelper fsFileNameHelper;

    private FileObject rootDir;
    private FileObject outFolder;
    private FileObject lockFile;
    private String dataFileName = "invoice.pdf";
    private String lockFileName = "invoice.pdf" + LOCK_SUFFIX;

    @Before
    public void setUp() throws IOException {
        String location = "ram:///FSPurgeLocksServiceTest";

        FileSystemManager fsManager = VFS.getManager();
        rootDir = fsManager.resolveFile(location);
        rootDir.createFolder();

        outFolder = rootDir.resolveFile(FSFilesManager.OUTGOING_FOLDER);
        outFolder.createFolder();

        lockFile = outFolder.resolveFile(lockFileName);
        lockFile.createFile();
    }

    @After
    public void tearDown() throws FileSystemException {
        rootDir.close();
        outFolder.close();
    }

    @Test
    public void testPurge() {
        final List<String> domains = new ArrayList<>();
        domains.add("DOMAIN1");
        domains.add("DOMAIN2");

        new Expectations(1, instance) {{
            fsMultiTenancyService.getDomainsToProcess();
            result = domains;

            fsMultiTenancyService.verifyDomainExists("DOMAIN1");
            result = true;

            fsMultiTenancyService.verifyDomainExists("DOMAIN2");
            result = true;
        }};

        instance.purge();

        new Verifications() {{
            instance.purgeForDomain("DOMAIN1");
            instance.purgeForDomain("DOMAIN2");
        }};
    }

    @Test
    public void testPurgeForDomain() throws FileSystemException, FSSetUpException {
        new Expectations(1, instance) {{
            fsFilesManager.setUpFileSystem(FSSendMessagesService.DEFAULT_DOMAIN);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outFolder;

            fsFilesManager.findAllDescendantFiles(outFolder);
            result = new FileObject[]{lockFile};

            fsFileNameHelper.isLockFile(lockFileName);
            result = true;

            fsFileNameHelper.stripLockSuffix(outFolder.getName().getRelativeName(lockFile.getName()));
            result = dataFileName;
        }};

        instance.purgeForDomain(FSSendMessagesService.DEFAULT_DOMAIN);

        new VerificationsInOrder(1) {{
            fsFilesManager.deleteFile(lockFile);
        }};
    }

    @Test
    public void testPurgeForDOmain_Domain1_BadConfiguration() throws FileSystemException, FSSetUpException {
        new Expectations(1, instance) {{
            fsFilesManager.setUpFileSystem("DOMAIN1");
            result = new FSSetUpException("Test-forced exception");
        }};

        instance.purgeForDomain("DOMAIN1");

        new Verifications() {{
            fsFilesManager.deleteFile(withAny(lockFile));
            maxTimes = 0;
        }};
    }

}
