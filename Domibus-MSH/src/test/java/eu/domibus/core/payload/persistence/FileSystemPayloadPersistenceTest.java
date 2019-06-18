package eu.domibus.core.payload.persistence;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.core.encryption.EncryptionService;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorage;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class FileSystemPayloadPersistenceTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FileSystemPayloadPersistenceTest.class);

    @Injectable
    protected PayloadFileStorageProvider storageProvider;

    @Injectable
    protected BackendNotificationService backendNotificationService;

    @Injectable
    protected CompressionService compressionService;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected EncryptionService encryptionService;

    @Tested
    FileSystemPayloadPersistence fileSystemPayloadPersistence;

    @Test
    public void storeIncomingPayload() {
    }

    @Test
    public void testSaveIncomingPayloadToDisk(@Injectable PartInfo partInfo,
                                              @Injectable PayloadFileStorage storage,
                                              @Mocked File file,
                                              @Injectable InputStream inputStream,
                                              @Mocked UUID uuid) throws IOException {

        String path = "/home/invoice.pdf";
        new Expectations(fileSystemPayloadPersistence) {{
            new File((File) any, anyString);
            result = file;

            file.getAbsolutePath();
            result = path;

            partInfo.getPayloadDatahandler().getInputStream();
            result = inputStream;

            fileSystemPayloadPersistence.saveIncomingFileToDisk(file, inputStream, false);
        }};

        fileSystemPayloadPersistence.saveIncomingPayloadToDisk(partInfo, storage, false);

        new Verifications() {{
            fileSystemPayloadPersistence.saveIncomingFileToDisk(file, inputStream, false);
            times = 1;

            partInfo.setFileName(path);
        }};
    }

    @Test
    public void saveIncomingFileToDisk() {

    }

    @Test
    public void storeOutgoingPayload() {
    }

    @Test
    public void saveOutgoingPayloadToDisk() {
    }

    @Test
    public void saveOutgoingFileToDisk() {
    }
}