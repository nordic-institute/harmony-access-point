package eu.domibus.core.payload.persistence;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.payload.encryption.PayloadEncryptionService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class DatabasePayloadPersistence implements PayloadPersistence {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DatabasePayloadPersistence.class);

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
        LOG.debug("Saving incoming payload [{}] to database", partInfo.getHref());

        OutputStream outputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream(PayloadPersistence.DEFAULT_BUFFER_SIZE);
            outputStream = byteArrayOutputStream;

            final Boolean encryptionActive = payloadPersistenceHelper.isPayloadEncryptionActive(userMessage);
            if (encryptionActive) {
                LOG.debug("Using encryption for part info [{}]", partInfo.getHref());
                final Cipher encryptCipherForPayload = encryptionService.getEncryptCipherForPayload();
                outputStream = new CipherOutputStream(outputStream, encryptCipherForPayload);
                partInfo.setEncrypted(true);
            }

            try (InputStream is = partInfo.getPayloadDatahandler().getInputStream()) {
                IOUtils.copy(is, outputStream, DEFAULT_BUFFER_SIZE);
            }
        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
        byte[] binaryData = byteArrayOutputStream.toByteArray();
        final int partInfoLength = binaryData.length;
        partInfo.setBinaryData(binaryData);
        partInfo.setLength(partInfoLength);
        partInfo.setFileName(null);
        LOG.debug("Finished saving incoming payload [{}] to database", partInfo.getHref());

        validatePayloadSize(legConfiguration, partInfoLength);
    }

    @Override
    public void storeOutgoingPayload(PartInfo partInfo, UserMessage userMessage, LegConfiguration legConfiguration, String backendName) throws IOException, EbMS3Exception {
        LOG.debug("Saving outgoing payload [{}] to database", partInfo.getHref());

        try (InputStream is = partInfo.getPayloadDatahandler().getInputStream()) {
            final String originalFileName = partInfo.getFileName();

            backendNotificationService.notifyPayloadSubmitted(userMessage, originalFileName, partInfo, backendName);

            final Boolean encryptionActive = payloadPersistenceHelper.isPayloadEncryptionActive(userMessage);

            byte[] binaryData = getOutgoingBinaryData(partInfo, is, userMessage, legConfiguration, encryptionActive);
            int partInfoLength = binaryData.length;
            partInfo.setBinaryData(binaryData);
            partInfo.setLength(partInfoLength);
            partInfo.setFileName(null);
            partInfo.setEncrypted(encryptionActive);

            LOG.debug("Finished saving outgoing payload [{}] to database", partInfo.getHref());

            validatePayloadSize(legConfiguration, partInfoLength);

            backendNotificationService.notifyPayloadProcessed(userMessage, originalFileName, partInfo, backendName);
        }
    }

    protected byte[] getOutgoingBinaryData(PartInfo partInfo, InputStream is, UserMessage userMessage, final LegConfiguration legConfiguration, final Boolean encryptionActive) throws IOException, EbMS3Exception {
        OutputStream outputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream(PayloadPersistence.DEFAULT_BUFFER_SIZE);
            outputStream = byteArrayOutputStream;

            boolean useCompression = compressionService.handleCompression(userMessage.getMessageInfo().getMessageId(), partInfo, legConfiguration);
            LOG.debug("Compression properties for message [{}] applied? [{}]", userMessage.getMessageInfo().getMessageId(), useCompression);

            if (encryptionActive) {
                LOG.debug("Using encryption for part info [{}]", partInfo.getHref());
                final Cipher encryptCipherForPayload = encryptionService.getEncryptCipherForPayload();
                outputStream = new CipherOutputStream(outputStream, encryptCipherForPayload);
            }

            if (useCompression) {
                LOG.debug("Using compression for part info [{}]", partInfo.getHref());
                outputStream = new GZIPOutputStream(outputStream);
            }

            IOUtils.copy(is, outputStream, DEFAULT_BUFFER_SIZE);

        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    private void validatePayloadSize(LegConfiguration legConfiguration, int partInfoLength) {
        final int payloadProfileMaxSize = legConfiguration.getPayloadProfile().getMaxSize();
        final String payloadProfileName = legConfiguration.getPayloadProfile().getName();

        if (payloadProfileMaxSize < 0) {
            LOG.warn("No validation will be made for [{}] as maxSize has the value [{}]", payloadProfileName, payloadProfileMaxSize);
        }

        if (partInfoLength > payloadProfileMaxSize) {
            throw new InvalidPayloadSizeException("Payload size [" + partInfoLength + "] is greater than the maximum value defined [" + payloadProfileMaxSize + "] for profile ["+payloadProfileName+"]");
        }
    }
}
