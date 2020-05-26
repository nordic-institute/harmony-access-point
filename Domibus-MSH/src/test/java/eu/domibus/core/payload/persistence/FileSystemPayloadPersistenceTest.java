package eu.domibus.core.payload.persistence;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.payload.encryption.PayloadEncryptionService;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorage;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
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
 * @since 4.1.1
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
    protected PayloadPersistenceHelper payloadPersistenceHelper;

    @Injectable
    protected PayloadEncryptionService encryptionService;

    @Tested
    FileSystemPayloadPersistence fileSystemPayloadPersistence;

    @Test
    public void testStoreIncomingPayload(@Injectable PartInfo partInfo,
                                         @Injectable UserMessage userMessage,
                                         @Injectable PayloadFileStorage currentStorage,
                                         @Injectable LegConfiguration legConfiguration) throws IOException {

        new Expectations(fileSystemPayloadPersistence) {{

            partInfo.getFileName();
            result = null;

            partInfo.getLength();
            result = 4;

            storageProvider.getCurrentStorage();
            result = currentStorage;

            payloadPersistenceHelper.isPayloadEncryptionActive(userMessage);
            result = true;

            fileSystemPayloadPersistence.saveIncomingPayloadToDisk(partInfo, currentStorage, true);
        }};

        fileSystemPayloadPersistence.storeIncomingPayload(partInfo, userMessage, legConfiguration);

        new FullVerifications(fileSystemPayloadPersistence) {{
            fileSystemPayloadPersistence.saveIncomingPayloadToDisk(partInfo, currentStorage, true);

            payloadPersistenceHelper.validatePayloadSize(legConfiguration, anyLong);
        }};
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
    public void testStoreOutgoingPayload(@Injectable PartInfo partInfo,
                                         @Injectable UserMessage userMessage,
                                         @Injectable PayloadFileStorage currentStorage,
                                         @Injectable LegConfiguration legConfiguration,
                                         @Injectable String backendName) throws IOException, EbMS3Exception {

        new Expectations(fileSystemPayloadPersistence) {{
            userMessage.isUserMessageFragment();
            result = false;

            storageProvider.getCurrentStorage();
            result = currentStorage;

            fileSystemPayloadPersistence.saveOutgoingPayloadToDisk(partInfo, userMessage, legConfiguration, currentStorage, backendName);
        }};

        fileSystemPayloadPersistence.storeOutgoingPayload(partInfo, userMessage, legConfiguration, backendName);

        new Verifications() {{
            fileSystemPayloadPersistence.saveOutgoingPayloadToDisk(partInfo, userMessage, legConfiguration, currentStorage, backendName);
        }};
    }

    @Test
    public void testSaveOutgoingPayloadToDisk(@Injectable PartInfo partInfo,
                                              @Injectable UserMessage userMessage,
                                              @Injectable PayloadFileStorage currentStorage,
                                              @Injectable LegConfiguration legConfiguration,
                                              @Injectable String backendName,
                                              @Injectable InputStream inputStream,
                                              @Mocked File file,
                                              @Mocked UUID uuid
    ) throws IOException, EbMS3Exception {

        final String myfile = "myfile";
        final int length = 123;
        final String myFilePath = "myFilePath";

        new Expectations(fileSystemPayloadPersistence) {{
            partInfo.getPayloadDatahandler().getInputStream();
            result = inputStream;

            partInfo.getFileName();
            result = myfile;

            new File((File) any, anyString);
            result = file;

            file.getAbsolutePath();
            result = myFilePath;

            payloadPersistenceHelper.isPayloadEncryptionActive(userMessage);
            result = false;

            fileSystemPayloadPersistence.saveOutgoingFileToDisk(file, partInfo, inputStream, userMessage, legConfiguration, Boolean.FALSE);
            result = length;
        }};

        fileSystemPayloadPersistence.saveOutgoingPayloadToDisk(partInfo, userMessage, legConfiguration, currentStorage, backendName);


        new Verifications() {{
            backendNotificationService.notifyPayloadSubmitted(userMessage, myfile, partInfo, backendName);
            backendNotificationService.notifyPayloadProcessed(userMessage, myfile, partInfo, backendName);

            partInfo.setLength(length);
            partInfo.setFileName(myFilePath);
            partInfo.setEncrypted(false);
        }};
    }
}