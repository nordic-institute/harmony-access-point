package eu.domibus.core.earchive;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.earchive.storage.EArchiveFileStorageFactory;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.test.common.SoapSampleUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.roda_project.commons_ip2.model.IPConstants;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STORAGE_LOCATION;
import static eu.domibus.core.earchive.EArchivingService.SOAP_ENVELOPE_XML;
import static org.junit.Assert.assertEquals;

/**
 * @author François Gautier
 * @since 5.0
 */
public class FileSystemEArchivePersistenceE2EIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FileSystemEArchivePersistenceE2EIT.class);

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
        messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";

        batchEArchiveDTO = new BatchEArchiveDTO();
        batchId = UUID.randomUUID().toString();
        batchEArchiveDTO.setBatchId(batchId);
        batchEArchiveDTO.setMessages(Arrays.asList(messageId));
        temp = Files.createTempDirectory("tmpDirPrefix").toFile();
        LOG.info("temp folder created: [{}]", temp.getAbsolutePath());

        uploadPmode(SERVICE_PORT);

        String filename = "SOAPMessage2.xml";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        mshWebserviceTest.invoke(soapMessage);

        Domain aDefault = domainService.getDomain("default");
        domibusPropertyProvider.setProperty(aDefault, DOMIBUS_EARCHIVE_ACTIVE, "true");
        domibusPropertyProvider.setProperty(DOMIBUS_EARCHIVE_ACTIVE, "true");
        domibusPropertyProvider.setProperty(aDefault, DOMIBUS_EARCHIVE_STORAGE_LOCATION, temp.getAbsolutePath());
        domibusPropertyProvider.setProperty(DOMIBUS_EARCHIVE_STORAGE_LOCATION, temp.getAbsolutePath());

        storageProvider.getCurrentStorage().reset();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(temp);
//        Desktop.getDesktop().open(temp);
        LOG.info("temp folder deleted: [{}]", temp.getAbsolutePath());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void createEArkSipStructure() {
        fileSystemEArchivePersistence.createEArkSipStructure(batchEArchiveDTO);

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

        File[] messageIDFiles = data.listFiles();
        for (File file : messageIDFiles) {
            if (StringUtils.contains(file.getName(), ".json")) {
                LOG.info("StringUtils.containsAny(file.getName(), \".json\") : [{}]", file.getName());
                assertEquals("batch.json", file.getName());
            }
            if (StringUtils.equalsIgnoreCase(file.getName(), messageId)) {
                assertEquals("message.attachment", file.listFiles()[0].getName());
                assertEquals(SOAP_ENVELOPE_XML, file.listFiles()[1].getName());
            }
        }
    }

}