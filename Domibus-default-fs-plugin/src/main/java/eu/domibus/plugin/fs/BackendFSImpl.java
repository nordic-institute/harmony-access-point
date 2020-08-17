package eu.domibus.plugin.fs;

import eu.domibus.common.*;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainTaskExtExecutor;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.fs.ebms3.Property;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSPluginException;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.fs.worker.FSDomainService;
import eu.domibus.plugin.fs.worker.FSProcessFileService;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.activation.DataHandler;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static eu.domibus.common.MessageStatus.*;

/**
 * File system backend integration plugin.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class BackendFSImpl extends AbstractBackendConnector<FSMessage, FSMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendFSImpl.class);


    protected static final String FILENAME_SANITIZE_REGEX = "[^\\w@.-]";
    protected static final String FILENAME_SANITIZE_REPLACEMENT = "_";

    protected static final Set<MessageStatus> SENDING_MESSAGE_STATUSES = EnumSet.of(
            READY_TO_SEND, SEND_ENQUEUED, SEND_IN_PROGRESS, WAITING_FOR_RECEIPT,
            WAITING_FOR_RETRY, SEND_ATTEMPT_FAILED
    );

    protected static final Set<MessageStatus> SEND_SUCCESS_MESSAGE_STATUSES = EnumSet.of(
            ACKNOWLEDGED, ACKNOWLEDGED_WITH_WARNING
    );

    protected static final Set<MessageStatus> SEND_FAILED_MESSAGE_STATUSES = EnumSet.of(
            SEND_FAILURE
    );


    // receiving statuses should be REJECTED, RECEIVED_WITH_WARNINGS, DOWNLOADED, DELETED, RECEIVED

    @Autowired
    protected FSMessageTransformer defaultTransformer;

    @Autowired
    protected FSFilesManager fsFilesManager;

    @Autowired
    protected FSPluginProperties fsPluginProperties;

    @Autowired
    protected FSSendMessagesService fsSendMessagesService;

    @Autowired
    protected FSProcessFileService fsProcessFileService;

    @Autowired
    protected DomainTaskExtExecutor domainTaskExtExecutor;

    @Autowired
    protected FSDomainService fsDomainService;

    @Autowired
    protected FSXMLHelper fsxmlHelper;

    @Autowired
    protected FSMimeTypeHelper fsMimeTypeHelper;

    @Autowired
    protected FSFileNameHelper fsFileNameHelper;

    /**
     * Creates a new <code>BackendFSImpl</code>.
     *
     * @param name Connector name
     */
    public BackendFSImpl(String name) {
        super(name);
    }

    /**
     * The implementations of the transformer classes are responsible for
     * transformation between the native backend formats and
     * eu.domibus.plugin.Submission.
     *
     * @return MessageSubmissionTransformer
     */
    @Override
    public MessageSubmissionTransformer<FSMessage> getMessageSubmissionTransformer() {
        return this.defaultTransformer;
    }

    /**
     * The implementations of the transformer classes are responsible for
     * transformation between the native backend formats and
     * eu.domibus.plugin.Submission.
     *
     * @return MessageRetrievalTransformer
     */
    @Override
    public MessageRetrievalTransformer<FSMessage> getMessageRetrievalTransformer() {
        return this.defaultTransformer;
    }

    @Override
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void deliverMessage(String messageId) {
        LOG.debug("Delivering File System Message [{}]", messageId);
        FSMessage fsMessage;

        // Browse message
        try {
            fsMessage = browseMessage(messageId, null);
        } catch (MessageNotFoundException e) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RETRIEVE_FAILED, e);
            throw new FSPluginException("Unable to browse message " + messageId, e);
        }

        //extract final recipient
        final String finalRecipient = getFinalRecipient(fsMessage.getMetadata());
        if (StringUtils.isBlank(finalRecipient)) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RETRIEVE_FAILED);
            throw new FSPluginException("Unable to extract finalRecipient from message " + messageId);
        }
        final String finalRecipientFolder = sanitizeFileName(finalRecipient);
        final String messageIdFolder = sanitizeFileName(messageId);

        String fsPluginDomain = fsDomainService.getFSPluginDomain(fsMessage);
        LOG.debug("Using FS Plugin domain [{}]", fsPluginDomain);

        // Persist message
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(fsPluginDomain);
             FileObject incomingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.INCOMING_FOLDER);
             FileObject incomingFolderByRecipient = fsFilesManager.getEnsureChildFolder(incomingFolder, finalRecipientFolder);
             FileObject incomingFolderByMessageId = fsFilesManager.getEnsureChildFolder(incomingFolderByRecipient, messageIdFolder)) {

            //let's write the metadata file first
            try (FileObject fileObject = incomingFolderByMessageId.resolveFile(FSSendMessagesService.METADATA_FILE_NAME);
                 FileContent fileContent = fileObject.getContent()) {

                writeMetadata(fileContent.getOutputStream(), fsMessage.getMetadata());
                LOG.info("Message metadata file written at: [{}]", fileObject.getName().getURI());
            }

            final boolean scheduleFSMessagePayloadsSaving = scheduleFSMessagePayloadsSaving(fsMessage, fsPluginDomain);
            if (scheduleFSMessagePayloadsSaving) {
                LOG.debug("FSMessage payloads for message [{}] will be scheduled for saving", messageId);

                final DomainDTO domainDTO = fsDomainService.fsDomainToDomibusDomain(fsPluginDomain);
                final Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
                domainTaskExtExecutor.submitLongRunningTask(() -> {
                    SecurityContextHolder.getContext().setAuthentication(currentAuthentication);
                    writePayloads(messageId, fsMessage, incomingFolderByMessageId);
                }, domainDTO);
            } else {
                writePayloads(messageId, fsMessage, incomingFolderByMessageId);
            }
        } catch (JAXBException ex) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RETRIEVE_FAILED, ex);
            throw new FSPluginException("An error occurred while writing metadata for downloaded message " + messageId, ex);
        } catch (IOException | FSSetUpException ex) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RETRIEVE_FAILED, ex);
            throw new FSPluginException("An error occurred persisting downloaded message " + messageId, ex);
        }
    }


    protected void writePayloads(String messageId, FSMessage fsMessage, FileObject incomingFolderByMessageId) throws FSPluginException {
        LOG.debug("Writing payloads for message [{}]", messageId);

        //write payloads
        for (Map.Entry<String, FSPayload> entry : fsMessage.getPayloads().entrySet()) {
            FSPayload fsPayload = entry.getValue();
            DataHandler dataHandler = fsPayload.getDataHandler();
            String contentId = entry.getKey();
            String fileName = getFileName(contentId, fsPayload, incomingFolderByMessageId);

            try (FileObject fileObject = incomingFolderByMessageId.resolveFile(fileName);
                 FileContent fileContent = fileObject.getContent()) {
                dataHandler.writeTo(fileContent.getOutputStream());
                LOG.info("Message payload with cid [{}] received: [{}]", contentId, fileObject.getName());
            } catch (IOException e) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RETRIEVE_FAILED, e);
                throw new FSPluginException("An error occurred persisting downloaded message " + messageId, e);
            }
        }
        // Downloads message
        try {
            downloadMessage(messageId, null);
        } catch (MessageNotFoundException e) {
            throw new FSPluginException("Unable to download message " + messageId, e);
        }
    }

    /**
     * Checks if the message payloads will be scheduled(async) or directly(sync) saved
     *
     * @param fsMessage The message payloads to be checked
     * @param domain    The current domain
     * @return true if the payloads will be scheduled for saving
     */
    protected boolean scheduleFSMessagePayloadsSaving(FSMessage fsMessage, String domain) {
        final Map<String, FSPayload> payloads = fsMessage.getPayloads();
        if (payloads == null || payloads.isEmpty()) {
            LOG.debug("FSMessage does not have any payloads");
            return false;
        }

        long totalPayloadLength = 0;
        for (FSPayload fsPayload : payloads.values()) {
            totalPayloadLength += fsPayload.getFileSize();
        }

        LOG.debug("FSMessage payloads totalPayloadLength(bytes) [{}]", totalPayloadLength);

        final Long payloadsScheduleThresholdMB = fsPluginProperties.getPayloadsScheduleThresholdMB(domain);
        LOG.debug("Using configured payloadsScheduleThresholdMB [{}]", payloadsScheduleThresholdMB);

        final Long payloadsScheduleThresholdBytes = payloadsScheduleThresholdMB * FileUtils.ONE_MB;
        if (totalPayloadLength > payloadsScheduleThresholdBytes) {
            LOG.debug("FSMessage payloads size [{}] is bigger than configured payloadsScheduleThresholdBytes [{}]", totalPayloadLength, payloadsScheduleThresholdBytes);
            return true;
        }
        return false;

    }

    protected String getFileName(String contentId, FSPayload fsPayload, FileObject incomingFolderByMessageId) {
        //original name + extension
        String fileName = fsPayload.getFileName();

        //contentId file name - if the parsing of the received fileName fails we will return this
        final String fileNameContentId = getFileNameContentIdBase(contentId) + getFileNameExtension(fsPayload.getMimeType());

        //received payloadName is empty, returning the content Id based one
        if (StringUtils.isBlank(fileName)) {
            LOG.debug("received payload filename is empty, returning contentId based one=[{}]", fileNameContentId);
            return fileNameContentId;
        }

        String decodedFileName;
        try {
            decodedFileName = UriParser.decode(fileName);
        } catch (FileSystemException e) {
            LOG.error("Error while decoding the fileName=[{}], returning contentId based one=[{}]", fileName, fileNameContentId, e);
            return fileNameContentId;
        }
        if (decodedFileName != null && !StringUtils.equals(fileName, decodedFileName)) {
            //we have an encoded fileName
            fileName = decodedFileName;
            LOG.debug("fileName value decoded to=[{}]", decodedFileName);
        }

        try (FileObject fileObject = incomingFolderByMessageId.resolveFile(fileName, NameScope.CHILD)) {
        } catch (FileSystemException e) {
            LOG.warn("invalid fileName or outside the parent folder=[{}], returning contentId based one=[{}]", fileName, fileNameContentId);
            return fileNameContentId;
        }
        LOG.debug("returned fileName=[{}]", fileName);
        return fileName;
    }

    protected String getFileNameContentIdBase(String contentId) {
        if(StringUtils.isBlank(contentId)){
            String randomUUID = UUID.randomUUID().toString();
            LOG.debug("received contentId is blank, generating alternate FileNameContentIdBase: [{}]", randomUUID);
            return randomUUID;
        }
        return contentId.replaceFirst("cid:", StringUtils.EMPTY);
    }

    protected String getFileNameExtension(String mimeType) {
        String extension = StringUtils.EMPTY;
        try {
            extension = fsMimeTypeHelper.getExtension(mimeType);
        } catch (MimeTypeException ex) {
            LOG.warn("Error parsing MIME type", ex);
        }
        return extension;
    }


    @Override
    public void payloadSubmittedEvent(PayloadSubmittedEvent event) {
        LOG.debug("Handling PayloadSubmittedEvent [{}]", event);
    }

    @Override
    public void payloadProcessedEvent(PayloadProcessedEvent event) {
        LOG.debug("Handling PayloadProcessedEvent [{}]", event);
        try {
            FileObject fileObject = fsFilesManager.getEnsureRootLocation(event.getFileName());
            fsProcessFileService.renameProcessedFile(fileObject, event.getMessageId());
            fsFilesManager.deleteLockFile(fileObject);
        } catch (FileSystemException e) {
            LOG.error("Error handling PayloadProcessedEvent", e);
        }
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
        // No-op
        // Probably, the AbstractBackendConnector should not throw the UnsupportedOperationException
    }

    @Override
    public void messageSendSuccess(String messageId) {
        // Implemented in messageStatusChanged to avoid event collision and use improved API
        // Probably, the AbstractBackendConnector should not throw the UnsupportedOperationException
    }

    @Override
    public void messageSendFailed(String messageId) {
        // Implemented in messageStatusChanged to avoid event collision and use improved API
        // Probably, the AbstractBackendConnector should implement a default no-op
    }

    protected void handleSendFailedMessage(String domain, String messageId) {
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
             FileObject outgoingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
             FileObject targetFileMessage = findMessageFile(outgoingFolder, messageId)) {

            fsSendMessagesService.handleSendFailedMessage(targetFileMessage, domain, getErrorMessage(messageId));

        } catch (IOException e) {
            throw new FSPluginException("Error handling the send failed message file " + messageId, e);
        }
    }


    protected String getErrorMessage(String messageId) throws IOException {
        List<ErrorResult> errors = super.getErrorsForMessage(messageId);
        String content;
        if (!errors.isEmpty()) {
            ErrorResult lastError = errors.get(errors.size() - 1);
            content = String.valueOf(getErrorFileContent(lastError));
        } else {
            // This might occur when the destination host is unreachable
            content = "Error detail information is not available";
            LOG.error("[{}] for [{}]", content, messageId);
        }
        return content;
    }


    protected StringBuilder getErrorFileContent(ErrorResult errorResult) {

        return fsSendMessagesService.buildErrorMessage(errorResult.getErrorCode() == null ? null : errorResult.getErrorCode().getErrorCodeName(),
                errorResult.getErrorDetail(),
                errorResult.getMessageInErrorId(),
                errorResult.getMshRole() == null ? null : errorResult.getMshRole().toString(),
                errorResult.getNotified() == null ? null : errorResult.getNotified().toString(),
                errorResult.getTimestamp() == null ? null : errorResult.getTimestamp().toString());
    }

    protected void handleSentMessage(String domain, String messageId) {
        LOG.debug("Preparing to handle sent message using domain [{}] and messageId [{}]", domain, messageId);

        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
             FileObject outgoingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
             FileObject targetFileMessage = findMessageFile(outgoingFolder, messageId)) {

            if (targetFileMessage != null) {
                if (fsPluginProperties.isSentActionDelete(domain)) {
                    //Delete
                    fsFilesManager.deleteFile(targetFileMessage);
                    LOG.debug("Successfully sent message file [{}] was deleted", messageId);
                } else if (fsPluginProperties.isSentActionArchive(domain)) {
                    // Archive
                    String targetFileMessageURI = targetFileMessage.getParent().getName().getPath();
                    String sentDirectoryLocation = fsFileNameHelper.deriveSentDirectoryLocation(targetFileMessageURI);
                    FileObject sentDirectory = fsFilesManager.getEnsureChildFolder(rootDir, sentDirectoryLocation);

                    String baseName = targetFileMessage.getName().getBaseName();
                    String newName = fsFileNameHelper.stripStatusSuffix(baseName);
                    FileObject archivedFile = sentDirectory.resolveFile(newName);
                    fsFilesManager.moveFile(targetFileMessage, archivedFile);

                    LOG.debug("Successfully sent message file [{}] was archived into [{}]", messageId, archivedFile.getName().getURI());
                }
            } else {
                LOG.error("The successfully sent message file [{}] was not found in domain [{}]", messageId, domain);
            }
        } catch (FileSystemException e) {
            LOG.error("Error handling the successfully sent message file [" + messageId + "]", e);
        }
    }

    protected FileObject findMessageFile(FileObject parentDir, String messageId) throws FileSystemException {
        LOG.debug("Finding message file in directory [{}] for message [{}]", parentDir.getName().getPath(), messageId);

        FileObject[] files = fsFilesManager.findAllDescendantFiles(parentDir);
        try {
            FileObject targetFile = null;
            for (FileObject file : files) {
                String baseName = file.getName().getBaseName();
                if (FSFileNameHelper.isMessageRelated(baseName, messageId)) {
                    targetFile = file;
                    LOG.debug("Found message file [{}] for message [{}]", targetFile.getName().getPath(), messageId);
                    break;
                }
            }
            return targetFile;
        } finally {
            fsFilesManager.closeAll(files);
        }
    }

    @Override
    public void messageStatusChanged(MessageStatusChangeEvent event) {
        LOG.debug("Handling messageStatusChanged event");

        Map<String, Object> properties = event.getProperties();
        String service = (String) properties.get("service");
        String action = (String) properties.get("action");
        String domain = fsDomainService.getFSPluginDomain(service, action);

        String messageId = event.getMessageId();
        LOG.debug("Message [{}] changed status from [{}] to [{}] in domain [{}]", messageId, event.getFromStatus(), event.getToStatus(), domain);

        if (isSendingEvent(event)) {
            renameMessageFile(domain, messageId, event.getToStatus());
        } else if (isSendSuccessEvent(event)) {
            handleSentMessage(domain, messageId);
        } else if (isSendFailedEvent(event)) {
            handleSendFailedMessage(domain, messageId);
        }
    }

    protected boolean isSendingEvent(MessageStatusChangeEvent event) {
        return SENDING_MESSAGE_STATUSES.contains(event.getToStatus());
    }

    protected boolean isSendSuccessEvent(MessageStatusChangeEvent event) {
        return SEND_SUCCESS_MESSAGE_STATUSES.contains(event.getToStatus());
    }

    protected boolean isSendFailedEvent(MessageStatusChangeEvent event) {
        return SEND_FAILED_MESSAGE_STATUSES.contains(event.getToStatus());
    }

    protected void renameMessageFile(String domain, String messageId, MessageStatus status) {
        LOG.debug("Preparing to rename file using domain [{}], messageId [{}] and messageStatus [{}]", domain, messageId, status);

        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
             FileObject outgoingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER);
             FileObject targetFile = findMessageFile(outgoingFolder, messageId)) {

            if (targetFile != null) {
                String baseName = targetFile.getName().getBaseName();
                String newName = fsFileNameHelper.deriveFileName(baseName, status);
                fsFilesManager.renameFile(targetFile, newName);
            } else {
                LOG.error("The message to rename [{}] was not found in domain [{}]", messageId, domain);
            }
        } catch (FileSystemException ex) {
            LOG.error("Error renaming file", ex);
        } catch (FSSetUpException ex) {
            LOG.error("Error setting up folders for domain [" + domain + "]", ex);
        }
    }

    /**
     * extracts finalRecipient from message properties
     *
     * @param userMessage Object which contains finalRecipient info
     * @return finalRecipient String
     * @see UserMessage
     */
    protected String getFinalRecipient(final UserMessage userMessage) {
        String finalRecipient = null;
        for (final Property p : userMessage.getMessageProperties().getProperty()) {
            if (p.getName() != null && p.getName().equals(MessageConstants.FINAL_RECIPIENT)) {
                finalRecipient = p.getValue();
                break;
            }
        }
        return finalRecipient;
    }

    /**
     * replacing all non [a-zA-z0-9] characters with _ from a fileName
     *
     * @param fileName filename to be sanitized
     * @return sanitized fileName
     */
    protected String sanitizeFileName(@NotNull final String fileName) {
        return fileName.replaceAll(FILENAME_SANITIZE_REGEX, FILENAME_SANITIZE_REPLACEMENT);
    }

    /**
     * Writes metadata file
     *
     * @param outputStream {@link OutputStream} to write the xml
     * @param userMessage  Object which contains metadata to be printed
     * @throws JAXBException exception thrown
     */
    protected void writeMetadata(OutputStream outputStream, UserMessage userMessage) throws JAXBException {
        fsxmlHelper.writeXML(outputStream, UserMessage.class, userMessage);
    }

}
