package eu.domibus.core.earchive;

import eu.domibus.core.earchive.storage.EArchiveFileStorage;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.core.property.DomibusVersionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "TestMethodWithIncorrectSignature"})
public class FileSystemEArchivePersistenceIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FileSystemEArchivePersistenceIT.class);

    @Injectable
    protected EArchiveFileStorageProvider storageProvider;

    @Injectable
    protected DomibusVersionService domibusVersionService;

    @Injectable
    protected EArchivingService eArchivingService;

    @Tested
    private FileSystemEArchivePersistence fileSystemEArchivePersistence;

    private File temp;

    private BatchEArchiveDTO batchEArchiveDTO;

    @Before
    public void setUp() throws Exception {
        batchEArchiveDTO = new BatchEArchiveDTO();
        batchEArchiveDTO.setBatchId(UUID.randomUUID().toString());
        batchEArchiveDTO.setMessages(Arrays.asList(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
        temp = Files.createTempDirectory("tmpDirPrefix").toFile();
        LOG.info("temp folder created: [{}]", temp.getAbsolutePath());
    }

    /**
     *         String filename = "SOAPMessage2.xml";
     *         String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
     *
     *         SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
     *         mshWebserviceTest.invoke(soapMessage);
     * @throws IOException
     */
    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(temp);
//        Desktop.getDesktop().open(temp);
        LOG.info("temp folder deleted: [{}]", temp.getAbsolutePath());
    }

    @Test
    public void createEArkSipStructure(@Injectable EArchiveFileStorage eArchiveFileStorage) {
        Map<String, InputStream> messageId1 = new HashMap<>();
        putRaw(messageId1, "test1");
        putFile(messageId1, "message.attachment.txt", "attachmentTXT");
        Map<String, InputStream> messageId2 = new HashMap<>();
        putRaw(messageId2, "test2");
        putFile(messageId2, "message.attachment.xml", "attachmentXML");

        new Expectations() {{
            domibusVersionService.getArtifactName();
            result = "getArtifactName";

            domibusVersionService.getDisplayVersion();
            result = "getDisplayVersion";

            domibusVersionService.getDisplayVersion();
            result = "getDisplayVersion";

            eArchivingService.getBatchFileJson(batchEArchiveDTO);
            result = new ByteArrayInputStream("batch.json content".getBytes(StandardCharsets.UTF_8));

            eArchivingService.getArchivingFiles(batchEArchiveDTO.getMessages().get(0));
            result = messageId1;

            eArchivingService.getArchivingFiles(batchEArchiveDTO.getMessages().get(1));
            result = messageId2;

            storageProvider.getCurrentStorage();
            result = eArchiveFileStorage;

            eArchiveFileStorage.getStorageDirectory();
            result = temp;
        }};


        fileSystemEArchivePersistence.createEArkSipStructure(batchEArchiveDTO);

        new FullVerifications() {
        };
        // TODO: François Gautier 08-09-21 add assertions

    }

    private void putRaw(Map<String, InputStream> messageId1, String test1) {
        putFile(messageId1, "soap.envelope.xml", test1);
    }

    private void putFile(Map<String, InputStream> messageId1, String s, String test1) {
        messageId1.put(s, new ByteArrayInputStream(test1.getBytes(StandardCharsets.UTF_8)));
    }
}