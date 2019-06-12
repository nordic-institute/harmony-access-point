package eu.domibus.core.payload.persistence;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.common.services.impl.MessagingServiceImpl;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @Override
    public void storeIncomingPayload(PartInfo partInfo, UserMessage userMessage) throws IOException {
        LOG.debug("Saving incoming payload [{}] to database", partInfo.getHref());
        try (InputStream is = partInfo.getPayloadDatahandler().getInputStream()) {
            byte[] binaryData = IOUtils.toByteArray(is);
            partInfo.setBinaryData(binaryData);
            partInfo.setLength(binaryData.length);
            partInfo.setFileName(null);
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
        byte[] binaryData = IOUtils.toByteArray(is);

        boolean useCompression = compressionService.handleCompression(userMessage.getMessageInfo().getMessageId(), partInfo, legConfiguration);
        LOG.debug("Compression for message with id: [{}] applied: [{}]", userMessage.getMessageInfo().getMessageId(), useCompression);

        if (useCompression) {
            binaryData = compress(binaryData);
        }

        return binaryData;
    }

    protected byte[] compress(byte[] binaryData) throws IOException {
        LOG.debug("Compressing binary data");
        final byte[] buffer = new byte[MessagingServiceImpl.DEFAULT_BUFFER_SIZE];
        InputStream sourceStream = new ByteArrayInputStream(binaryData);
        ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
        GZIPOutputStream targetStream = new GZIPOutputStream(compressedContent);
        int i;
        while ((i = sourceStream.read(buffer)) > 0) {
            targetStream.write(buffer, 0, i);
        }
        sourceStream.close();
        targetStream.finish();
        targetStream.close();

        return compressedContent.toByteArray();
    }
}
