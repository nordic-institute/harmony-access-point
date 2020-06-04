package eu.domibus.core.message;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.message.splitandjoin.SplitAndJoinService;
import eu.domibus.core.payload.persistence.PayloadPersistence;
import eu.domibus.core.payload.persistence.PayloadPersistenceProvider;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorage;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.PayloadInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ioana Dragusanu
 * @author Cosmin Baciu
 * @since 3.3
 */

@RunWith(JMockit.class)
public class MessagingServiceImplTest {

    @Tested
    MessagingServiceImpl messagingService;

    @Injectable
    protected PayloadPersistenceProvider payloadPersistenceProvider;

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    PayloadFileStorage storage;

    @Injectable
    PayloadFileStorageProvider storageProvider;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    LegConfiguration legConfiguration;

    @Injectable
    SplitAndJoinService splitAndJoinService;

    @Injectable
    CompressionService compressionService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    DomainTaskExecutor domainTaskExecutor;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    BackendNotificationService backendNotificationService;

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Test
    public void testStoreOutgoingPayload(@Injectable UserMessage userMessage,
                                         @Injectable PartInfo partInfo,
                                         @Injectable LegConfiguration legConfiguration,
                                         @Injectable String backendName,
                                         @Injectable PayloadPersistence payloadPersistence) throws IOException, EbMS3Exception {
        new Expectations(messagingService) {{
            payloadPersistenceProvider.getPayloadPersistence(partInfo, userMessage);
            result = payloadPersistence;
        }};

        messagingService.storeOutgoingPayload(partInfo, userMessage, legConfiguration, backendName);

        new Verifications() {{
            payloadPersistence.storeOutgoingPayload(partInfo, userMessage, legConfiguration, backendName);
            times = 1;
        }};
    }

    @Test
    public void testStoreIncomingPayload(@Injectable UserMessage userMessage,
                                         @Injectable PartInfo partInfo,
                                         @Injectable PayloadPersistence payloadPersistence) throws IOException {
        new Expectations() {{
            payloadPersistenceProvider.getPayloadPersistence(partInfo, userMessage);
            result = payloadPersistence;
        }};

        messagingService.storeIncomingPayload(partInfo, userMessage, null);

        new Verifications() {{
            payloadPersistence.storeIncomingPayload(partInfo, userMessage, null);
            times = 1;
        }};
    }

    @Test
    public void testStoreSourceMessagePayloads(@Injectable Messaging messaging,
                                               @Injectable MSHRole mshRole,
                                               @Injectable LegConfiguration legConfiguration,
                                               @Injectable String backendName) {

        String messageId = "123";
        new Expectations(messagingService) {{
            messaging.getUserMessage().getMessageInfo().getMessageId();
            result = messageId;

            messagingService.storePayloads(messaging, mshRole, legConfiguration, backendName);
        }};

        messagingService.storeSourceMessagePayloads(messaging, mshRole, legConfiguration, backendName);

        new Verifications() {{
            userMessageService.scheduleSourceMessageSending(messageId);
        }};
    }

    @Test
    public void testScheduleSourceMessagePayloads(@Injectable final Messaging messaging,
                                                  @Injectable final Domain domain,
                                                  @Injectable final PayloadInfo payloadInfo,
                                                  @Injectable final PartInfo partInfo) {


        List<PartInfo> partInfos = new ArrayList<>();
        partInfos.add(partInfo);

        new Expectations() {{
            messaging.getUserMessage().getPayloadInfo();
            result = payloadInfo;

            payloadInfo.getPartInfo();
            result = partInfos;

            partInfo.getLength();
            result = 20 * MessagingServiceImpl.BYTES_IN_MB;

            domibusPropertyProvider.getLongProperty(MessagingServiceImpl.PROPERTY_PAYLOADS_SCHEDULE_THRESHOLD);
            result = 15;
        }};

        final boolean scheduleSourceMessagePayloads = messagingService.scheduleSourceMessagePayloads(messaging);
        Assert.assertTrue(scheduleSourceMessagePayloads);
    }

    @Test
    public void testStoreMessageCalls(@Injectable final Messaging messaging) throws IOException, JAXBException, XMLStreamException {
        messagingService.storeMessage(messaging, MSHRole.SENDING, legConfiguration, "backend");

        new Verifications() {{
            messagingDao.create(messaging);
            times = 1;
        }};
    }

    @Test
    public void testStoreOutgoingMessage(@Injectable Messaging messaging,
                                         @Injectable UserMessage userMessage,
                                         @Injectable PartInfo partInfo,
                                         @Injectable PayloadPersistence payloadPersistence) throws Exception {

        List<PartInfo> partInfos = new ArrayList<>();
        partInfos.add(partInfo);

        new Expectations() {{
            payloadPersistenceProvider.getPayloadPersistence((PartInfo) any, userMessage);
            result = payloadPersistence;

            messaging.getUserMessage();
            result = userMessage;

            userMessage.getPayloadInfo().getPartInfo();
            result = partInfos;
        }};

        final String backend = "backend";
        messagingService.storeMessage(messaging, MSHRole.SENDING, legConfiguration, backend);

        new Verifications() {{
            payloadPersistence.storeOutgoingPayload(partInfo, userMessage, legConfiguration, backend);
            times = 1;
        }};
    }

}
