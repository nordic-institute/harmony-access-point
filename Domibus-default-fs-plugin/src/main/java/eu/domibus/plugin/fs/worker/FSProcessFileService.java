package eu.domibus.plugin.fs.worker;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.fs.*;
import eu.domibus.plugin.fs.ebms3.UserMessage;
import eu.domibus.plugin.fs.exception.FSPluginException;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSProcessFileService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSProcessFileService.class);

    @Resource(name = "backendFSPlugin")
    protected FSPluginImpl backendFSPlugin;

    @Autowired
    protected FSFilesManager fsFilesManager;

    @Autowired
    protected FSPluginProperties fsPluginProperties;

    @Autowired
    protected FSXMLHelper fsxmlHelper;

    @Autowired
    protected FSFileNameHelper fsFileNameHelper;

    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void processFile(FileObject processableFile, String domain) throws FileSystemException, JAXBException, MessagingProcessingException {
        LOG.debug("processFile start for file: {}", processableFile);

        try (FileObject metadataFile = fsFilesManager.resolveSibling(processableFile, FSSendMessagesService.METADATA_FILE_NAME)) {
            if (metadataFile.exists()) {
                UserMessage metadata = parseMetadata(metadataFile);
                LOG.debug("Metadata found and valid: [{}]", processableFile.getName());

                DataHandler dataHandler = fsFilesManager.getDataHandler(processableFile);
                Map<String, FSPayload> fsPayloads = new HashMap<>(1);

                //we add mimetype later, base name and dataHandler now
                String payloadId = fsPluginProperties.getPayloadId(domain);
                final FSPayload fsPayload = new FSPayload(null, processableFile.getName().getBaseName(), dataHandler);
                fsPayload.setFileSize(processableFile.getContent().getSize());
                fsPayload.setFilePath(processableFile.getURL().getPath());
                fsPayloads.put(payloadId, fsPayload);
                FSMessage message = new FSMessage(fsPayloads, metadata);
                String messageId = backendFSPlugin.submit(message);

                LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
                LOG.info("Message [{}] submitted: [{}]", messageId, processableFile.getName());

            } else {
                LOG.error("Metadata file is missing for " + processableFile.getName().getURI());
            }
        }
    }

    public void renameProcessedFile(FileObject processableFile, String messageId) {
        final String baseName = processableFile.getName().getBaseName();
        String newFileName = fsFileNameHelper.deriveFileName(baseName, messageId);

        LOG.debug("Renaming file [{}] to [{}]", baseName, newFileName);

        try {
            fsFilesManager.renameFile(processableFile, newFileName);
        } catch (FileSystemException ex) {
            throw new FSPluginException("Error renaming file [" + processableFile.getName().getURI() + "] to [" + newFileName + "]", ex);
        }
    }

    protected UserMessage parseMetadata(FileObject metadataFile) throws JAXBException, FileSystemException {
        return fsxmlHelper.parseXML(metadataFile.getContent().getInputStream(), UserMessage.class);
    }

}
