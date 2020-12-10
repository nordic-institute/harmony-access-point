package eu.domibus.core.ebms3.receiver.interceptor;

import eu.domibus.core.metrics.MetricsHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.attachment.AttachmentDataSource;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.MustUnderstandInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.phase.Phase;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * @author Cosmin Baciu
 * @since 4.1.2
 */
public class SOAPMessageBuilderInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SOAPMessageBuilderInterceptor.class);

    public SOAPMessageBuilderInterceptor() {
        super(Phase.PRE_LOGICAL);
        this.addAfter(MustUnderstandInterceptor.MustUnderstandEndingInterceptor.class.getName());
    }


    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        com.codahale.metrics.Timer.Context methodTimer = MetricsHelper.getMetricRegistry().timer(name(SOAPMessageBuilderInterceptor.class, "handleMessage_internal", "timer")).time();

        final SOAPMessage result = message.getContent(SOAPMessage.class);
        try {
            SAAJInInterceptor.replaceHeaders(result, message);
            result.removeAllAttachments();
            final Collection<Attachment> atts = message.getAttachments();
            if (atts != null) {
                for (final Attachment a : atts) {
                    if (a.getDataHandler().getDataSource() instanceof AttachmentDataSource) {
                        try {
                            ((AttachmentDataSource) a.getDataHandler().getDataSource()).cache(message);
                        } catch (final IOException e) {
                            throw new Fault(e);
                        }
                    }
                    final AttachmentPart ap = result.createAttachmentPart(a.getDataHandler());
                    final Iterator<String> i = a.getHeaderNames();
                    while (i != null && i.hasNext()) {
                        final String h = i.next();
                        final String val = a.getHeader(h);
                        ap.addMimeHeader(h, val);
                    }
                    if (StringUtils.isEmpty(ap.getContentId())) {
                        ap.setContentId(a.getId());
                    }
                    result.addAttachmentPart(ap);
                }
            }

        } catch (final SOAPException soapEx) {
            LOG.error("Could not replace headers for incoming Message", soapEx);
        } finally {
            methodTimer.stop();
        }

    }
}
