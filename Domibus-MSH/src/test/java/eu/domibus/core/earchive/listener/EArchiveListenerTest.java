package eu.domibus.core.earchive.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.earchive.*;
import eu.domibus.core.earchive.eark.FileSystemEArchivePersistence;
import eu.domibus.core.util.JmsUtil;
import eu.domibus.ext.domain.archive.BatchArchiveStatusType;
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
import java.util.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
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

    private List<EArchiveBatchUserMessage> batchUserMessages;

    @Before
    public void setUp() {
        batchId = UUID.randomUUID().toString();
        entityId = new Random().nextLong();

        batchUserMessages = Arrays.asList(
                new EArchiveBatchUserMessage(new Random().nextLong(), UUID.randomUUID().toString()),
                new EArchiveBatchUserMessage(new Random().nextLong(), UUID.randomUUID().toString()));
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

            jmsUtil.getStringPropertySafely(message, MessageConstants.STATUS_TO);
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

            jmsUtil.getStringPropertySafely(message, MessageConstants.STATUS_TO);
            result = null;

            eArchivingDefaultService.getEArchiveBatch(entityId, true);
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

            jmsUtil.getStringPropertySafely(message, MessageConstants.STATUS_TO);
            result = null;

            eArchivingDefaultService.getEArchiveBatch(entityId, true);
            result = eArchiveBatch;
        }};

        eArchiveListener.onMessage(message);

        new FullVerifications() {{
            jmsUtil.setDomain(message);
        }};
    }

    @Test
    public void onMessage_ExportOK(@Injectable Message message,
                                     @Injectable EArchiveBatchEntity eArchiveBatch,
                                     @Injectable FileObject fileObject) throws IOException {
        new Expectations() {{
            databaseUtil.getDatabaseUserName();
            result = "unitTest";

            jmsUtil.getStringPropertySafely(message, MessageConstants.BATCH_ID);
            result = batchId;

            jmsUtil.getStringPropertySafely(message, MessageConstants.STATUS_TO);
            result = null;

            jmsUtil.getLongPropertySafely(message, MessageConstants.BATCH_ENTITY_ID);
            result = entityId;

            eArchivingDefaultService.getEArchiveBatch(entityId, true);
            result = eArchiveBatch;

            eArchiveBatch.geteArchiveBatchUserMessages();
            result = batchUserMessages;

            eArchiveBatch.getDateRequested();
            result = new Date();

            eArchiveBatch.getRequestType();
            result = EArchiveRequestType.CONTINUOUS;

            eArchiveBatch.getEArchiveBatchStatus();
            result = EArchiveBatchStatus.STARTED;

            fileSystemEArchivePersistence.createEArkSipStructure((BatchEArchiveDTO) any, (List<EArchiveBatchUserMessage>) any);
            result = fileObject;

            eArchiveBatch.getBatchId();
            result = batchId;

        }};

        eArchiveListener.onMessage(message);

        new FullVerifications() {{
            jmsUtil.setDomain(message);
            times = 1;

            eArchivingDefaultService.executeBatchIsExported(((EArchiveBatchEntity) any));
            times = 1;

            eArchiveBatchUtils.getMessageIds((List<EArchiveBatchUserMessage>) any);
            times = 1;

            fileObject.close();

            eArchivingDefaultService.setStatus(eArchiveBatch, EArchiveBatchStatus.STARTED);
            times = 1;
        }};
    }

    @Test
    public void onMessage_ArchiveOK(@Injectable Message message,
                                   @Injectable EArchiveBatchEntity eArchiveBatch,
                                   @Injectable FileObject fileObject){
        new Expectations() {{
            databaseUtil.getDatabaseUserName();
            result = "unitTest";

            jmsUtil.getStringPropertySafely(message, MessageConstants.BATCH_ID);
            result = batchId;

            jmsUtil.getStringPropertySafely(message, MessageConstants.STATUS_TO);
            result = BatchArchiveStatusType.ARCHIVED.name();

            jmsUtil.getLongPropertySafely(message, MessageConstants.BATCH_ENTITY_ID);
            result = entityId;

            eArchivingDefaultService.getEArchiveBatch(entityId, true);
            result = eArchiveBatch;

            eArchiveBatch.geteArchiveBatchUserMessages();
            result = batchUserMessages;

        }};

        eArchiveListener.onMessage(message);

        new FullVerifications() {{
            jmsUtil.setDomain(message);
            times = 1;

            eArchivingDefaultService.executeBatchIsArchived(eArchiveBatch, batchUserMessages);
            times = 1;
        }};
    }
}