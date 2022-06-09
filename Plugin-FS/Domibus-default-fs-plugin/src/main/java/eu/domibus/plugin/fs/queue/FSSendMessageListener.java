package eu.domibus.plugin.fs.queue;

import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * FS Plugin Send Queue message listener
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Service("fsSendMessageListener")
public class FSSendMessageListener implements MessageListener {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(FSSendMessageListener.class);

    private final FSSendMessagesService fsSendMessagesService;

    protected final FSFilesManager fsFilesManager;


    public FSSendMessageListener(FSSendMessagesService fsSendMessagesService,
                                 FSFilesManager fsFilesManager) {
        this.fsSendMessagesService = fsSendMessagesService;
        this.fsFilesManager = fsFilesManager;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = {AuthenticationExtException.class}, timeout = 1200)
    // 20 minutes
    @Override
    @MDCKey(value = {IDomibusLogger.MDC_MESSAGE_ID, IDomibusLogger.MDC_MESSAGE_ENTITY_ID}, cleanOnStart = true)
    public void onMessage(Message message) {
        LOG.debug("received message on fsPluginSendQueue");

        String domain;
        String fileName;
        try {
            domain = message.getStringProperty(MessageConstants.DOMAIN);
            fileName = message.getStringProperty(MessageConstants.FILE_NAME);
            LOG.debug("received message on fsPluginSendQueue for domain={} and fileName={}", domain, fileName);
        } catch (JMSException e) {
            LOG.error("Unable to extract domainCode or fileName from JMS message", e);
            return;
        }

        if (StringUtils.isBlank(fileName)) {
            LOG.error("Error while consuming JMS message: [{}] fileName empty.", message);
            return;
        }

        try (FileObject fileObject = getVFSManager().resolveFile(fileName)) {
            if (!fileObject.exists()) {
                LOG.warn("File does not exist: [{}] discard the JMS message", fileName);
                fsFilesManager.deleteLockFile(fileObject);
                return;
            }
            fsSendMessagesService.authenticateForDomain(domain);

            //process the file
            LOG.debug("now send the file: {}", fileObject);
            fsSendMessagesService.processFileSafely(fileObject, domain);
        } catch (FileSystemException e) {
            LOG.error("Error occurred while trying to access the file to be sent: " + fileName, e);
        }
    }

    protected FileSystemManager getVFSManager() throws FileSystemException {
        return VFS.getManager();
    }
}
