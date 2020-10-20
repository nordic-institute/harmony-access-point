package eu.domibus.webservice.backend;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.webservice.backend.generated.*;
import org.apache.commons.lang3.StringUtils;

import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@javax.jws.WebService(
        serviceName = "BackendService",
        portName = "BACKEND_PORT",
        targetNamespace = "eu.europa.ec.eu.edelivery.domibus",
        endpointInterface = "eu.domibus.webservice.backend.generated.BackendInterface")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class BackendClient implements BackendInterface {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendClient.class);

    @Override
    public List<String> sendSuccess(String messageID) throws SendSuccessFault {
        LOG.info("SendSuccess received for id [{}]", messageID);
        if(StringUtils.containsIgnoreCase(messageID, "err")){
            throw new SendSuccessFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
        return Collections.singletonList(UUID.randomUUID().toString());
    }

    private String getErrorMessage(String messageID) {
        return "Error for message [" + messageID + "]";
    }

    @Override
    public List<String> sendFailure(String messageID) throws SendFailureFault {
        LOG.info("sendFailure received for id [{}]", messageID);
        if(StringUtils.containsIgnoreCase(messageID, "err")){
            throw new SendFailureFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
        return Collections.singletonList(UUID.randomUUID().toString());
    }

    @Override
    public List<String> receiveSuccess(String messageID) throws ReceiveSuccessFault {
        LOG.info("ReceiveSuccess received for id [{}]", messageID);
        if(StringUtils.containsIgnoreCase(messageID, "err")){
            throw new ReceiveSuccessFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
        return Collections.singletonList(UUID.randomUUID().toString());
    }

    @Override
    public List<String> receiveFailure(String messageID) throws ReceiveFailureFault {
        LOG.info("receiveFailure received for id [{}]", messageID);
        if(StringUtils.containsIgnoreCase(messageID, "err")){
            throw new ReceiveFailureFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
        return Collections.singletonList(UUID.randomUUID().toString());
    }

    @Override
    public List<String> messageStatusChange(String messageID, MessageStatus messageStatus) throws MessageStatusChangeFault {
        LOG.info("messageStatusChange received for id [{}] and status [{}]", messageID, messageStatus);
        if(StringUtils.containsIgnoreCase(messageID, "err")){
            throw new MessageStatusChangeFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
        return Collections.singletonList(UUID.randomUUID().toString());
    }

    private FaultDetail getDefaultFaultDetail() {
        FaultDetail faultInfo = new FaultDetail();
        faultInfo.setCode("0001");
        faultInfo.setMessage("ERROR_0001");
        return faultInfo;
    }
}
