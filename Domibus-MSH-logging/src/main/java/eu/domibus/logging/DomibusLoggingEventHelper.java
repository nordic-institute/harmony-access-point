package eu.domibus.logging;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.event.EventType;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

/**
 * Business for striping the apache cxf log payloads
 *
 * @author Catalin Enache
 * @since 4.1.4
 */
@Service
public class DomibusLoggingEventHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DomibusLoggingEventHelper.class);

    static final String CONTENT_TYPE_MARKER = "Content-Type:";
    static final String VALUE_START_MARKER = "<value";
    static final String VALUE_END_MARKER = "</value";
    static final String BOUNDARY_MARKER = "boundary=\"";
    static final String BOUNDARY_MARKER_PREFIX = "--";

    /**
     * Strips the payload base on LogEvent details
     *
     * @param event
     */
    public void stripPayload(LogEvent event) {
        final String operationName = event.getOperationName();
        final EventType eventType = event.getType();
        LOG.debug("operationName=[{}] eventType=[{}] multipart=[{}]", operationName, eventType, event.isMultipartContent());

        //check conditions to strip the payload
        final EventStripPayloadEnum eventStripPayloadEnum = EventStripPayloadEnum.getEventStripPayloadEnum(operationName, eventType);
        if (eventStripPayloadEnum == null) {
            LOG.debug("for operationName=[{}] and eventType=[{}] we don't strip the payload", operationName, eventType);
            return;
        }

        final String xmlNode = eventStripPayloadEnum.getXmlNode();
        switch (eventStripPayloadEnum) {
            case MSH_INVOKE:
                stripPayloadMSH(event, xmlNode);
                break;
            case WS_PLUGIN_SUBMIT_MESSAGE:
            case WS_PLUGIN_RETRIEVE_MESSAGE:
                stripPayloadWSPlugin(event, xmlNode);
                break;
            default:
        }
    }

    private void stripPayloadMSH(final LogEvent event, final String xmlNode) {
        //if it's multipart and we want to strip the AS4 payload
        if (event.isMultipartContent()) {
            String payload = event.getPayload();
            event.setPayload(replaceInPayload(payload, xmlNode));
        }
    }

    private void stripPayloadWSPlugin(final LogEvent event, final String xmlNode) {
        //get the payload
        String payload = event.getPayload();

        //strip 'values' if it's submitMessage or retrieveMessage
        if (event.isMultipartContent()) {
            final String boundary = getMultipartBoundary(event.getContentType());
            payload = replaceInPayloadMultipart(payload, boundary, xmlNode);
        } else {
            payload = replaceInPayloadValues(payload, xmlNode);
        }

        //finally set the payload back
        event.setPayload(payload);
    }


    private String replaceInPayload(final String payload, final String xmlNode) {
        String newPayload = payload;
        //C2 -> C3
        if (payload.contains(xmlNode)) {
            String[] payloadSplits = payload.split(CONTENT_TYPE_MARKER);
            //keeping only first 2 Content-Type elements
            if (payloadSplits.length >= 2) {
                newPayload = payloadSplits[0] + CONTENT_TYPE_MARKER + payloadSplits[1] + AbstractLoggingInterceptor.CONTENT_SUPPRESSED;
            }
        }
        return newPayload;
    }

    private String replaceInPayloadValues(String payload, String xmlNode) {
        String newPayload = payload;

        //first we find the xml node starting index - e.g submitRequest
        int xmlNodeStartIndex = newPayload.indexOf(xmlNode);
        if (xmlNodeStartIndex == -1) {
            LOG.debug("[{}] xml node wasn't found in the payload so no striping will occur", xmlNode);
            return newPayload;
        }

        //start to replace/suppress the content between <value>...</value> pairs
        int indexStart = newPayload.indexOf(VALUE_START_MARKER);
        int startTagLength = newPayload.indexOf('>', indexStart) - indexStart + 1;

        int indexEnd = newPayload.indexOf(VALUE_END_MARKER);
        int endTagLength = newPayload.indexOf('>', indexEnd) - indexEnd + 1;

        while (indexStart >= 0 && indexStart > xmlNodeStartIndex && indexStart < indexEnd) {
            String toBeReplaced = newPayload.substring(indexStart + startTagLength, indexEnd);
            newPayload = newPayload.replace(toBeReplaced,
                    AbstractLoggingInterceptor.CONTENT_SUPPRESSED);

            int fromIndex = indexEnd + endTagLength + AbstractLoggingInterceptor.CONTENT_SUPPRESSED.length() - toBeReplaced.length() + 1;
            indexStart = newPayload.indexOf(VALUE_START_MARKER, fromIndex);
            indexEnd = newPayload.indexOf(VALUE_END_MARKER, fromIndex);
            startTagLength = newPayload.indexOf('>', indexStart) - indexStart + 1;
            endTagLength = newPayload.indexOf('>', indexEnd) - indexEnd + 1;
        }

        return newPayload;
    }

    private String replaceInPayloadMultipart(final String payload, final String boundary, final String xmlTag) {
        String newPayload = payload;

        if (payload.contains(xmlTag)) {
            String[] payloadSplits = payload.split(boundary);
            if (payloadSplits.length > 0) {
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
        String[] contentTypeSplits = contentType.split(BOUNDARY_MARKER);
        if (contentTypeSplits.length >= 2) {
            return BOUNDARY_MARKER_PREFIX + contentTypeSplits[1].substring(0, contentTypeSplits[1].length() - 1);
        }
        return StringUtils.EMPTY;
    }

    private String getReplacementPart(final String boundarySplit, final String xmlTag) {
        if (boundarySplit.contains(CONTENT_TYPE_MARKER) && !boundarySplit.contains(xmlTag)) {
            return System.lineSeparator() + AbstractLoggingInterceptor.CONTENT_SUPPRESSED + System.lineSeparator();
        }
        return boundarySplit;
    }

    /**
     * * It maps positives cases when the strip payload occurs
     */
    enum EventStripPayloadEnum {

        /**
         * WS-PLUGIN
         */
        WS_PLUGIN_SUBMIT_MESSAGE("submitMessage", EventType.REQ_IN, "submitRequest"),
        WS_PLUGIN_RETRIEVE_MESSAGE("retrieveMessage", EventType.RESP_OUT, "retrieveMessageResponse"),

        /**
         * MSH
         */
        MSH_INVOKE("Invoke", EventType.REQ_OUT, "UserMessage"
        );

        String xmlNode;
        String allowedOperationName;
        EventType eventType;


        EventStripPayloadEnum(final String allowedOperationName, final EventType eventType, final String xmlNode) {
            this.allowedOperationName = allowedOperationName;
            this.eventType = eventType;
            this.xmlNode = xmlNode;
        }

        static EventStripPayloadEnum getEventStripPayloadEnum(final String operationName, final EventType eventType) {
            if (operationName == null || eventType == null) return null;
            return Stream.of(EventStripPayloadEnum.values()).
                    filter(e -> operationName.contains(e.allowedOperationName) && e.eventType == eventType).
                    findFirst().
                    orElse(null);
        }

        public String getXmlNode() {
            return this.xmlNode;
        }
    }
}
