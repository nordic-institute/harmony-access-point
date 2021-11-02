package eu.domibus.core.earchive.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.earchive.*;
import eu.domibus.core.earchive.eark.FileSystemEArchivePersistence;
import eu.domibus.core.util.JmsUtil;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Message;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class EArchiveListenerTest {

    @Tested
    private EArchiveListener eArchiveListener;

    @Injectable
    private FileSystemEArchivePersistence fileSystemEArchivePersistence;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Injectable
    private EArchivingDefaultService eArchivingDefaultService;

    @Injectable
    private JmsUtil jmsUtil;

    @Injectable
    private ObjectMapper jsonMapper;

    @Injectable
    private EArchiveBatchUtils eArchiveBatchUtils;

    private String batchId;

    private Long entityId;

    private List<UserMessageDTO> userMessageDTOS;

    @Before
    public void setUp() throws Exception {
        batchId = UUID.randomUUID().toString();
        entityId = new Random().nextLong();

        userMessageDTOS = Arrays.asList(
                new UserMessageDTO(new Random().nextLong(), UUID.randomUUID().toString()),
                new UserMessageDTO(new Random().nextLong(), UUID.randomUUID().toString()));
    }

    @Test
    public void onMessage_noBatchInfo(@Injectable Message message) {
        new Expectations() {{
            databaseUtil.getDatabaseUserName();
            result = "unitTest";

            jmsUtil.getStringPropertySafely(message, MessageConstants.BATCH_ID);
            result = null;

            jmsUtil.getLongPropertySafely(message, MessageConstants.BATCH_ENTITY_ID);
            result = null;
        }};
        eArchiveListener.onMessage(message);

        new FullVerifications() {
        };
    }

    @Test(expected = DomibusEArchiveException.class)
    public void onMessage_noBatchFound(@Injectable Message message) {
        new Expectations() {{
            databaseUtil.getDatabaseUserName();
            result = "unitTest";

            jmsUtil.getStringPropertySafely(message, MessageConstants.BATCH_ID);
            result = batchId;

            jmsUtil.getLongPropertySafely(message, MessageConstants.BATCH_ENTITY_ID);
            result = entityId;

            eArchivingDefaultService.getEArchiveBatch(entityId);
            result = new DomibusEArchiveException("EArchive batch not found for batchId: [" + entityId + "]");
        }};

        eArchiveListener.onMessage(message);

        new FullVerifications() {
        };
    }

    @Test(expected = DomibusEArchiveException.class)
    public void onMessage_noMessages(@Injectable Message message,
                                     @Injectable EArchiveBatchEntity eArchiveBatch) {
        new Expectations() {{
            databaseUtil.getDatabaseUserName();
            result = "unitTest";

            jmsUtil.getStringPropertySafely(message, MessageConstants.BATCH_ID);
            result = batchId;

            jmsUtil.getLongPropertySafely(message, MessageConstants.BATCH_ENTITY_ID);
            result = entityId;

            eArchivingDefaultService.getEArchiveBatch(entityId);
            result = eArchiveBatch;
        }};

        eArchiveListener.onMessage(message);

        new FullVerifications() {{
            jmsUtil.setDomain(message);
        }};
    }

    @Test
    public void onMessage_ok(@Injectable Message message,
                                     @Injectable EArchiveBatchEntity eArchiveBatch,
                                     @Injectable FileObject fileObject) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes = objectMapper.writeValueAsString(new ListUserMessageDto(userMessageDTOS)).getBytes(StandardCharsets.UTF_8);
        new Expectations() {{
            databaseUtil.getDatabaseUserName();
            result = "unitTest";

            jmsUtil.getStringPropertySafely(message, MessageConstants.BATCH_ID);
            result = batchId;

            jmsUtil.getLongPropertySafely(message, MessageConstants.BATCH_ENTITY_ID);
            result = entityId;

            eArchivingDefaultService.getEArchiveBatch(entityId);
            result = eArchiveBatch;

            eArchiveBatchUtils.getUserMessageDtoFromJson((EArchiveBatchEntity)any);
            result = objectMapper.readValue(new String(bytes,StandardCharsets.UTF_8), ListUserMessageDto.class);

            eArchiveBatch.getDateRequested();
            result = new Date();

            eArchiveBatch.getRequestType();
            result = RequestType.CONTINUOUS;

            eArchiveBatch.geteArchiveBatchStatus();
            result = EArchiveBatchStatus.STARTED;

            fileSystemEArchivePersistence.createEArkSipStructure((BatchEArchiveDTO) any, (List<UserMessageDTO>) any);
            result = fileObject;

            eArchiveBatch.getBatchId();
            result = batchId;

        }};

        eArchiveListener.onMessage(message);

        new FullVerifications() {{
            jmsUtil.setDomain(message);
            times = 1;

            eArchivingDefaultService.executeBatchIsExported(((EArchiveBatchEntity) any), (List<UserMessageDTO>) any);
            times = 1;

            eArchiveBatchUtils.getMessageIds((List<UserMessageDTO>) any);
            times = 1;

            fileObject.close();

            eArchivingDefaultService.setStatus(eArchiveBatch, EArchiveBatchStatus.STARTED);
            times = 1;
        }};
    }
}