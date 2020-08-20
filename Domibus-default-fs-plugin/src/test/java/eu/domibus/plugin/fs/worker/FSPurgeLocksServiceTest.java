package eu.domibus.plugin.fs.worker;

import eu.domibus.plugin.fs.FSFileNameHelper;
import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
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

    @Injectable
    private FSPluginProperties fsPluginProperties;

    private FileObject rootDir;
    private FileObject outFolder;
    private FileObject lockFile1;
    private String dataFileName1 = "invoice.pdf";
    private String lockFileName1 = "invoice.pdf" + LOCK_SUFFIX;

    @Before
    public void setUp() throws IOException {
        String location = "ram:///FSPurgeLocksServiceTest";

        FileSystemManager fsManager = VFS.getManager();
        rootDir = fsManager.resolveFile(location);
        rootDir.createFolder();

        outFolder = rootDir.resolveFile(FSFilesManager.OUTGOING_FOLDER);
        outFolder.createFolder();

        lockFile1 = outFolder.resolveFile(lockFileName1);
        lockFile1.createFile();
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
    public void testPurgeForDomain_OldAndOrphan() throws FileSystemException, FSSetUpException {
        Integer expiredLimit = 600;
        String domain = FSSendMessagesService.DEFAULT_DOMAIN;

        new Expectations(1, instance) {{
            fsPluginProperties.getLocksPurgeExpired(domain);
            result = expiredLimit;

            fsFilesManager.setUpFileSystem(domain);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outFolder;

            fsFilesManager.findAllDescendantFiles(outFolder);
            result = new FileObject[]{lockFile1};

            fsFileNameHelper.isLockFile(lockFileName1);
            result = true;

            fsFilesManager.isFileOlderThan(lockFile1, expiredLimit);
            result = true;

            fsFileNameHelper.stripLockSuffix(outFolder.getName().getRelativeName(lockFile1.getName()));
            result = dataFileName1;

            fsFilesManager.fileExists(outFolder, dataFileName1);
            result = false;
        }};

        instance.purgeForDomain(domain);

        new Verifications(1) {{
            fsFilesManager.deleteFile(lockFile1);
        }};
    }

    @Test
    public void testPurgeForDomain_OldAndNotOrphan() throws FileSystemException, FSSetUpException {
        Integer expiredLimit = 600;
        String domain = FSSendMessagesService.DEFAULT_DOMAIN;

        new Expectations(1, instance) {{
            fsPluginProperties.getLocksPurgeExpired(domain);
            result = expiredLimit;

            fsFilesManager.setUpFileSystem(domain);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outFolder;

            fsFilesManager.findAllDescendantFiles(outFolder);
            result = new FileObject[]{lockFile1};

            fsFileNameHelper.isLockFile(lockFileName1);
            result = true;

            fsFilesManager.isFileOlderThan(lockFile1, expiredLimit);
            result = true;

            fsFileNameHelper.stripLockSuffix(outFolder.getName().getRelativeName(lockFile1.getName()));
            result = dataFileName1;

            fsFilesManager.fileExists(outFolder, dataFileName1);
            result = true;
        }};

        instance.purgeForDomain(domain);

        new Verifications() {{
            fsFilesManager.deleteFile(lockFile1);
            times = 0;
        }};
    }

    @Test
    public void testPurgeForDomain_NotOld() throws FileSystemException, FSSetUpException {
        Integer expiredLimit = 600;
        String domain = FSSendMessagesService.DEFAULT_DOMAIN;

        new Expectations(1, instance) {{
            fsPluginProperties.getLocksPurgeExpired(domain);
            result = expiredLimit;

            fsFilesManager.setUpFileSystem(domain);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outFolder;

            fsFilesManager.findAllDescendantFiles(outFolder);
            result = new FileObject[]{lockFile1};

            fsFileNameHelper.isLockFile(lockFileName1);
            result = true;

            fsFilesManager.isFileOlderThan(lockFile1, expiredLimit);
            result = false;
        }};

        instance.purgeForDomain(domain);

        new Verifications() {{
            fsFilesManager.deleteFile(lockFile1);
            times = 0;
            fsFileNameHelper.stripLockSuffix(outFolder.getName().getRelativeName(lockFile1.getName()));
            times = 0;
            fsFilesManager.fileExists(outFolder, dataFileName1);
            times = 0;
        }};
    }

    @Test
    public void testPurgeForDomain_NotLock() throws FileSystemException, FSSetUpException {
        Integer expiredLimit = 600;
        String domain = FSSendMessagesService.DEFAULT_DOMAIN;

        new Expectations(1, instance) {{
            fsPluginProperties.getLocksPurgeExpired(domain);
            result = expiredLimit;

            fsFilesManager.setUpFileSystem(domain);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outFolder;

            fsFilesManager.findAllDescendantFiles(outFolder);
            result = new FileObject[]{lockFile1};

            fsFileNameHelper.isLockFile(lockFileName1);
            result = false;
        }};

        instance.purgeForDomain(domain);

        new Verifications() {{
            fsFilesManager.isFileOlderThan(lockFile1, expiredLimit);
            times = 0;
            fsFilesManager.deleteFile(lockFile1);
            times = 0;
            fsFileNameHelper.stripLockSuffix(outFolder.getName().getRelativeName(lockFile1.getName()));
            times = 0;
            fsFilesManager.fileExists(outFolder, dataFileName1);
            times = 0;
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
            fsFilesManager.deleteFile(withAny(lockFile1));
            maxTimes = 0;
        }};
    }

}
