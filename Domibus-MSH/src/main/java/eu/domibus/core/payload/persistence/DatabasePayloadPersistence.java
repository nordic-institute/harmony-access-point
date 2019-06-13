package eu.domibus.core.payload.persistence;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.core.encryption.EncryptionService;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional(propagation = Propagation.SUPPORTS)
@Service
public class DatabasePayloadPersistence implements PayloadPersistence {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DatabasePayloadPersistence.class);

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Autowired
    protected CompressionService compressionService;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected EncryptionService encryptionService;

    @Override
    public void storeIncomingPayload(PartInfo partInfo, UserMessage userMessage) throws IOException {
        LOG.debug("Saving incoming payload [{}] to database", partInfo.getHref());

        OutputStream outputStream = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(PayloadPersistence.DEFAULT_BUFFER_SIZE);
            outputStream = byteArrayOutputStream;

            final Boolean encryptionActive = domibusConfigurationService.isPayloadEncryptionActive(domainContextProvider.getCurrentDomain());
            if (encryptionActive) {
                LOG.debug("Using encryption for part info [{}]", partInfo.getHref());
                final Cipher encryptCipherForPayload = encryptionService.getEncryptCipherForPayload();
                outputStream = new CipherOutputStream(outputStream, encryptCipherForPayload);
            }

            try (InputStream is = partInfo.getPayloadDatahandler().getInputStream()) {
                IOUtils.copy(is, outputStream, DEFAULT_BUFFER_SIZE);

                byte[] binaryData = byteArrayOutputStream.toByteArray();
                partInfo.setBinaryData(binaryData);
                partInfo.setLength(binaryData.length);
                partInfo.setFileName(null);
            }
        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
        LOG.debug("Finished saving incoming payload [{}] to database", partInfo.getHref());
    }

    @Override
    public void storeOutgoingPayload(PartInfo partInfo, UserMessage userMessage, LegConfiguration legConfiguration, String backendName) throws IOException, EbMS3Exception {
        LOG.debug("Saving outgoing payload [{}] to database", partInfo.getHref());

        try (InputStream is = partInfo.getPayloadDatahandler().getInputStream()) {
            final String originalFileName = partInfo.getFileName();

            backendNotificationService.notifyPayloadSubmitted(userMessage, originalFileName, partInfo, backendName);

            byte[] binaryData = getOutgoingBinaryData(partInfo, is, userMessage, legConfiguration);
            partInfo.setBinaryData(binaryData);
            partInfo.setLength(binaryData.length);
            partInfo.setFileName(null);

            LOG.debug("Finished saving outgoing payload [{}] to database", partInfo.getHref());

            backendNotificationService.notifyPayloadProcessed(userMessage, originalFileName, partInfo, backendName);
        }
    }

    protected byte[] getOutgoingBinaryData(PartInfo partInfo, InputStream is, UserMessage userMessage, final LegConfiguration legConfiguration) throws IOException, EbMS3Exception {
        OutputStream outputStream = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(PayloadPersistence.DEFAULT_BUFFER_SIZE);
            outputStream = byteArrayOutputStream;

            boolean useCompression = compressionService.handleCompression(userMessage.getMessageInfo().getMessageId(), partInfo, legConfiguration);
            LOG.debug("Compression properties for message [{}] applied? [{}]", userMessage.getMessageInfo().getMessageId(), useCompression);

            if (useCompression) {
                LOG.debug("Using compression for part info [{}]", partInfo.getHref());
                outputStream = new GZIPOutputStream(outputStream);
            }

            final Boolean encryptionActive = domibusConfigurationService.isPayloadEncryptionActive(domainContextProvider.getCurrentDomain());
            if (encryptionActive) {
                LOG.debug("Using encryption for part info [{}]", partInfo.getHref());
                final Cipher encryptCipherForPayload = encryptionService.getEncryptCipherForPayload();
                outputStream = new CipherOutputStream(outputStream, encryptCipherForPayload);
            }

            IOUtils.copy(is, outputStream, DEFAULT_BUFFER_SIZE);
            byte[] binaryData = byteArrayOutputStream.toByteArray();

            return binaryData;
        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
    }
}
