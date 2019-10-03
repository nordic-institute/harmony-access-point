package eu.domibus.plugin.webService.logging;

import eu.domibus.logging.DomibusLoggingEventSender;
import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.event.LogEvent;

/**
 * This class extends {@code DomibusLoggingEventSender}
 * <p>
 * It will implement operations based on {@code LogEvent} like partially printing the payload, etc
 *
 * @author Catalin Enache
 * @since 4.1.1
 */
public class DomibusWSPluginLoggingEventSender extends DomibusLoggingEventSender {

    static final String RETRIEVE_MESSAGE_RESPONSE = "retrieveMessageResponse";
    static final String VALUE_START = "<value>";
    static final String VALUE_END = "</value>";
    static final String SUBMIT_MESSAGE = "submitRequest";


    /**
     * It removes some parts of the payload info
     *
     * @param event
     */
    @Override
    protected void stripPayload(LogEvent event) {
        String payload = event.getPayload();

        //strip values if it's submitMessage
        payload = replaceInPayload(payload, SUBMIT_MESSAGE);

        //strip values if it's a retrieveMessage
        payload = replaceInPayload(payload, RETRIEVE_MESSAGE_RESPONSE);

        //finally set the paylaod back
        event.setPayload(payload);

    }

    private String replaceInPayload(String payload, String xmlNodeStartTag) {
        String newPayload = payload;

        //first we find the xml node starting index - e.g submitRequest
        int xmlNodeStartIndex = newPayload.indexOf(xmlNodeStartTag);
        if (xmlNodeStartIndex == -1) {
            return newPayload;
        }

        //start to replace/suppress the content between <value>...</value> pairs
        int indexStart = newPayload.indexOf(VALUE_START);
        int indexEnd = newPayload.indexOf(VALUE_END);

        while (indexStart >= 0 && indexStart > xmlNodeStartIndex && indexStart < indexEnd) {
            String toBeReplaced = newPayload.substring(indexStart + VALUE_START.length(), indexEnd);
            newPayload = newPayload.replace(toBeReplaced,
                    AbstractLoggingInterceptor.CONTENT_SUPPRESSED);

            int fromIndex = indexEnd + VALUE_END.length() + AbstractLoggingInterceptor.CONTENT_SUPPRESSED.length() - toBeReplaced.length() + 1;
            indexStart = newPayload.indexOf(VALUE_START, fromIndex);
            indexEnd = newPayload.indexOf(VALUE_END, fromIndex);
        }

        return newPayload;
    }


}
