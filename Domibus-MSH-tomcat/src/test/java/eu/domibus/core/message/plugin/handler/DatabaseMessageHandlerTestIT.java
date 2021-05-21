package eu.domibus.core.message.plugin.handler;

import eu.domibus.AbstractIT;
import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.message.MessagesLogServiceImpl;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.util.SubmissionUtil;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.Submission;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;

@Transactional
public class DatabaseMessageHandlerTestIT extends AbstractIT {

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public BackendConnectorProvider backendConnectorProvider() {
            return Mockito.mock(BackendConnectorProvider.class);
        }
    }

    @Autowired
    protected SubmissionUtil submissionUtil;

    @Autowired
    @Qualifier(DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE)
    private DataSource dataSource;

    @Autowired
    BackendConnectorProvider backendConnectorProvider;

    @Autowired
    DatabaseMessageHandler databaseMessageHandler;

    @Autowired
    MessagesLogServiceImpl messagesLogService;

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Test
    public void submit() throws MessagingProcessingException, IOException {
        BackendConnector backendConnector = Mockito.mock(BackendConnector.class);
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);

        Submission submission = submissionUtil.createSubmission();
        uploadPmode();
        final String messageId = databaseMessageHandler.submit(submission, "mybackend");

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        assertNotNull(userMessageLog);

        final HashMap<String, Object> filters = new HashMap<>();
        filters.put("receivedTo", new Date());
        filters.put("messageSubtype", null);
        messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 10, "received", false, filters);

    }


}
