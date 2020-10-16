package eu.domibus.plugin.jms;

import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.ext.services.FileUtilExtService;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.Submission;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

import javax.activation.DataHandler;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static eu.domibus.plugin.jms.JMSMessageConstants.*;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.junit.Assert.assertEquals;

/**
 * Created by Arun Raj on 18/10/2016.
 */
@RunWith(JMockit.class)
public class JMSMessageTransformerTest {

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
    private static final String FINAL_RECIPIENT_TYPE = "iso6523-actorid-upis";
    private static final String ACTION_TC1LEG1 = "TC1Leg1";
    private static final String PROTOCOL_AS4 = "AS4";
    private static final String SERVICE_NOPROCESS = "bdx:noprocess";
    private static final String SERVICE_TYPE_TC1 = "tc1";
    private static final String PAYLOAD_FILENAME = "FileName";
    private static final String PAYLOAD_1_FILENAME = "payload_1_fileName";
    private static final String PAYLOAD_2_FILENAME = "payload_2_fileName";
    private static final String FILENAME_TEST = "09878378732323.payload";
    private static final String CUSTOM_AGREEMENT_REF = "customAgreement";
    public static final String PROPERTY_TEST = "test";
    public static final String PROPERTY_PREFIX = "property_";
    public static final String PAY_LOAD = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";

    @Injectable
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Injectable
    protected DomainContextExtService domainContextExtService;

    @Injectable
    protected FileUtilExtService fileUtilExtService;

    @Tested
    JMSMessageTransformer testObj = new JMSMessageTransformer();
    @Mocked
    private MapMessage messageIn;


    /**
     * Testing basic happy flow scenario of the transform from submission of JMS transformer
     */
    @Test
    public void transformFromSubmission_HappyFlow() throws Exception {
        Submission submissionObj = new Submission();
        submissionObj.setMpc(MPC);
        submissionObj.setAction(ACTION_TC1LEG1);
        submissionObj.setService(SERVICE_NOPROCESS);
        submissionObj.setServiceType(SERVICE_TYPE_TC1);
        submissionObj.setConversationId("123");
        submissionObj.setMessageId("1234");
        submissionObj.addFromParty(DOMIBUS_BLUE, UNREGISTERED_PARTY_TYPE);
        submissionObj.setFromRole(INITIATOR_ROLE);
        submissionObj.addToParty(DOMIBUS_RED, UNREGISTERED_PARTY_TYPE);
        submissionObj.setToRole(RESPONDER_ROLE);
        submissionObj.addMessageProperty(PROPERTY_ORIGINAL_SENDER, ORIGINAL_SENDER);
        submissionObj.addMessageProperty(PROPERTY_ENDPOINT, "http://localhost:8080/domibus/domibus-blue");
        submissionObj.addMessageProperty(PROPERTY_FINAL_RECIPIENT, FINAL_RECIPIENT, FINAL_RECIPIENT_TYPE);
        submissionObj.addMessageProperty(PROPERTY_TEST, "test property");
        submissionObj.setAgreementRef("12345");
        submissionObj.setRefToMessageId("123456");

        DataHandler payLoadDataHandler1 = new DataHandler(new ByteArrayDataSource(PAY_LOAD.getBytes(), DEFAULT_MT));

        DataHandler payLoadDataHandler2 = new DataHandler(new ByteArrayDataSource(PAY_LOAD.getBytes(), DEFAULT_MT));


        Submission.TypedProperty objTypedProperty1 = new Submission.TypedProperty(MIME_TYPE, DEFAULT_MT);
        Submission.TypedProperty objTypedProperty2 = new Submission.TypedProperty(PAYLOAD_FILENAME, FILENAME_TEST);
        Collection<Submission.TypedProperty> listTypedProperty = new ArrayList<>();
        listTypedProperty.add(objTypedProperty1);
        listTypedProperty.add(objTypedProperty2);
        Submission.Payload objPayload1 = new Submission.Payload(PAYLOAD_ID, payLoadDataHandler1, listTypedProperty, false, null, null);
        submissionObj.addPayload(objPayload1);
        Submission.Payload objBodyload = new Submission.Payload("", payLoadDataHandler2, listTypedProperty, true, null, null);
        submissionObj.addPayload(objBodyload);

        new Expectations() {{
            domibusPropertyExtService.getProperty(JMS_PLUGIN_PROPERTY_PREFIX + "." + PUT_ATTACHMENTS_IN_QUEUE);
            result = "false";
        }};

        MapMessage messageMap = new ActiveMQMapMessage();
        messageMap = testObj.transformFromSubmission(submissionObj, messageMap);


        assertEquals(MPC, messageMap.getStringProperty(MPC));
        assertEquals(ACTION_TC1LEG1, messageMap.getStringProperty(ACTION));
        assertEquals(SERVICE_NOPROCESS, messageMap.getStringProperty(SERVICE));
        assertEquals(SERVICE_TYPE_TC1, messageMap.getStringProperty(SERVICE_TYPE));
        assertEquals("123", messageMap.getStringProperty(CONVERSATION_ID));
        assertEquals("1234", messageMap.getStringProperty(MESSAGE_ID));
        assertEquals(DOMIBUS_BLUE, messageMap.getStringProperty(FROM_PARTY_ID));
        assertEquals(UNREGISTERED_PARTY_TYPE, messageMap.getStringProperty(FROM_PARTY_TYPE));
        assertEquals(INITIATOR_ROLE, messageMap.getStringProperty(FROM_ROLE));
        assertEquals(DOMIBUS_RED, messageMap.getStringProperty(TO_PARTY_ID));
        assertEquals(UNREGISTERED_PARTY_TYPE, messageMap.getStringProperty(TO_PARTY_TYPE));
        assertEquals(RESPONDER_ROLE, messageMap.getStringProperty(TO_ROLE));
        assertEquals(ORIGINAL_SENDER, messageMap.getStringProperty(PROPERTY_ORIGINAL_SENDER));
        assertEquals(FINAL_RECIPIENT, messageMap.getStringProperty(PROPERTY_FINAL_RECIPIENT));
        assertEquals("test property", messageMap.getStringProperty(PROPERTY_PREFIX + PROPERTY_TEST));
        assertEquals("12345", messageMap.getStringProperty(AGREEMENT_REF));
        assertEquals("123456", messageMap.getStringProperty(REF_TO_MESSAGE_ID));
        messageMap.setStringProperty(JMSMessageConstants.AGREEMENT_REF, "customAgreement");
        assertEquals("true", messageMap.getStringProperty(P1_IN_BODY));

        File file = new File(FILENAME_TEST);
        assertEquals(file.getName(), messageMap.getStringProperty(PAYLOAD_2_FILENAME));

    }

    /*
     * Testing basic happy flow scenario of the transform from messaging to submission of JMS transformer
     */
    @Test
    public void transformToSubmission_HappyFlow() throws Exception {
        ReflectionTestUtils.setField(testObj,"fileUtilExtService", (FileUtilExtService) fileName -> fileName);

        MapMessage messageMap = new ActiveMQMapMessage();
        messageMap.setStringProperty(JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, "submitMessage");
        messageMap.setStringProperty(SERVICE, SERVICE_NOPROCESS);
        messageMap.setStringProperty(SERVICE_TYPE, SERVICE_TYPE_TC1);
        messageMap.setStringProperty(ACTION, ACTION_TC1LEG1);
        messageMap.setStringProperty(FROM_PARTY_ID, DOMIBUS_BLUE);
        messageMap.setStringProperty(FROM_PARTY_TYPE, UNREGISTERED_PARTY_TYPE);
        messageMap.setStringProperty(TO_PARTY_ID, DOMIBUS_RED);
        messageMap.setStringProperty(TO_PARTY_TYPE, UNREGISTERED_PARTY_TYPE);
        messageMap.setStringProperty(FROM_ROLE, INITIATOR_ROLE);
        messageMap.setStringProperty(TO_ROLE, RESPONDER_ROLE);
        messageMap.setStringProperty(PROPERTY_ORIGINAL_SENDER, ORIGINAL_SENDER);
        messageMap.setStringProperty(PROPERTY_FINAL_RECIPIENT, FINAL_RECIPIENT);
        messageMap.setStringProperty(PROPERTY_FINAL_RECIPIENT_TYPE, FINAL_RECIPIENT_TYPE);
        messageMap.setStringProperty(PROTOCOL, PROTOCOL_AS4);
        messageMap.setStringProperty(AGREEMENT_REF, "customAgreement");
        messageMap.setStringProperty(AGREEMENT_REF_TYPE, "ref_type");
        messageMap.setStringProperty(PAYLOAD_1_FILENAME, FILENAME_TEST);
        messageMap.setStringProperty(PROPERTY_PREFIX + PROPERTY_TEST, "test property");

        messageMap.setJMSCorrelationID("12345");

        messageMap.setStringProperty(JMSMessageConstants.TOTAL_NUMBER_OF_PAYLOADS, "2");
        messageMap.setStringProperty(MessageFormat.format(PAYLOAD_MIME_CONTENT_ID_FORMAT, 1), PAYLOAD_ID);
        messageMap.setStringProperty(MessageFormat.format(PAYLOAD_MIME_TYPE_FORMAT, 1), DEFAULT_MT);
        messageMap.setStringProperty(MessageFormat.format(PAYLOAD_FILE_NAME_FORMAT, 1), "filename");
        messageMap.setStringProperty(MessageFormat.format(JMS_PAYLOAD_NAME_FORMAT, 1), JMS_PAYLOAD_NAME_FORMAT+"name");
        messageMap.setBytes(MessageFormat.format(PAYLOAD_NAME_FORMAT, 1), PAY_LOAD.getBytes());

        Submission objSubmission = testObj.transformToSubmission(messageMap);
        Assert.assertNotNull(objSubmission);
        assertEquals(SERVICE_NOPROCESS, objSubmission.getService());
        assertEquals(SERVICE_TYPE_TC1, objSubmission.getServiceType());
        assertEquals(ACTION_TC1LEG1, objSubmission.getAction());
        assertEquals("customAgreement", objSubmission.getAgreementRef());
        assertEquals("ref_type", objSubmission.getAgreementRefType());

        assertEquals(1, objSubmission.getFromParties().size());
        Submission.Party fromParty = objSubmission.getFromParties().iterator().next();
        assertEquals(DOMIBUS_BLUE, fromParty.getPartyId());
        assertEquals(UNREGISTERED_PARTY_TYPE, fromParty.getPartyIdType());

        assertEquals(INITIATOR_ROLE, objSubmission.getFromRole());

        assertEquals(1, objSubmission.getToParties().size());

        Submission.Party party = objSubmission.getToParties().iterator().next();
        assertEquals(DOMIBUS_RED, party.getPartyId());
        assertEquals(UNREGISTERED_PARTY_TYPE, party.getPartyIdType());

        assertEquals(RESPONDER_ROLE, objSubmission.getToRole());

        Collection<Submission.TypedProperty> messageProperties = objSubmission.getMessageProperties();
        assertEquals(3, messageProperties.size());


        assertEquals(ORIGINAL_SENDER, getMandatoryProperty(messageProperties, PROPERTY_ORIGINAL_SENDER).getValue());
        assertEquals(FINAL_RECIPIENT, getMandatoryProperty(messageProperties, PROPERTY_FINAL_RECIPIENT).getValue());
        assertEquals(FINAL_RECIPIENT_TYPE, getMandatoryProperty(messageProperties, PROPERTY_FINAL_RECIPIENT).getType());
        assertEquals("test property", getMandatoryProperty(messageProperties, PROPERTY_TEST).getValue());

        assertEquals(2, objSubmission.getPayloads().size());

        List<Submission.TypedProperty> typedProperties =
                objSubmission.getPayloads()
                        .stream()
                        .flatMap(payload -> payload.getPayloadProperties().stream())
                        .collect(toList());

        assertEquals(4, typedProperties.size());

        assertEquals(DEFAULT_MT, getMandatoryProperties(typedProperties, MIME_TYPE).get(0).getValue());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, getMandatoryProperties(typedProperties, MIME_TYPE).get(1).getValue());
        assertEquals("filename", getMandatoryProperty(typedProperties, PAYLOAD_FILENAME).getValue());
        assertEquals(JMS_PAYLOAD_NAME_FORMAT+"name", getMandatoryProperty(typedProperties, MessageConstants.PAYLOAD_PROPERTY_FILE_NAME).getValue());
    }

    /**
     * @param typedProperties from {@link Submission}
     * @param key             of the property
     * @return Submission.TypedProperty with {@param key}
     * @throws AssertionError if property not found
     */
    private Submission.TypedProperty getMandatoryProperty(Collection<Submission.TypedProperty> typedProperties, String key) {
        return typedProperties.stream()
                .filter(typedProperty -> equalsAnyIgnoreCase(key, typedProperty.getKey()))
                .findAny()
                .orElseThrow(() -> new AssertionError("Property:" + key + "Not found"));
    }
    /**
     * @param typedProperties from {@link Submission}
     * @param key             of the property
     * @return Submission.TypedProperty with {@param key}
     * @throws AssertionError if property not found
     */
    private List<Submission.TypedProperty> getMandatoryProperties(Collection<Submission.TypedProperty> typedProperties, String key) {
        return typedProperties.stream()
                .filter(typedProperty -> equalsAnyIgnoreCase(key, typedProperty.getKey()))
                .collect(toList());
    }

    /*
     * Testing for bug EDELIVERY-1371, trimming whitespaces in the transform from UserMessage to Submission of JMS transformer
     */
    @Test
    public void transformToSubmission_TrimWhiteSpaces() throws Exception {
        MapMessage messageMap = new ActiveMQMapMessage();
        messageMap.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, "submitMessage");
        messageMap.setStringProperty(JMSMessageConstants.SERVICE, "\t" + SERVICE_NOPROCESS + "   ");
        messageMap.setStringProperty(JMSMessageConstants.SERVICE_TYPE, "\t" + SERVICE_TYPE_TC1 + "    ");
        messageMap.setStringProperty(JMSMessageConstants.ACTION, "    " + ACTION_TC1LEG1 + "\t");
        messageMap.setStringProperty(JMSMessageConstants.FROM_PARTY_ID, '\t' + DOMIBUS_BLUE + "\t");
        messageMap.setStringProperty(JMSMessageConstants.FROM_PARTY_TYPE, "   " + UNREGISTERED_PARTY_TYPE + '\t');
        messageMap.setStringProperty(JMSMessageConstants.TO_PARTY_ID, "\t" + DOMIBUS_RED + "\t");
        messageMap.setStringProperty(JMSMessageConstants.TO_PARTY_TYPE, "   " + UNREGISTERED_PARTY_TYPE + "\t");
        messageMap.setStringProperty(JMSMessageConstants.FROM_ROLE, "    " + INITIATOR_ROLE + "\t");
        messageMap.setStringProperty(JMSMessageConstants.TO_ROLE, '\t' + RESPONDER_ROLE + "   ");
        messageMap.setStringProperty(JMSMessageConstants.PROPERTY_ORIGINAL_SENDER, "\t" + ORIGINAL_SENDER + "    ");
        messageMap.setStringProperty(JMSMessageConstants.PROPERTY_FINAL_RECIPIENT, "\t" + FINAL_RECIPIENT + "\t");
        messageMap.setStringProperty(JMSMessageConstants.PROTOCOL, "\t" + PROTOCOL_AS4 + "\t\t");
        messageMap.setStringProperty(JMSMessageConstants.AGREEMENT_REF, CUSTOM_AGREEMENT_REF);

        messageMap.setJMSCorrelationID("12345");

        messageMap.setStringProperty(JMSMessageConstants.TOTAL_NUMBER_OF_PAYLOADS, "1");
        messageMap.setStringProperty(MessageFormat.format(PAYLOAD_MIME_CONTENT_ID_FORMAT, 1), "\t" + PAYLOAD_ID + "   ");
        messageMap.setStringProperty(MessageFormat.format(PAYLOAD_MIME_TYPE_FORMAT, 1), "   " + DEFAULT_MT + "\t\t");
        messageMap.setBytes(MessageFormat.format(PAYLOAD_NAME_FORMAT, 1), PAY_LOAD.getBytes());

        Submission objSubmission = testObj.transformToSubmission(messageMap);
        Assert.assertNotNull("Submission object in the response should not be null:", objSubmission);
        Set<Submission.Party> fromParties = objSubmission.getFromParties();
        assertEquals(INITIATOR_ROLE, objSubmission.getFromRole());

        assertEquals(1, fromParties.size());
        Submission.Party fromParty = fromParties.iterator().next();
        assertEquals(DOMIBUS_BLUE, fromParty.getPartyId());
        assertEquals(UNREGISTERED_PARTY_TYPE, fromParty.getPartyIdType());
        assertEquals(RESPONDER_ROLE, objSubmission.getToRole());

        assertEquals(SERVICE_NOPROCESS, objSubmission.getService());
        assertEquals(SERVICE_TYPE_TC1, objSubmission.getServiceType());
        assertEquals(ACTION_TC1LEG1, objSubmission.getAction());
    }

    /*
     * Testing for bug EDELIVERY-1386, fallback to defaults for missing properties
     */
    @Test
    public void transformToSubmission_FallbackToDefaults() throws Exception {

        new NonStrictExpectations(testObj) {{
            testObj.getProperty(JMSMessageConstants.SERVICE);
            result = SERVICE_NOPROCESS;

            testObj.getProperty(JMSMessageConstants.SERVICE_TYPE);
            result = SERVICE_TYPE_TC1;

            testObj.getProperty(JMSMessageConstants.ACTION);
            result = ACTION_TC1LEG1;

            testObj.getProperty(JMSMessageConstants.FROM_ROLE);
            result = INITIATOR_ROLE;

            testObj.getProperty(JMSMessageConstants.TO_ROLE);
            result = RESPONDER_ROLE;

            testObj.getProperty(JMSMessageConstants.FROM_PARTY_ID);
            result = DOMIBUS_BLUE;

            testObj.getProperty(JMSMessageConstants.FROM_PARTY_TYPE);
            result = UNREGISTERED_PARTY_TYPE;

            testObj.getProperty(JMSMessageConstants.TO_PARTY_ID);
            result = DOMIBUS_RED;

            testObj.getProperty(JMSMessageConstants.TO_PARTY_TYPE);
            result = UNREGISTERED_PARTY_TYPE;

            testObj.getProperty(JMSMessageConstants.AGREEMENT_REF);
            result = CUSTOM_AGREEMENT_REF;
        }};

        MapMessage messageMap = new ActiveMQMapMessage();
        messageMap.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, "submitMessage");
        messageMap.setStringProperty(JMSMessageConstants.PROPERTY_ORIGINAL_SENDER, ORIGINAL_SENDER);
        messageMap.setStringProperty(JMSMessageConstants.PROPERTY_FINAL_RECIPIENT, FINAL_RECIPIENT);
        messageMap.setStringProperty(JMSMessageConstants.PROPERTY_FINAL_RECIPIENT_TYPE, FINAL_RECIPIENT_TYPE);
        messageMap.setStringProperty(JMSMessageConstants.PROTOCOL, PROTOCOL_AS4);

        messageMap.setJMSCorrelationID("12345");

        messageMap.setStringProperty(JMSMessageConstants.TOTAL_NUMBER_OF_PAYLOADS, "1");
        messageMap.setStringProperty(MessageFormat.format(PAYLOAD_MIME_CONTENT_ID_FORMAT, 1), "Content_id");
        messageMap.setStringProperty(MessageFormat.format(PAYLOAD_MIME_TYPE_FORMAT, 1), "type_format");
        messageMap.setBytes(MessageFormat.format(PAYLOAD_NAME_FORMAT, 1), PAY_LOAD.getBytes());

        Submission objSubmission = testObj.transformToSubmission(messageMap);
        Assert.assertNotNull("Submission object in the response should not be null:", objSubmission);
        Set<Submission.Party> fromParties = objSubmission.getFromParties();
        assertEquals(1, fromParties.size());
        Submission.Party fromParty = fromParties.iterator().next();
        assertEquals(DOMIBUS_BLUE, fromParty.getPartyId());
        assertEquals(UNREGISTERED_PARTY_TYPE, fromParty.getPartyIdType());
        assertEquals(INITIATOR_ROLE, objSubmission.getFromRole());

        Set<Submission.Party> toParties = objSubmission.getToParties();
        assertEquals(1, toParties.size());
        Submission.Party toParty = toParties.iterator().next();
        assertEquals(DOMIBUS_RED, toParty.getPartyId());
        assertEquals(UNREGISTERED_PARTY_TYPE, toParty.getPartyIdType());
        assertEquals(RESPONDER_ROLE, objSubmission.getToRole());

        assertEquals(SERVICE_NOPROCESS, objSubmission.getService());
        assertEquals(SERVICE_TYPE_TC1, objSubmission.getServiceType());
        assertEquals(ACTION_TC1LEG1, objSubmission.getAction());
        assertEquals(CUSTOM_AGREEMENT_REF, objSubmission.getAgreementRef());
    }

    @Test
    public void getMimeType_null() throws JMSException {
        assertGetMimeType(MediaType.APPLICATION_OCTET_STREAM, null);
    }

    @Test
    public void getMimeType_empty() throws JMSException {
        assertGetMimeType(MediaType.APPLICATION_OCTET_STREAM, "");
    }

    @Test
    public void getMimeType_spaces() throws JMSException {
        assertGetMimeType(MediaType.APPLICATION_OCTET_STREAM, "     ");
    }

    @Test
    public void getMimeType_ok() throws JMSException {
        assertGetMimeType("application/json", "  application/json   ");
    }

    private void assertGetMimeType(String expected, String actual) throws JMSException {
        new Expectations(){{
            messageIn.getStringProperty("payload_1_mimeType");
            times = 1;
            result = actual;
        }};
        String mimeType = testObj.getMimeType(messageIn, 1);
        assertEquals(expected, mimeType);
        new FullVerifications(){};
    }
}