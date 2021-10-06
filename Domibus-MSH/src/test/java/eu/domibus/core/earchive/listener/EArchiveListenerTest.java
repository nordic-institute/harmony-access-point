package eu.domibus.core.earchive.listener;

import com.google.gson.Gson;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.earchive.*;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.util.JmsUtil;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Message;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_BATCH_INSERT_BATCH_SIZE;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(JMockit.class)
public class EArchiveListenerTest {

    @Tested
    private EArchiveListener eArchiveListener;

    @Injectable
    private FileSystemEArchivePersistence fileSystemEArchivePersistence;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Injectable
    private EArchiveBatchDao eArchiveBatchDao;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private UserMessageLogDefaultService userMessageLogDefaultService;

    @Injectable
    private JmsUtil jmsUtil;

    String batchId;
    Long entityId;
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

            eArchiveBatchDao.findEArchiveBatchByBatchId(entityId);
            result = null;
        }};
        eArchiveListener.onMessage(message);

        new FullVerifications() {
        };
    }

    @Test
    public void onMessage_noMessages(@Injectable Message message,
                                     @Injectable EArchiveBatchEntity eArchiveBatch) {
        new Expectations() {{
            databaseUtil.getDatabaseUserName();
            result = "unitTest";

            jmsUtil.getStringPropertySafely(message, MessageConstants.BATCH_ID);
            result = batchId;

            jmsUtil.getLongPropertySafely(message, MessageConstants.BATCH_ENTITY_ID);
            result = entityId;

            eArchiveBatchDao.findEArchiveBatchByBatchId(entityId);
            result = eArchiveBatch;

            eArchiveBatch.getMessageIdsJson();
            result = new Gson().toJson(new ListUserMessageDto(null), ListUserMessageDto.class).getBytes(StandardCharsets.UTF_8);

        }};

        eArchiveListener.onMessage(message);

        new FullVerifications() {{
            jmsUtil.setDomain(message);
        }};
    }

    @Test
    public void onMessage_ok(@Injectable Message message,
                                     @Injectable EArchiveBatchEntity eArchiveBatch,
                                     @Injectable FileObject fileObject) throws FileSystemException {
        new Expectations() {{
            databaseUtil.getDatabaseUserName();
            result = "unitTest";

            jmsUtil.getStringPropertySafely(message, MessageConstants.BATCH_ID);
            result = batchId;

            jmsUtil.getLongPropertySafely(message, MessageConstants.BATCH_ENTITY_ID);
            result = entityId;

            eArchiveBatchDao.findEArchiveBatchByBatchId(entityId);
            result = eArchiveBatch;

            eArchiveBatch.getMessageIdsJson();
            result = new Gson().toJson(new ListUserMessageDto(userMessageDTOS), ListUserMessageDto.class).getBytes(StandardCharsets.UTF_8);

            fileSystemEArchivePersistence.createEArkSipStructure((BatchEArchiveDTO) any, (List<UserMessageDTO>) any);
            result = fileObject;

            fileObject.getPath().toAbsolutePath();
            result = Paths.get("");

            eArchiveBatch.getBatchId();
            result = batchId;

            domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_BATCH_INSERT_BATCH_SIZE);
            result = 5;
        }};

        eArchiveListener.onMessage(message);

        new FullVerifications() {{
            jmsUtil.setDomain(message);
            times = 1;

            userMessageLogDefaultService.updateStatusToArchived(userMessageDTOS.stream().map(UserMessageDTO::getEntityId).collect(Collectors.toList()), 5);
            times = 1;

            fileObject.close();
        }};
    }
}