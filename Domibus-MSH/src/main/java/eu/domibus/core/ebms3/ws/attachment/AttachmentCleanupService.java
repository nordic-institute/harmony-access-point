package eu.domibus.core.ebms3.ws.attachment;

import eu.domibus.ext.domain.metrics.Counter;
import eu.domibus.ext.domain.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.attachment.AttachmentDataSource;
import org.apache.cxf.message.Attachment;
import org.springframework.stereotype.Service;

import javax.activation.DataSource;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class AttachmentCleanupService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AttachmentCleanupService.class);

    @Timer
    @Counter
    public void cleanAttachments(SOAPMessage soapMessage) {
        final Iterator iterator = soapMessage.getAttachments();
        while (iterator.hasNext()) {
            AttachmentPart attachmentPart = (AttachmentPart) iterator.next();
            try {
                cleanAttachmentPart(attachmentPart);
            } catch (IOException | SOAPException e) {
                LOG.warn("Could not close the input stream of this attachment [" + attachmentPart.getContentId() + "]", e);
            }
        }
    }

    protected void cleanAttachmentPart(AttachmentPart attachment) throws SOAPException, IOException {
        if (attachment == null || attachment.getDataHandler() == null || attachment.getDataHandler().getDataSource() == null) {
            return;
        }
        InputStream is = attachment.getDataHandler().getDataSource().getInputStream();
        // close will delete resource(etc: temporary file) held by the input stream;
        is.close();
        LOG.debug("Input stream successfully closed for attachment [{}]", attachment.getContentId());
    }

    public void cleanAttachments(Collection<Attachment> attachments) {
        if (attachments == null) {
            LOG.debug("No attachments to clean");
            return;
        }

        LOG.debug("Cleaning attachments");

        for (Attachment attachment : attachments) {
            try {
                cleanRequestAttachment(attachment);
            } catch (IOException e) {
                LOG.warn("Could not close the input stream of this attachment [" + attachment.getId() + "]", e);
            }
        }
        LOG.debug("Finished cleaning attachments");
    }

    private void cleanRequestAttachment(Attachment attachment) throws IOException {
        if (attachment == null || attachment.getDataHandler() == null || attachment.getDataHandler().getDataSource() == null) {
            return;
        }

        DataSource ds = attachment.getDataHandler().getDataSource();
        if (ds instanceof AttachmentDataSource) {
            InputStream is = ds.getInputStream();
            // close will delete resource(etc: temporary file) held by the input stream;
            is.close();
            LOG.debug("Input stream successfully closed");
        } else {
            LOG.debug("Data source is [" + ds.getClass().getName() + "] and for content type [" + ds.getContentType() + "]");
        }
    }
}
