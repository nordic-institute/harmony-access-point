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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

    @Before
    public void setUp() throws Exception {
        messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";

        batchEArchiveDTO = new BatchEArchiveDTO();
        batchEArchiveDTO.setBatchId(UUID.randomUUID().toString());
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

    @Test
    public void createEArkSipStructure() {
        fileSystemEArchivePersistence.createEArkSipStructure(batchEArchiveDTO);
        // TODO: François Gautier 08-09-21 add assertions
    }

}