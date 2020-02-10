package eu.domibus.logging;

import org.apache.cxf.ext.logging.event.EventType;

import java.util.stream.Stream;

/**
 * It maps positives cases when the strip payload occurs
 *
 * @since 4.1.4
 * @author Catalin Enache
 */
public enum DomibusLoggingEventStripPayloadEnum {

    /** WS-PLUGIN */
    SUBMIT_MESSAGE("submitMessage", EventType.REQ_IN, "submitRequest"),
    RETRIEVE_MESSAGE_RESPONSE("retrieveMessageResponse",  EventType.REQ_IN, "retrieveMessageResponse"),

    /** MSH */
    MSH_INVOKE("Invoke", EventType.REQ_OUT, "Content-Type:");

    String xmlTag;
    String allowedOperationName;
    EventType eventType;

    DomibusLoggingEventStripPayloadEnum(final String allowedOperationName, final EventType eventType, final String xmlTag) {
        this.allowedOperationName = allowedOperationName;
        this.eventType = eventType;
        this.xmlTag = xmlTag;
    }

    public static String getXmlTagIfStripPayloadIsPossible(final String operationName, final EventType eventType) {
        DomibusLoggingEventStripPayloadEnum x = Stream.of(DomibusLoggingEventStripPayloadEnum.values()).
                filter(e -> operationName.contains(e.allowedOperationName) && e.eventType == eventType).
                findFirst().
                orElse(null);

        if (x == null)  return null;
        return x.xmlTag;
    }

}
