package eu.domibus.webservice.backend;

import eu.domibus.webservice.backend.generated.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;


@javax.jws.WebService(
        serviceName = "BackendService",
        portName = "BACKEND_PORT",
        targetNamespace = "eu.europa.ec.eu.edelivery.domibus",
        endpointInterface = "eu.domibus.webservice.backend.generated.BackendInterface")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class BackendWebservice implements BackendInterface {
    private static final Logger LOG = LoggerFactory.getLogger(BackendWebservice.class);
    private final ObjectFactory objectFactory = new ObjectFactory();

    @Override
    public Object sendSuccess(SendSuccess sendSuccess) throws SendSuccessFault {
        String messageID = sendSuccess.getMessageID();
        LOG.info("SendSuccess received for id [{}]", messageID);
        if (StringUtils.containsIgnoreCase(messageID, "err")) {
            throw new SendSuccessFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
        return null;
    }

    private String getErrorMessage(String messageID) {
        return "Error for message [" + messageID + "]";
    }

    @Override
    public Object sendFailure(SendFailure sendSuccess) throws SendFailureFault {
        String messageID = sendSuccess.getMessageID();
        LOG.info("sendFailure received for id [{}]", messageID);
        if (StringUtils.containsIgnoreCase(messageID, "err")) {
            throw new SendFailureFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
        return null;
    }

    @Override
    public Object receiveSuccess(ReceiveSuccess sendSuccess) throws ReceiveSuccessFault {
        String messageID = sendSuccess.getMessageID();
        LOG.info("ReceiveSuccess received for id [{}]", messageID);
        if (StringUtils.containsIgnoreCase(messageID, "err")) {
            throw new ReceiveSuccessFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
        return null;
    }

    @Override
    public Object receiveFailure(ReceiveFailure sendSuccess) throws ReceiveFailureFault {
        String messageID = sendSuccess.getMessageID();
        LOG.info("receiveFailure received for id [{}]", messageID);
        if (StringUtils.containsIgnoreCase(messageID, "err")) {
            throw new ReceiveFailureFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
        return null;
    }

    @Override
    public Object messageStatusChange(MessageStatusChange messageStatusChange) throws MessageStatusChangeFault {
        String messageID = messageStatusChange.getMessageID();
        LOG.info("messageStatusChange received for id [{}] and status [{}]", messageID, messageStatusChange.getMessageStatus());
        if (StringUtils.containsIgnoreCase(messageID, "err")) {
            throw new MessageStatusChangeFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
        return null;
    }

    private BackendFaultDetail getDefaultFaultDetail() {
        BackendFaultDetail faultInfo = objectFactory.createBackendFaultDetail();
        faultInfo.setCode("0001");
        faultInfo.setMessage("ERROR_0001");
        return faultInfo;
    }
}
