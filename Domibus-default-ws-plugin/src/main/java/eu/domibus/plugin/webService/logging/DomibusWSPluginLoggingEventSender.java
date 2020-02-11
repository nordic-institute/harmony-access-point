package eu.domibus.plugin.webService.logging;

import eu.domibus.logging.DomibusLoggingEventSender;
import eu.domibus.logging.DomibusLoggingEventStripPayloadEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.event.EventType;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends {@code DomibusLoggingEventSender}
 * <p>
 * It will implement operations based on {@code LogEvent} like partially printing the payload, etc
 *
 * @author Catalin Enache
 * @since 4.1.1
 */
public class DomibusWSPluginLoggingEventSender extends DomibusLoggingEventSender {
    private static final Logger LOG = LoggerFactory.getLogger(DomibusWSPluginLoggingEventSender.class);

    static final String RETRIEVE_MESSAGE_RESPONSE = "retrieveMessageResponse";
    static final String VALUE_START = "<value";
    static final String VALUE_END = "</value";
    static final String SUBMIT_MESSAGE = "submitRequest";




    /**
     * It removes some parts of the payload info
     *
     * @param event LogEvent
     */
    @Override
    protected void stripPayload(LogEvent event) {
        final String operationName = event.getOperationName();
        final EventType eventType = event.getType();
        LOG.debug("operationName=[{}] eventType=[{}]", operationName, eventType);

        //check conditions to strip the payload and get the xmlTag
        final String xmlTag = DomibusLoggingEventStripPayloadEnum.getXmlTagIfStripPayloadIsPossible(operationName, eventType);
        if (xmlTag == null) {
            LOG.debug("for operationName=[{}] and eventType=[{}] we don't strip the payload", operationName, eventType);
            return;
        }

        //get the payload
        String payload = event.getPayload();

        //strip 'values' if it's submitMessage or retrieveMessage
        if (!event.isMultipartContent()) {
            payload = replaceInPayloadValues(payload, xmlTag);
        } else {
            final String boundary = getMultipartBoundary(event.getContentType());
            payload = replaceInPayloadMultipart(payload, boundary, xmlTag);
        }

        //finally set the payload back
        event.setPayload(payload);
    }

    private String replaceInPayloadValues(String payload, String xmlNodeStartTag) {
        String newPayload = payload;

        //first we find the xml node starting index - e.g submitRequest
        int xmlNodeStartIndex = newPayload.indexOf(xmlNodeStartTag);
        if (xmlNodeStartIndex == -1) {
            return newPayload;
        }

        //start to replace/suppress the content between <value>...</value> pairs
        int indexStart = newPayload.indexOf(VALUE_START);
        int startTagLength = newPayload.indexOf('>', indexStart) - indexStart + 1;

        int indexEnd = newPayload.indexOf(VALUE_END);
        int endTagLength = newPayload.indexOf('>', indexEnd) - indexEnd + 1;

        while (indexStart >= 0 && indexStart > xmlNodeStartIndex && indexStart < indexEnd) {
            String toBeReplaced = newPayload.substring(indexStart + startTagLength, indexEnd);
            newPayload = newPayload.replace(toBeReplaced,
                    AbstractLoggingInterceptor.CONTENT_SUPPRESSED);

            int fromIndex = indexEnd + endTagLength + AbstractLoggingInterceptor.CONTENT_SUPPRESSED.length() - toBeReplaced.length() + 1;
            indexStart = newPayload.indexOf(VALUE_START, fromIndex);
            indexEnd = newPayload.indexOf(VALUE_END, fromIndex);
            startTagLength = newPayload.indexOf('>', indexStart) - indexStart + 1;
            endTagLength = newPayload.indexOf('>', indexEnd) - indexEnd + 1;
        }

        return newPayload;
    }

    private String replaceInPayloadMultipart(final String payload, final String boundary, final String xmlTag) {
        String newPayload = payload;

        if (payload.contains(boundary)) {
            String[] payloadSplits = payload.split(boundary);

            //the first payload is the message itself
            if (payloadSplits.length >= 3) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < payloadSplits.length; i++) {
                    stringBuilder.append(getReplacementPart(payloadSplits[i], xmlTag));
                    if (i != payloadSplits.length - 1) {
                        stringBuilder.append(boundary);
                    }
                }
                newPayload = stringBuilder.toString();
            }
        }
        return newPayload;
    }

    private String getMultipartBoundary(final String contentType) {
        String[] tmp = contentType.split("boundary=\"");
        if (tmp.length >= 2) {
            return System.lineSeparator() + "--" + tmp[1].substring(0, tmp[1].length() - 1);
        }
        return StringUtils.EMPTY;
    }

    private String getReplacementPart(final String boundarySplit, final String xmlTag) {
        if (boundarySplit.isEmpty() ||
                (boundarySplit.contains("Content-Type: ") && boundarySplit.contains(xmlTag))
        || boundarySplit.equals("--" + System.lineSeparator())) return boundarySplit;

        return AbstractLoggingInterceptor.CONTENT_SUPPRESSED;
    }
}
