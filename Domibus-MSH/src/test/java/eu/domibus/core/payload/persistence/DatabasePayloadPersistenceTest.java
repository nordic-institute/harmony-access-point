package eu.domibus.core.payload.persistence;

import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorage;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Cosmin Baciu
 * @since
 */
@RunWith(JMockit.class)
public class DatabasePayloadPersistenceTest {

    @Injectable
    protected BackendNotificationService backendNotificationService;

    @Injectable
    protected CompressionService compressionService;

    @Tested
    DatabasePayloadPersistence databasePayloadPersistence;

    @Test
    public void testStoreIncomingPayload(@Injectable PartInfo partInfo,
                                         @Injectable PayloadFileStorage storage,
                                         @Mocked IOUtils ioUtils,
                                         @Injectable InputStream inputStream,
                                         @Injectable UserMessage userMessage) throws IOException {
        final byte[] binaryData = "test".getBytes();

        new Expectations() {{
            partInfo.getPayloadDatahandler().getInputStream();
            result = inputStream;

            IOUtils.toByteArray(inputStream);
            result = binaryData;
        }};

        databasePayloadPersistence.storeIncomingPayload(partInfo, userMessage);

        new Verifications() {{
            partInfo.setBinaryData(binaryData);
            partInfo.setLength(binaryData.length);
            partInfo.setFileName(null);
        }};
    }

    @Test
    public void storeOutgoingPayload() {
    }

    @Test
    public void getOutgoingBinaryData() {
    }

    @Test
    public void compress() {
    }
}