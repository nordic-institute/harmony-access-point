package eu.domibus.core.earchive.eark;

import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.earchive.EArchiveBatchDTO;
import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.earchive.BatchEArchiveDTOBuilder;
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
import org.roda_project.commons_ip2.model.IPConstants;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static eu.domibus.core.earchive.eark.EArchivingFileService.SOAP_ENVELOPE_XML;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.junit.Assert.assertEquals;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "TestMethodWithIncorrectSignature"})
public class FileSystemEArchivePersistenceIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FileSystemEArchivePersistenceIT.class);
    public static final String MESSAGE_ATTACHMENT_MSG1 = "message.attachment.txt";
    public static final String MESSAGE_ATTACHMENT_MSG2 = "message.attachment.xml";

    @Injectable
    protected EArchiveFileStorageProvider storageProvider;

    @Injectable
    protected DomibusVersionService domibusVersionService;

    @Injectable
    protected EArchivingFileService eArchivingFileService;

    @Injectable
    protected EARKSIPBuilderService eArkSipBuilderService;

    @Tested
    private FileSystemEArchivePersistence fileSystemEArchivePersistence;

    private File temp;

    private BatchEArchiveDTO batchEArchiveDTO;
    private String batchId;
    private String msg1;
    private String msg2;
    private long entityId1;
    private long entityId2;
    private List<UserMessageDTO> userMessageEntityIds;

    @Before
    public void setUp() throws Exception {
        batchId = UUID.randomUUID().toString();
        msg1 = UUID.randomUUID().toString();
        msg2 = UUID.randomUUID().toString();

        batchEArchiveDTO = new BatchEArchiveDTOBuilder()
                .batchId(batchId)
                .messages(asList(msg1, msg2))
                .createBatchEArchiveDTO();
        Random random = new Random();
        entityId1 = random.nextLong();
        entityId2 = random.nextLong();
        userMessageEntityIds = asList(new UserMessageDTO(entityId1, msg1), new UserMessageDTO(entityId2, msg2));

        temp = Files.createTempDirectory("tmpDirPrefix").toFile();
        LOG.info("temp folder created: [{}]", temp.getAbsolutePath());
    }

    /**
     * String filename = "SOAPMessage2.xml";
     * String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
     * <p>
     * SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
     * mshWebserviceTest.invoke(soapMessage);
     *
     * @throws IOException
     */
    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(temp);
        LOG.info("temp folder deleted: [{}]", temp.getAbsolutePath());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void createEArkSipStructure(@Injectable EArchiveFileStorage eArchiveFileStorage) {
        ReflectionTestUtils.setField(fileSystemEArchivePersistence,"eArkSipBuilderService", new EARKSIPBuilderService());

        Map<String, InputStream> messageId1 = new HashMap<>();
        putRaw(messageId1, "test1");
        putFile(messageId1, MESSAGE_ATTACHMENT_MSG1, "attachmentTXT");
        Map<String, InputStream> messageId2 = new HashMap<>();
        putRaw(messageId2, "test2");
        putFile(messageId2, MESSAGE_ATTACHMENT_MSG2, "attachmentXML");

        new Expectations() {{
            domibusVersionService.getArtifactName();
            result = "getArtifactName";

            domibusVersionService.getDisplayVersion();
            result = "getDisplayVersion";

            domibusVersionService.getDisplayVersion();
            result = "getDisplayVersion";

            eArchivingFileService.getBatchFileJson(batchEArchiveDTO);
            result = new ByteArrayInputStream("batch.json content".getBytes(StandardCharsets.UTF_8));

            eArchivingFileService.getArchivingFiles(entityId1);
            result = messageId1;

            eArchivingFileService.getArchivingFiles(entityId2);
            result = messageId2;

            storageProvider.getCurrentStorage();
            result = eArchiveFileStorage;

            eArchiveFileStorage.getStorageDirectory();
            result = temp;
        }};


        fileSystemEArchivePersistence.createEArkSipStructure(batchEArchiveDTO, userMessageEntityIds);

        new FullVerifications() {
        };
        File[] files = temp.listFiles();
        File batchFolder = files[0];
        File representation = batchFolder.listFiles()[1];
        File representation1 = representation.listFiles()[0];
        File data = representation1.listFiles()[0];

        assertEquals(batchId, batchFolder.getName());
        assertEquals(IPConstants.METS_FILE, batchFolder.listFiles()[0].getName());
        assertEquals(IPConstants.REPRESENTATIONS, representation.getName());
        assertEquals(IPConstants.METS_REPRESENTATION_TYPE_PART_1 + "1", representation1.getName());
        assertEquals(IPConstants.DATA, data.getName());

        File[] files1 = data.listFiles();
        for (File file : files1) {
            if (contains(file.getName(), ".json")) {
                assertEquals("batch.json", file.getName());
            }
            if (equalsIgnoreCase(file.getName(), msg1)) {
                List<File> collect = Arrays.stream(file.listFiles()).sorted().collect(Collectors.toList());
                assertEquals(MESSAGE_ATTACHMENT_MSG1, collect.get(0).getName());
                assertEquals(SOAP_ENVELOPE_XML, collect.get(1).getName());
            }
            if (equalsIgnoreCase(file.getName(), msg2)) {
                List<File> collect = Arrays.stream(file.listFiles()).sorted().collect(Collectors.toList());
                assertEquals(MESSAGE_ATTACHMENT_MSG2, collect.get(0).getName());
                assertEquals(SOAP_ENVELOPE_XML, collect.get(1).getName());
            }
        }
    }

    private void putRaw(Map<String, InputStream> messageId1, String test1) {
        putFile(messageId1, SOAP_ENVELOPE_XML, test1);
    }

    private void putFile(Map<String, InputStream> messageId1, String s, String test1) {
        messageId1.put(s, new ByteArrayInputStream(test1.getBytes(StandardCharsets.UTF_8)));
    }
}