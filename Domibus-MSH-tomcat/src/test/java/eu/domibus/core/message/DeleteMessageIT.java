package eu.domibus.core.message;


import eu.domibus.AbstractIT;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.test.common.SoapSampleUtil;
import eu.domibus.test.common.SubmissionUtil;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.Submission;
import eu.domibus.test.common.MessageDBUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author idragusa
 * @since 5.0
 */
@Ignore("EDELIVERY-8052 Failing tests must be ignored (FAILS ON BAMBOO) ")
public abstract class DeleteMessageIT extends AbstractIT {

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public BackendConnectorProvider backendConnectorProvider() {
            return Mockito.mock(BackendConnectorProvider.class);
        }

        @Bean
        public BackendConnector backendConnector() {
            return Mockito.mock(BackendConnector.class);
        }
    }

    @Autowired
    protected BackendConnectorProvider backendConnectorProvider;

    @Autowired
    protected BackendConnector backendConnector;

    @Autowired
    protected MessageRetentionDefaultService messageRetentionService;

    @Autowired
    protected Provider<SOAPMessage> mshWebserviceTest;

    @Autowired
    protected MessageDBUtil messageDBUtil;

    @Autowired
    protected SubmissionUtil submissionUtil;

    @Autowired
    protected DatabaseMessageHandler databaseMessageHandler;

    @Autowired
    protected SoapSampleUtil soapSampleUtil;

    protected static List<String> tablesToExclude;

    @BeforeClass
    public static void initTablesToExclude() {
        tablesToExclude = new ArrayList<>(Arrays.asList(
                "TB_EVENT",
                "TB_EVENT_ALERT",
                "TB_EVENT_PROPERTY",
                "TB_ALERT"
        ));
    }

    @Before
    public void initialize() {
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);
    }

    protected void receiveMessageToDelete() throws SOAPException, IOException, ParserConfigurationException, SAXException {
        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";

        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);
    }

    protected void deleteMessages() {
        messageRetentionService.deleteExpiredMessages();
    }

    protected void sendMessageToDelete(MessageStatus status) throws MessagingProcessingException, IOException {

        Submission submission = submissionUtil.createSubmission();
        final String messageId = databaseMessageHandler.submit(submission, "mybackend");

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        userMessageLogDao.setMessageStatus(userMessageLog, MessageStatus.SEND_FAILURE);
    }
}
