package eu.domibus.webservice.backend;

import eu.domibus.webservice.backend.generated.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;

/**
 * @author François Gautier
 * @since 5.0
 */
@javax.jws.WebService(
        serviceName = "BackendService",
        portName = "BACKEND_PORT",
        targetNamespace = "eu.domibus",
        endpointInterface = "eu.domibus.webservice.backend.generated.BackendInterface")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class BackendWebservice implements BackendInterface {
    private static final Logger LOG = LoggerFactory.getLogger(BackendWebservice.class);
    private final ObjectFactory objectFactory = new ObjectFactory();

    @Override
    public void submitMessage(SubmitMessage submitMessage) throws SubmitMessageFault {
        String messageID = submitMessage.getMessageID();
        LOG.info("SubmitMessage received for id [{}]. Payload id [{}], contentType [{}]. [{}] Payload",
                messageID,
                submitMessage.getBodyload().getContentType(),
                submitMessage.getBodyload().getPayloadId(),
                submitMessage.getPayload().size());
        if (StringUtils.containsIgnoreCase(messageID, "err")) {
            throw new SubmitMessageFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
    }

    @Override
    public void sendSuccess(SendSuccess sendSuccess) throws SendSuccessFault {
        String messageID = sendSuccess.getMessageID();
        LOG.info("SendSuccess received for id [{}]", messageID);
        if (StringUtils.containsIgnoreCase(messageID, "err")) {
            throw new SendSuccessFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
    }

    private String getErrorMessage(String messageID) {
        return "Error for message [" + messageID + "]";
    }

    @Override
    public void sendFailure(SendFailure sendSuccess) throws SendFailureFault {
        String messageID = sendSuccess.getMessageID();
        LOG.info("sendFailure received for id [{}]", messageID);
        if (StringUtils.containsIgnoreCase(messageID, "err")) {
            throw new SendFailureFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
    }

    @Override
    public void receiveSuccess(ReceiveSuccess sendSuccess) throws ReceiveSuccessFault {
        String messageID = sendSuccess.getMessageID();
        LOG.info("ReceiveSuccess received for id [{}]", messageID);
        if (StringUtils.containsIgnoreCase(messageID, "err")) {
            throw new ReceiveSuccessFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
    }

    @Override
    public void receiveFailure(ReceiveFailure sendSuccess) throws ReceiveFailureFault {
        String messageID = sendSuccess.getMessageID();
        LOG.info("receiveFailure received for id [{}]", messageID);
        if (StringUtils.containsIgnoreCase(messageID, "err")) {
            throw new ReceiveFailureFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
    }

    @Override
    public void messageStatusChange(MessageStatusChange messageStatusChange) throws MessageStatusChangeFault {
        String messageID = messageStatusChange.getMessageID();
        LOG.info("messageStatusChange received for id [{}] and status [{}]", messageID, messageStatusChange.getMessageStatus());
        if (StringUtils.containsIgnoreCase(messageID, "err")) {
            throw new MessageStatusChangeFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
    }

    private BackendFaultDetail getDefaultFaultDetail() {
        BackendFaultDetail faultInfo = objectFactory.createBackendFaultDetail();
        faultInfo.setCode("0001");
        faultInfo.setMessage("ERROR_0001");
        return faultInfo;
    }
}
