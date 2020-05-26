package eu.domibus.core.payload.persistence;

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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class FileSystemPayloadPersistence implements PayloadPersistence {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FileSystemPayloadPersistence.class);

    public static final String PAYLOAD_EXTENSION = ".payload";

    @Autowired
    protected PayloadFileStorageProvider storageProvider;

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Autowired
    protected CompressionService compressionService;

    @Autowired
    protected PayloadPersistenceHelper payloadPersistenceHelper;

    @Autowired
    protected PayloadEncryptionService encryptionService;

    @Override
    public void storeIncomingPayload(PartInfo partInfo, UserMessage userMessage, LegConfiguration legConfiguration) throws IOException {
        if (StringUtils.isBlank(partInfo.getFileName())) {
            PayloadFileStorage currentStorage = storageProvider.getCurrentStorage();
            final Boolean encryptionActive = payloadPersistenceHelper.isPayloadEncryptionActive(userMessage);
            saveIncomingPayloadToDisk(partInfo, currentStorage, encryptionActive);
        } else {
            LOG.debug("Incoming payload [{}] is already saved on file disk under [{}]", partInfo.getHref(), partInfo.getFileName());
        }

        payloadPersistenceHelper.validatePayloadSize(legConfiguration, partInfo.getLength());
    }

    protected void saveIncomingPayloadToDisk(PartInfo partInfo, PayloadFileStorage currentStorage, final Boolean encryptionActive) throws IOException {
        LOG.debug("Saving incoming payload [{}] to file disk", partInfo.getHref());

        final File attachmentStore = new File(currentStorage.getStorageDirectory(), UUID.randomUUID().toString() + PAYLOAD_EXTENSION);
        partInfo.setFileName(attachmentStore.getAbsolutePath());
        try (final InputStream inputStream = partInfo.getPayloadDatahandler().getInputStream()) {
            final long fileLength = saveIncomingFileToDisk(attachmentStore, inputStream, encryptionActive);
            partInfo.setLength(fileLength);
            partInfo.setEncrypted(encryptionActive);
        }

        LOG.debug("Finished saving incoming payload [{}] to file disk", partInfo.getHref());
    }

    protected long saveIncomingFileToDisk(File file, InputStream is, final Boolean encryptionActive) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);//NOSONAR the stream is closed in the finally block

            if (encryptionActive) {
                LOG.debug("Using encryption for file [{}]", file);
                final Cipher encryptCipherForPayload = encryptionService.getEncryptCipherForPayload();
                outputStream = new CipherOutputStream(outputStream, encryptCipherForPayload);
            }

            final long total = IOUtils.copy(is, outputStream, DEFAULT_BUFFER_SIZE);
            outputStream.flush();
            LOG.debug("Done writing file [{}]. Written [{}] bytes.", file.getName(), total);
            return total;
        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
    }

    @Override
    public void storeOutgoingPayload(PartInfo partInfo, UserMessage userMessage, LegConfiguration legConfiguration, String backendName) throws IOException, EbMS3Exception {
        //message fragment files are already saved on the file system
        if (!userMessage.isUserMessageFragment()) {
            PayloadFileStorage currentStorage = storageProvider.getCurrentStorage();
            saveOutgoingPayloadToDisk(partInfo, userMessage, legConfiguration, currentStorage, backendName);
        }
    }

    protected void saveOutgoingPayloadToDisk(PartInfo partInfo, UserMessage userMessage, LegConfiguration legConfiguration, PayloadFileStorage currentStorage, String backendName) throws IOException, EbMS3Exception {
        LOG.debug("Saving outgoing payload [{}] to file disk", partInfo.getHref());

        try (InputStream is = partInfo.getPayloadDatahandler().getInputStream()) {
            final String originalFileName = partInfo.getFileName();

            backendNotificationService.notifyPayloadSubmitted(userMessage, originalFileName, partInfo, backendName);

            final File attachmentStore = new File(currentStorage.getStorageDirectory(), UUID.randomUUID().toString() + PAYLOAD_EXTENSION);
            partInfo.setFileName(attachmentStore.getAbsolutePath());

            final Boolean encryptionActive = payloadPersistenceHelper.isPayloadEncryptionActive(userMessage);
            final long fileLength = saveOutgoingFileToDisk(attachmentStore, partInfo, is, userMessage, legConfiguration, encryptionActive);
            partInfo.setLength(fileLength);
            partInfo.setEncrypted(encryptionActive);

            payloadPersistenceHelper.validatePayloadSize(legConfiguration, partInfo.getLength());
            LOG.debug("Finished saving outgoing payload [{}] to file disk", partInfo.getHref());

            backendNotificationService.notifyPayloadProcessed(userMessage, originalFileName, partInfo, backendName);
        }
    }

    protected long saveOutgoingFileToDisk(File file, PartInfo partInfo, InputStream is, UserMessage userMessage, final LegConfiguration legConfiguration, final Boolean encryptionActive) throws IOException, EbMS3Exception {
        boolean useCompression = compressionService.handleCompression(userMessage.getMessageInfo().getMessageId(), partInfo, legConfiguration);
        LOG.debug("Compression for message with id: [{}] applied: [{}]", userMessage.getMessageInfo().getMessageId(), useCompression);

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file); //NOSONAR the stream is closed in the finally block

            if (encryptionActive) {
                LOG.debug("Using encryption for file [{}]", file);
                final Cipher encryptCipherForPayload = encryptionService.getEncryptCipherForPayload();
                outputStream = new CipherOutputStream(outputStream, encryptCipherForPayload); //NOSONAR the stream is closed in the finally block
            }

            if (useCompression) {
                LOG.debug("Using compression for storing the file [{}]", file);
                outputStream = new GZIPOutputStream(outputStream); //NOSONAR the stream is closed in the finally block
            }

            final long total = IOUtils.copy(is, outputStream, PayloadPersistence.DEFAULT_BUFFER_SIZE);
            LOG.debug("Done writing file [{}]. Written [{}] bytes.", file.getName(), total);
            return total;
        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
    }

}
