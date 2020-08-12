package eu.domibus.plugin.webService.logging;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.event.EventType;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * {@inheritDoc}
 */
@Service
public class WSPluginLoggingEventHelperImpl implements WSPluginLoggingEventHelper {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginLoggingEventHelperImpl.class);

    static final String BOUNDARY_MARKER = "boundary=\"";
    static final String BOUNDARY_MARKER_PREFIX = "--";
    static final String CONTENT_TYPE_MARKER = "Content-Type:";
    static final String VALUE_START_MARKER = "<value";
    static final String VALUE_END_MARKER = "</value";
    static final String RETRIEVE_MESSAGE_RESPONSE = "retrieveMessageResponse";
    static final String SUBMIT_REQUEST = "submitRequest";
    static final String OPERATION_SUBMIT_MESSAGE = "submitMessage";
    static final String OPERATION_RETRIEVE_MESSAGE = "retrieveMessage";
    static final String HEADERS_AUTHORIZATION = "Authorization";

    @Override
    public void stripPayload(LogEvent event) {
        final String operationName = event.getOperationName();
        final EventType eventType = event.getType();
        LOG.debug("operationName=[{}] eventType=[{}] multipart=[{}]", operationName, eventType, event.isMultipartContent());

        //get the payload
        String payload = event.getPayload();
        String xmlNode = checkIfOperationIsAllowed(event);
        if (xmlNode == null) {
            LOG.debug("payload not striped for operationName=[{}] eventType=[{}]", operationName, eventType);
            return;
        }

        if (event.isMultipartContent()) {
            final String boundary = getMultipartBoundary(event.getContentType());
            payload = replaceInPayloadMultipart(payload, boundary, xmlNode);
        } else {
            //strip 'values' if it's submitMessage or retrieveMessage
            payload = replaceInPayloadValues(payload, xmlNode);
        }

        // finally set the payload back
        event.setPayload(payload);
    }

    @Override
    public void stripHeaders(LogEvent event) {
        Map<String, String> headers = event.getHeaders();
        if (CollectionUtils.isEmpty(headers)) {
            LOG.debug("no apache cxf headers to strip");
            return;
        }
        headers.entrySet()
                .removeIf(e -> HEADERS_AUTHORIZATION.equalsIgnoreCase(e.getKey()));
    }

    @Override
    public String checkIfOperationIsAllowed(LogEvent logEvent) {
        if (logEvent.getType() == EventType.REQ_IN && logEvent.getOperationName().contains(OPERATION_SUBMIT_MESSAGE)) {
            return SUBMIT_REQUEST;
        }
        if (logEvent.getType() == EventType.RESP_OUT && logEvent.getOperationName().contains(OPERATION_RETRIEVE_MESSAGE)) {
            return RETRIEVE_MESSAGE_RESPONSE;
        }
        return null;
    }

    private String replaceInPayloadMultipart(final String payload, final String boundary, final String xmlTag) {
        String newPayload = payload;

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
}
