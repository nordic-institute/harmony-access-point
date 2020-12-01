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
        LOG.info("SubmitMessage received for id [{}]. Bodyload [{}]. [{}] Payload(s)",
                messageID,
                submitMessage.getBodyload(),
                submitMessage.getPayload().size());
//        for (LargePayloadType entry : submitMessage.getPayload()) {
//            FSPayload fsPayload = entry.getValue();
//            DataHandler dataHandler = entry.getValue();
//            String contentId = entry.getKey();
//            String fileName = getFileName(contentId, fsPayload);
//
//            try (FileObject fileObject = incomingFolderByMessageId.resolveFile(fileName);
//                 FileContent fileContent = fileObject.getContent()) {
//                dataHandler.writeTo(fileContent.getOutputStream());
//                LOG.info("Message payload with cid [{}] received: [{}]", contentId, fileObject.getName());
//            } catch (IOException e) {
//                throw new IllegalStateException("An error occurred persisting downloaded message " + messageId, e);
//            }
//        }
        if (StringUtils.containsIgnoreCase(messageID, "err")) {
            throw new SubmitMessageFault(getErrorMessage(messageID), getDefaultFaultDetail());
        }
    }
//    protected String getFileName(String contentId, FSPayload fsPayload) {
//        //original name + extension
//        String fileName = fsPayload.getFileName();
//
//        //contentId file name - if the parsing of the received fileName fails we will return this
//        final String fileNameContentId = getFileNameContentIdBase(contentId) + getFileNameExtension(fsPayload.getMimeType());
//
//        //received payloadName is empty, returning the content Id based one
//        if (StringUtils.isBlank(fileName)) {
//            LOG.debug("received payload filename is empty, returning contentId based one=[{}]", fileNameContentId);
//            return fileNameContentId;
//        }
//
//        String decodedFileName;
//        try {
//            decodedFileName = UriParser.decode(fileName);
//        } catch (FileSystemException e) {
//            LOG.error("Error while decoding the fileName=[{}], returning contentId based one=[{}]", fileName, fileNameContentId, e);
//            return fileNameContentId;
//        }
//        if (decodedFileName != null && !StringUtils.equals(fileName, decodedFileName)) {
//            //we have an encoded fileName
//            fileName = decodedFileName;
//            LOG.debug("fileName value decoded to=[{}]", decodedFileName);
//        }
//
//        try (FileObject fileObject = incomingFolderByMessageId.resolveFile(fileName, NameScope.CHILD)) {
//        } catch (FileSystemException e) {
//            LOG.warn("invalid fileName or outside the parent folder=[{}], returning contentId based one=[{}]", fileName, fileNameContentId);
//            return fileNameContentId;
//        }
//        LOG.debug("returned fileName=[{}]", fileName);
//        return fileName;
//    }


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
