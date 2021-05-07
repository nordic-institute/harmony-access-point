package eu.domibus.core.message.plugin.handler;

import eu.domibus.AbstractIT;
import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.model.MessageType;
import eu.domibus.core.message.MessagesLogServiceImpl;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.Submission;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class DatabaseMessageHandlerTestIT extends AbstractIT {

    private static final String MIME_TYPE = "MimeType";
    private static final String DEFAULT_MT = "text/xml";
    private static final String DOMIBUS_BLUE = "domibus-blue";
    private static final String DOMIBUS_RED = "domibus-red";
    private static final String INITIATOR_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";
    private static final String RESPONDER_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";
    private static final String PAYLOAD_ID = "cid:message";
    private static final String UNREGISTERED_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String ORIGINAL_SENDER = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";
    private static final String FINAL_RECIPIENT = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";
    private static final String ACTION_TC1LEG1 = "TC1Leg1";
    private static final String PROTOCOL_AS4 = "AS4";
    private static final String SERVICE_NOPROCESS = "bdx:noprocess";
    private static final String SERVICE_TYPE_TC1 = "tc1";
    private static final String PROPERTY_ENDPOINT = "endPointAddress";

    @Configuration
    static class ContextConfiguration {

        @Primary
        @Bean
        public BackendConnectorProvider backendConnectorProvider() {
            return Mockito.mock(BackendConnectorProvider.class);
        }
    }

    @Autowired
    @Qualifier(DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE)
    private DataSource dataSource;

    @Autowired
    BackendConnectorProvider backendConnectorProvider;

    @Autowired
    DatabaseMessageHandler databaseMessageHandler;

    @Autowired
    MessagesLogServiceImpl messagesLogService;

    @Test
    public void submit() throws MessagingProcessingException, IOException {
        BackendConnector backendConnector = Mockito.mock(BackendConnector.class);
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);

        Submission submission = createSubmission();
        uploadPmode();
        databaseMessageHandler.submit(submission, "mybackend");

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "TB_USER_MESSAGE_LOG"));

        final HashMap<String, Object> filters = new HashMap<>();
        filters.put("receivedTo", new Date());
        filters.put("messageSubtype", null);
        messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 10, "received", false, filters);

    }

    protected Submission createSubmission() {
        Submission submission = new Submission();
        submission.setMpc(Ebms3Constants.DEFAULT_MPC);
        submission.setAction(ACTION_TC1LEG1);
        submission.setService(SERVICE_NOPROCESS);
        submission.setServiceType(SERVICE_TYPE_TC1);
        submission.setConversationId("123");
        submission.setMessageId("1234");
        submission.addFromParty(DOMIBUS_BLUE, UNREGISTERED_PARTY_TYPE);
        submission.setFromRole(INITIATOR_ROLE);
        submission.addToParty(DOMIBUS_RED, UNREGISTERED_PARTY_TYPE);
        submission.setToRole(RESPONDER_ROLE);
        submission.addMessageProperty(MessageConstants.ORIGINAL_SENDER, ORIGINAL_SENDER);
        submission.addMessageProperty(MessageConstants.FINAL_RECIPIENT, FINAL_RECIPIENT);
//        submission.setAgreementRef("12345");
        submission.setRefToMessageId("123456");

        String strPayLoad1 = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";
        DataHandler payLoadDataHandler = new DataHandler(new ByteArrayDataSource(strPayLoad1.getBytes(), DEFAULT_MT));
        Submission.TypedProperty objTypedProperty = new Submission.TypedProperty(MIME_TYPE, DEFAULT_MT);
        Collection<Submission.TypedProperty> listTypedProperty = new ArrayList<>();
        listTypedProperty.add(objTypedProperty);
        Submission.Payload objPayload1 = new Submission.Payload(PAYLOAD_ID, payLoadDataHandler, listTypedProperty, false, null, null);
        submission.addPayload(objPayload1);

        return submission;
    }
}
