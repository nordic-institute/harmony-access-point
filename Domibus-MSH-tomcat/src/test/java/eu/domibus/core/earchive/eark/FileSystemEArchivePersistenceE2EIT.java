package eu.domibus.core.earchive.eark;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.AbstractIT;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.earchive.BatchEArchiveDTOBuilder;
import eu.domibus.core.earchive.EArchiveBatchUserMessage;
import eu.domibus.core.earchive.storage.EArchiveFileStorageFactory;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.test.common.SoapSampleUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.roda_project.commons_ip2.model.IPConstants;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STORAGE_LOCATION;
import static eu.domibus.core.earchive.eark.EArchivingFileService.SOAP_ENVELOPE_XML;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@Ignore("EDELIVERY-8052 Failing tests must be ignored (FAILS ON BAMBOO)")
public class FileSystemEArchivePersistenceE2EIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FileSystemEArchivePersistenceE2EIT.class);

    @Autowired
    private UserMessageDao userMessageDao;

    @Autowired
    private FileSystemEArchivePersistence fileSystemEArchivePersistence;

    @Autowired
    protected Provider<SOAPMessage> mshWebserviceTest;

    @Autowired
    protected SoapSampleUtil soapSampleUtil;

    @Autowired
    protected EArchiveFileStorageProvider storageProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected EArchiveFileStorageFactory storageFactory;

    private File temp;

    private BatchEArchiveDTO batchEArchiveDTO;

    private String messageId;
    private String batchId;

    @Before
    public void setUp() throws Exception {
        // because we must not use DirtyContext do not use common indentifiers!
        //messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        messageId = UUID.randomUUID().toString() + "@domibus.eu";

        batchId = UUID.randomUUID().toString();
        batchEArchiveDTO = new BatchEArchiveDTOBuilder()
                .batchId(batchId)
                .messageEndId("")
                .messages(singletonList(messageId))
                .createBatchEArchiveDTO();
        temp = Files.createTempDirectory(Paths.get("target"), "tmpDirPrefix").toFile();
        LOG.info("temp folder created: [{}]", temp.getAbsolutePath());

        uploadPmode(SERVICE_PORT);

        String filename = "SOAPMessage2.xml";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        mshWebserviceTest.invoke(soapMessage);

        domibusPropertyProvider.setProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_EARCHIVE_ACTIVE, "true");
        domibusPropertyProvider.setProperty(DOMIBUS_EARCHIVE_ACTIVE, "true");
        domibusPropertyProvider.setProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_EARCHIVE_STORAGE_LOCATION, temp.getAbsolutePath());
        domibusPropertyProvider.setProperty(DOMIBUS_EARCHIVE_STORAGE_LOCATION, temp.getAbsolutePath());

        storageProvider.getCurrentStorage().reset();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(temp);
        LOG.info("temp folder deleted: [{}]", temp.getAbsolutePath());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void createEArkSipStructure() throws IOException {
        UserMessage byMessageId = userMessageDao.findByMessageId(messageId);

        DomibusEARKSIPResult fileObject = fileSystemEArchivePersistence.createEArkSipStructure(batchEArchiveDTO, singletonList(new EArchiveBatchUserMessage(byMessageId.getEntityId(), messageId)));
        try (FileObject batchDirectory = VFS.getManager().resolveFile(fileObject.getDirectory().toUri())) {

            // must have more that one subfolder item
            assertTrue(temp.listFiles().length > 0);
            File batchFolder = getFileItem(batchDirectory.getName().getBaseName(), temp.listFiles(), true);
            assertNotNull(batchFolder);
            File representation = getFileItem("representations", batchFolder.listFiles(), true);
            assertNotNull(representation);
            File representation1 = getFileItem("representation1", representation.listFiles(), true);
            assertNotNull(representation1);
            File data = getFileItem(IPConstants.DATA, representation1.listFiles(), true);
            assertNotNull(data);
            File metsData = getFileItem(IPConstants.METS_FILE, batchFolder.listFiles(), false);
            assertNotNull(metsData);

            assertEquals(batchId, batchFolder.getName());
            assertEquals(IPConstants.REPRESENTATIONS, representation.getName());
            assertEquals(IPConstants.METS_REPRESENTATION_TYPE_PART_1 + "1", representation1.getName());

            File[] messageIDFiles = data.listFiles();
            for (File file : messageIDFiles) {
                if (StringUtils.contains(file.getName(), ".json")) {
                    LOG.info("StringUtils.containsAny(file.getName(), \".json\") : [{}]", file.getName());
                    assertEquals("batch.json", file.getName());
                    BatchEArchiveDTO batchEArchiveDTO = new ObjectMapper().readValue(file, BatchEArchiveDTO.class);
                    assertNotNull(batchEArchiveDTO.getManifestChecksum());
                    assertEquals(byMessageId.getMessageId(), batchEArchiveDTO.getMessages().get(0));
                    assertEquals(batchEArchiveDTO.getBatchId(), batchEArchiveDTO.getBatchId());
                }
                if (StringUtils.equalsIgnoreCase(file.getName(), messageId)) {
                    List<File> collect = Arrays.stream(file.listFiles()).sorted().collect(Collectors.toList());
                    assertEquals("message.attachment", collect.get(0).getName());
                    assertEquals(SOAP_ENVELOPE_XML, collect.get(1).getName());
                }
            }
        }
    }

    public File getFileItem(String name, File[] files, boolean isFolder) {
        Optional<File> optFile = Arrays.stream(files).filter(file -> (isFolder ? file.isDirectory() : file.isFile()) && StringUtils.equals(file.getName(), name)).findFirst();
        return optFile.isPresent() ? optFile.get() : null;

    }
}