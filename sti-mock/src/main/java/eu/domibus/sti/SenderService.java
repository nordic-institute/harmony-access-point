package eu.domibus.sti;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.uuid.NoArgGenerator;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;
import eu.domibus.ext.domain.PartInfoDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.LargePayloadType;
import eu.domibus.plugin.webService.generated.SubmitMessageFault;
import eu.domibus.plugin.webService.generated.SubmitRequest;
/*import eu.domibus.rest.client.ApiException;
import eu.domibus.rest.client.api.UsermessageApi;
import eu.domibus.rest.client.model.*;*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;

import javax.activation.DataHandler;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SenderService {

    private static final Logger LOG = LoggerFactory.getLogger(SenderService.class);

    private final static String ORIGINAL_SENDER = "originalSender";

    private final static String FINAL_RECIPIENT = "finalRecipient";

    private static final String CID_MESSAGE = "cid:message";

    private static final String MIME_TYPE = "MimeType";

    private static final String TEXT_XML = "text/xml";

    private final static String HAPPY_FLOW_MESSAGE_TEMPLATE = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<response_to_message_id>\n" +
            " $messId\n" +
            "</response_to_message_id>\n" +
            "<dataset>\n" +
            "This is a test for 500 bytes sent from a Weblogic cluster to a single Wildfly instance using Domibus." +
            "This is a test for 500 bytes sent from a Weblogic cluster to a single Wildfly instance using Domibus." +
            "This is ... \n" +
            "</dataset>";

    private JmsTemplate jmsTemplate;

    private BackendInterface backendInterface;

    private MetricRegistry metricRegistry;

    @Value("${send.with.jms}")
    private Boolean sendWithJms;

    @Value("${send.metadata.only}")
    private Boolean sendMetadataOnly;


    //protected UsermessageApi usermessageApi;

    protected NoArgGenerator uuidGenerator;

    public SenderService(JmsTemplate jmsTemplate,
                         /*BackendInterface backendInterface,*/
                         MetricRegistry metricRegistry,
                         //UsermessageApi usermessageApi,
                         NoArgGenerator uuidGenerator) {
        this.jmsTemplate = jmsTemplate;
        this.backendInterface = backendInterface;
        this.metricRegistry = metricRegistry;
        //this.usermessageApi = usermessageApi;
        this.uuidGenerator = uuidGenerator;
    }

    //@Async("threadPoolTaskExecutor")
    public void reverseAndSend(MapMessage mapMessage) {
        if (sendWithJms) {
            LOG.debug("Reverse and send message through jms in queue");
            jmsTemplate.send(session -> prepareResponse(mapMessage, session));
        } else {
            /*try {
                LOG.debug("Reverse and send message through webservice in queue");

                Submission submission = prepareSubmission(mapMessage);
                backendInterface.submitMessage(submission.getSubmitRequest(), submission.getMessaging());
            } catch (JMSException e) {
                LOG.error("Error preparing response message", e);
            } catch (SubmitMessageFault submitMessageFault) {
                LOG.error("Error submitting message", submitMessageFault);
            } catch (Exception e) {
                LOG.error("Error submitting message", e);
            }*/
        }
    }

    private MapMessage prepareResponse(MapMessage received, Session session) throws JMSException {

        MapMessage messageMap = session.createMapMessage();

        // Declare message as submit
        messageMap.setStringProperty("username", "plugin_admin");
        messageMap.setStringProperty("password", "123456");

        messageMap.setStringProperty("messageType", "submitMessage");
        messageMap.setStringProperty("messageId", uuidGenerator.generate().toString());
        final String messageId = received.getStringProperty("messageId");

        messageMap.setStringProperty("refToMessageId", messageId);

        messageMap.setStringProperty("service", "eu_ics2_c2t");
        messageMap.setStringProperty("agreementRef", "EU-ICS2-TI-V1.0");


        messageMap.setStringProperty("action", "IE3R01");
        messageMap.setStringProperty("fromPartyId", received.getStringProperty("toPartyId"));
        messageMap.setStringProperty("fromPartyType", received.getStringProperty("toPartyType")); // Mandatory

        messageMap.setStringProperty("fromRole", received.getStringProperty("toRole"));

        messageMap.setStringProperty("toPartyId", received.getStringProperty("fromPartyId"));
        messageMap.setStringProperty("toPartyType", received.getStringProperty("fromPartyType")); // Mandatory

        messageMap.setStringProperty("toRole", received.getStringProperty("fromRole"));

        messageMap.setStringProperty("originalSender", received.getStringProperty("finalRecipient"));
        messageMap.setStringProperty("finalRecipient", received.getStringProperty("originalSender"));
        messageMap.setStringProperty("protocol", "AS4");

        // messageMap.setJMSCorrelationID("12345");
        //Set up the payload properties
        LOG.info("Send metadata only is " + sendMetadataOnly);

        messageMap.setStringProperty("totalNumberOfPayloads", "1");
        messageMap.setStringProperty("payload_1_description", "message");
        messageMap.setStringProperty("payload_1_mimeContentId", "cid:message");
        messageMap.setStringProperty("payload_1_mimeType", "text/xml");

        if(!sendMetadataOnly) {
            LOG.info("Adding happy flow payload to the map message");
            String response = HAPPY_FLOW_MESSAGE_TEMPLATE.replace("$messId", messageId);

            //messageMap.setStringProperty("p1InBody", "true"); // If true payload_1 will be sent in the body of the AS4 message. Only XML payloads may be sent in the AS4 message body. Optional

            //send the payload in the JMS message as byte array
            //byte[] payload = response.getBytes();
            byte[] payload = org.apache.commons.codec.binary.Base64.encodeBase64(response.getBytes());
            messageMap.setBytes("payload_1", payload);
        }
        return messageMap;

    }

    /*private Submission prepareSubmission(MapMessage received) throws JMSException, ApiException {
        final String messageId = received.getStringProperty("messageId");

        UserMessageDTO userMessageDTO = usermessageApi.getUserMessage(messageId);//TODO put the payloads in the REST message
        List<PartInfoDTO> partInfo = userMessageDTO.getPayloadInfo().getPartInfo();
        for (PartInfoDTO partInfoDTO : partInfo) {
            String payload = partInfoDTO.getPayload();
            byte[] asBytes = Base64.getDecoder().decode(payload);
            try {
                String resultAsStringAgain = new String(asBytes, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        Optional<PropertyDTO> finalRecipientOptional = userMessageDTO.getMessageProperties().getProperty().stream().filter(propertyDTO -> "finalRecipient".equals(propertyDTO.getName())).findFirst();
        String finalRecipient = null;
        if(finalRecipientOptional.isPresent()) {
            finalRecipient = finalRecipientOptional.get().getValue();
        }

        Optional<PropertyDTO> originalSenderOptional = userMessageDTO.getMessageProperties().getProperty().stream().filter(propertyDTO -> "originalSender".equals(propertyDTO.getName())).findFirst();
        String originalSender = null;
        if(originalSenderOptional.isPresent()) {
            originalSender = originalSenderOptional.get().getValue();
        }
        FromDTO from = userMessageDTO.getPartyInfo().getFrom();
        String fromRole = from.getRole();
        ToDTO to = userMessageDTO.getPartyInfo().getTo();
        String toRole = to.getRole();

        String response = HAPPY_FLOW_MESSAGE_TEMPLATE.replace("$messId", messageId);


        //create payload.
        LargePayloadType payloadType = new LargePayloadType();
        payloadType.setPayloadId(CID_MESSAGE);
        payloadType.setContentType(MediaType.TEXT_XML);
        payloadType.setValue(getPayload(response, MediaType.TEXT_XML));

        //setup submit request.
        SubmitRequest submitRequest = new SubmitRequest();
        submitRequest.getPayload().add(payloadType);

        //setup messaging.
        Messaging messaging = new Messaging();
        UserMessage userMessage = new UserMessage();
        MessageInfo responseMessageInfo = new MessageInfo();
        //responseMessageInfo.setMessageId(UUID.randomUUID() + "@domibus");
        responseMessageInfo.setRefToMessageId(messageId);

        userMessage.setMessageInfo(responseMessageInfo);
        PartyInfo partyInfo = new PartyInfo();
        userMessage.setPartyInfo(partyInfo);


        From responseFrom = new From();
        responseFrom.setRole(toRole);
        partyInfo.setFrom(responseFrom);
        PartyId responseFromPartyId = new PartyId();

        PartyIdDTO toPartyIdDTO = to.getPartyId().stream().findFirst().get();
        final String toPartyId = toPartyIdDTO.getValue();
        final String toPartyIdType = toPartyIdDTO.getType();
        responseFromPartyId.setValue(toPartyId);
        responseFromPartyId.setType(toPartyIdType);
        responseFrom.setPartyId(responseFromPartyId);

        To responseTo = new To();
        responseTo.setRole(fromRole);
        partyInfo.setTo(responseTo);
        PartyId responseToPartyId = new PartyId();


        PartyIdDTO fromPartyIdDTO = from.getPartyId().stream().findFirst().get();
        responseToPartyId.setValue(fromPartyIdDTO.getValue());
        responseToPartyId.setType(fromPartyIdDTO.getType());
        responseTo.setPartyId(responseToPartyId);

        CollaborationInfo collaborationInfo = new CollaborationInfo();
        Service responseService = new Service();
        responseService.setValue("eu_ics2_c2t");
        AgreementRef agreementRef = new AgreementRef();
        agreementRef.setValue("EU-ICS2-TI-V1.0");
        collaborationInfo.setAgreementRef(agreementRef);
        collaborationInfo.setService(responseService);
        collaborationInfo.setAction("IE3R01");
        userMessage.setCollaborationInfo(collaborationInfo);

        MessageProperties responseMessageProperties = new MessageProperties();
        userMessage.setMessageProperties(responseMessageProperties);

        Property responseOriginalSender = new Property();
        responseMessageProperties.getProperty().add(responseOriginalSender);
        responseOriginalSender.setName(ORIGINAL_SENDER);
        responseOriginalSender.setValue(finalRecipient);

        Property responseFinalRecipient = new Property();
        responseMessageProperties.getProperty().add(responseFinalRecipient);
        responseFinalRecipient.setName(FINAL_RECIPIENT);
        responseFinalRecipient.setValue(originalSender);

        PayloadInfo responsePayloadInfo = new PayloadInfo();
        userMessage.setPayloadInfo(responsePayloadInfo);

        PartInfo responsePartInfo = new PartInfo();
        responsePayloadInfo.getPartInfo().add(responsePartInfo);
        responsePartInfo.setHref(CID_MESSAGE);
        PartProperties responsePartProperty = new PartProperties();
        Property responsePartInfoProperty = new Property();
        responsePartProperty.getProperty().add(responsePartInfoProperty);
        responsePartInfo.setPartProperties(responsePartProperty);
        responsePartInfoProperty.setName(MIME_TYPE);
        responsePartInfoProperty.setValue(TEXT_XML);
        messaging.setUserMessage(userMessage);
        return new Submission(submitRequest, messaging);
    }*/

    private DataHandler getPayload(final String payloadContent, final String mediaType) {
        javax.mail.util.ByteArrayDataSource dataSource = null;
        dataSource = new javax.mail.util.ByteArrayDataSource(org.apache.commons.codec.binary.Base64.encodeBase64(payloadContent.getBytes()), mediaType);
        dataSource.setName("content.xml");
        return new DataHandler(dataSource);
    }

    static class Submission {
        private SubmitRequest submitRequest;
        private Messaging messaging;

        public Submission(SubmitRequest submitRequest, Messaging messaging) {
            this.submitRequest = submitRequest;
            this.messaging = messaging;
        }

        public SubmitRequest getSubmitRequest() {
            return submitRequest;
        }

        public Messaging getMessaging() {
            return messaging;
        }
    }
}
