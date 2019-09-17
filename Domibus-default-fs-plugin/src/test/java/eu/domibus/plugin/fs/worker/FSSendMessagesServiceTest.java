package eu.domibus.plugin.fs.worker;


import eu.domibus.ext.services.AuthenticationExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.fs.BackendFSImpl;
import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.fs.FSTestHelper;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.jms.Queue;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno, Catalin Enache
 */
@RunWith(JMockit.class)
public class FSSendMessagesServiceTest {

    @Tested
    private FSSendMessagesService instance;

    @Injectable
    private FSPluginProperties fsPluginProperties;

    @Injectable
    private BackendFSImpl backendFSPlugin;

    @Injectable
    private FSFilesManager fsFilesManager;

    @Injectable
    private AuthenticationExtService authenticationExtService;

    @Injectable
    private DomibusConfigurationExtService domibusConfigurationExtService;

    @Injectable
    private FSDomainService fsMultiTenancyService;

    @Injectable
    private JMSExtService jmsExtService;

    @Injectable
    @Qualifier("fsPluginSendQueue")
    private Queue fsPluginSendQueue;

    @Tested
    @Injectable
    private FSProcessFileService fsProcessFileService;

    private FileObject rootDir;
    private FileObject outgoingFolder;
    private FileObject contentFile;
    private FileObject metadataFile;

    private UserMessage metadata;

    @Before
    public void setUp() throws IOException, JAXBException {
        String location = "ram:///FSSendMessagesServiceTest";

        FileSystemManager fsManager = VFS.getManager();
        rootDir = fsManager.resolveFile(location);
        rootDir.createFolder();

        outgoingFolder = rootDir.resolveFile(FSFilesManager.OUTGOING_FOLDER);
        outgoingFolder.createFolder();

        metadata = FSTestHelper.getUserMessage(this.getClass(), "testSendMessages_metadata.xml");

        try (InputStream testMetadata = FSTestHelper.getTestResource(this.getClass(), "testSendMessages_metadata.xml")) {
            metadataFile = outgoingFolder.resolveFile("metadata.xml");
            metadataFile.createFile();
            FileContent metadataFileContent = metadataFile.getContent();
            IOUtils.copy(testMetadata, metadataFileContent.getOutputStream());
            metadataFile.close();
        }

        try (InputStream testContent = FSTestHelper.getTestResource(this.getClass(), "testSendMessages_content.xml")) {
            contentFile = outgoingFolder.resolveFile("content.xml");
            contentFile.createFile();
            FileContent contentFileContent = contentFile.getContent();
            IOUtils.copy(testContent, contentFileContent.getOutputStream());
            contentFile.close();
        }
    }

    @After
    public void tearDown() throws FileSystemException {
        rootDir.close();
        outgoingFolder.close();
    }

    @Test
    public void test_SendMessages_Root_Domain1() {
        final String domain0 = FSSendMessagesService.DEFAULT_DOMAIN;
        final String domain1 = "DOMAIN1";
        new Expectations(instance) {{
            domibusConfigurationExtService.isSecuredLoginRequired();
            result = true;

            fsPluginProperties.getDomains();
            result = Arrays.asList(domain0, domain1);

            fsMultiTenancyService.verifyDomainExists(domain0);
            result = true;

            fsMultiTenancyService.verifyDomainExists(domain1);
            result = true;
        }};

        //tested method
        instance.sendMessages();

        new FullVerifications(instance) {{
            instance.sendMessages(domain0);
            times = 1;
            instance.sendMessages(domain1);
            times = 1;
        }};
    }

    @Test
    public void testSendMessages_RootDomain_NoMultitenancy() throws MessagingProcessingException, FileSystemException, FSSetUpException {
        final String domain = null; //root
        new Expectations(1, instance) {{
            domibusConfigurationExtService.isSecuredLoginRequired();
            result = false;

            fsFilesManager.setUpFileSystem(domain);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outgoingFolder;

            fsFilesManager.findAllDescendantFiles(outgoingFolder);
            result = new FileObject[]{metadataFile, contentFile};

            instance.canReadFileSafely((FileObject) any, anyString);
            result = true;
        }};

        //tested method
        instance.sendMessages(domain);

        new VerificationsInOrder(1) {{
            FileObject fileActual;
            instance.enqueueProcessableFile(fileActual = withCapture());
            Assert.assertEquals(contentFile, fileActual);
        }};
    }

    @Test
    public void test_SendMessages_RootDomain_Multitenancy() throws FileSystemException, FSSetUpException {
        final String domainDefault = FSSendMessagesService.DEFAULT_DOMAIN;
        new Expectations(1, instance) {{
            domibusConfigurationExtService.isSecuredLoginRequired();
            result = true;

            fsPluginProperties.getAuthenticationUser(domainDefault);
            result = "user1";

            fsPluginProperties.getAuthenticationPassword(domainDefault);
            result = "pass1";

            fsFilesManager.setUpFileSystem(domainDefault);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outgoingFolder;

            fsFilesManager.findAllDescendantFiles(outgoingFolder);
            result = new FileObject[]{metadataFile, contentFile};

            instance.canReadFileSafely((FileObject) any, anyString);
            result = true;
        }};

        //tested method
        instance.sendMessages(domainDefault);

        new VerificationsInOrder(1) {{
            authenticationExtService.basicAuthenticate(anyString, anyString);

            FileObject fileActual;
            instance.enqueueProcessableFile(fileActual = withCapture());
            Assert.assertEquals(contentFile, fileActual);
        }};
    }

    @Test
    public void testSendMessages_Domain1() throws MessagingProcessingException, FileSystemException {
        final String domain1 = "DOMAIN1";
        new Expectations(1, instance) {{
            domibusConfigurationExtService.isSecuredLoginRequired();
            result = true;

            fsFilesManager.setUpFileSystem(domain1);
            result = rootDir;

            fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
            result = outgoingFolder;

            fsFilesManager.findAllDescendantFiles(outgoingFolder);
            result = new FileObject[]{metadataFile, contentFile};

            fsPluginProperties.getAuthenticationUser(anyString);
            result = "user1";

            fsPluginProperties.getAuthenticationPassword(anyString);
            result = "pass1";

            instance.canReadFileSafely((FileObject) any, anyString);
            result = true;
        }};

        instance.sendMessages(domain1);

        new Verifications() {{
            authenticationExtService.basicAuthenticate(anyString, anyString);

            FileObject fileActual;
            instance.enqueueProcessableFile(fileActual = withCapture());
            Assert.assertEquals(contentFile, fileActual);
        }};
    }

    @Test
    public void testSendMessages_Domain1_BadConfiguration() throws MessagingProcessingException, FileSystemException, FSSetUpException {
        final String domain1 = "DOMAIN1";
        new Expectations(1, instance) {{
            domibusConfigurationExtService.isSecuredLoginRequired();
            result = true;

            fsPluginProperties.getAuthenticationUser(anyString);
            result = "user1";

            fsPluginProperties.getAuthenticationPassword(anyString);
            result = "pass1";

            fsFilesManager.setUpFileSystem("DOMAIN1");
            result = new FSSetUpException("Test-forced exception");
        }};

        instance.sendMessages(domain1);

        new Verifications() {{
            authenticationExtService.basicAuthenticate(anyString, anyString);

            instance.enqueueProcessableFile((FileObject) any);
            maxTimes = 0;
        }};
    }

    @Test
    public void testHandleSendFailedMessage() throws FileSystemException, FSSetUpException, IOException {
        final String domain = null; //root
        final String errorMessage = "mock error";
        final FileObject processableFile = metadataFile;
        new Expectations(1, instance) {{
            fsFilesManager.setUpFileSystem(domain);
            result = rootDir;

            fsPluginProperties.isFailedActionArchive(domain);
            result = true;
        }};

        instance.handleSendFailedMessage(processableFile, domain, errorMessage);

        new Verifications() {{
            fsFilesManager.createFile((FileObject) any, anyString, anyString);
        }};
    }

    @Test
    public void testCanReadFileSafely() {
        String domain = "domain1";
        new Expectations(1, instance) {{
            instance.checkSizeChangedRecently(contentFile, domain);
            result = false;
            instance.checkTimestampChangedRecently(contentFile, domain);
            result = false;
            instance.checkHasWriteLock(contentFile);
            result = false;
        }};

        //tested method
        boolean actualRes = instance.canReadFileSafely(contentFile, domain);

        Assert.assertEquals(true, actualRes);
    }

    @Test
    public void testCanReadFileSafelyFalse() {
        String domain = "domain1";
        new Expectations(1, instance) {{
            instance.checkSizeChangedRecently(contentFile, domain);
            result = false;
            instance.checkTimestampChangedRecently(contentFile, domain);
            result = true;
        }};

        //tested method
        boolean actualRes = instance.canReadFileSafely(contentFile, domain);

        Assert.assertEquals(false, actualRes);
    }

    @Test
    public void testCheckSizeChangedRecently() throws InterruptedException {
        final String domain = "default";
        new Expectations(1, instance) {{
            fsPluginProperties.getSendDelay(domain);
            result = 200;
        }};

        //tested method
        boolean actualRes = instance.checkSizeChangedRecently(contentFile, domain);
        Assert.assertEquals(true, actualRes);
        Thread.sleep(100);
        boolean actualRes2 = instance.checkSizeChangedRecently(contentFile, domain);
        Assert.assertEquals(true, actualRes2);
        Thread.sleep(400);
        boolean actualRes3 = instance.checkSizeChangedRecently(contentFile, domain);
        Assert.assertEquals(false, actualRes3);
    }

    @Test
    public void testCheckTimestampChangedRecently() throws InterruptedException {
        final String domain = "default";
        new Expectations(1, instance) {{
            fsPluginProperties.getSendDelay(domain);
            result = 200;
        }};

        //tested method
        boolean actualRes = instance.checkTimestampChangedRecently(contentFile, domain);
        Assert.assertEquals(true, actualRes);
        Thread.sleep(100);
        boolean actualRes2 = instance.checkTimestampChangedRecently(contentFile, domain);
        Assert.assertEquals(true, actualRes2);
        Thread.sleep(400);
        boolean actualRes3 = instance.checkTimestampChangedRecently(contentFile, domain);
        Assert.assertEquals(false, actualRes3);
    }

    @Test
    public void testCheckHasWriteLock() throws InterruptedException, FileSystemException {
        final String domain = "default";
        //tested method
        boolean actualRes = instance.checkHasWriteLock(contentFile);
        Assert.assertEquals(true, actualRes);
    }

    @Test
    public void testClearObservedFiles() throws InterruptedException {
        final String domain = "default";

        new Expectations(1, instance) {{
            fsPluginProperties.getSendDelay(domain);
            result = 100;
            fsPluginProperties.getSendWorkerInterval(domain);
            result = 300;
        }};
        instance.checkSizeChangedRecently(contentFile, domain);

        //tested method
        Assert.assertEquals(1, instance.observedFilesInfo.size());
        instance.clearObservedFiles(domain);
        Assert.assertEquals(1, instance.observedFilesInfo.size());
        Thread.sleep(800);
        instance.clearObservedFiles(domain);
        Assert.assertEquals(0, instance.observedFilesInfo.size());
    }
}
