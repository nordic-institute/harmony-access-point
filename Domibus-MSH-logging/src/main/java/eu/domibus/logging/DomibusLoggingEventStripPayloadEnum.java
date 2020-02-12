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
    WS_PLUGIN_SUBMIT_MESSAGE("submitMessage", EventType.REQ_IN, "submitRequest"),
    WS_PLUGIN_RETRIEVE_MESSAGE("retrieveMessage",  EventType.RESP_OUT, "retrieveMessageResponse"),

    /** MSH */
    MSH_INVOKE("Invoke", EventType.REQ_OUT, "UserMessage");

    String xmlNode;
    String allowedOperationName;
    EventType eventType;

    DomibusLoggingEventStripPayloadEnum(final String allowedOperationName, final EventType eventType, final String xmlNode) {
        this.allowedOperationName = allowedOperationName;
        this.eventType = eventType;
        this.xmlNode = xmlNode;
    }

    /**
     * It will checks the oepration name and the event Type and returns xml node if
     * it's allowed to strip payload
     * @param operationName Soap operation Name
     * @param eventType EventType
     * @return xml node to be present
     */
    public static String getXmlNodeIfStripPayloadIsPossible(final String operationName, final EventType eventType) {
        DomibusLoggingEventStripPayloadEnum x = Stream.of(DomibusLoggingEventStripPayloadEnum.values()).
                filter(e -> operationName.contains(e.allowedOperationName) && e.eventType == eventType).
                findFirst().
                orElse(null);

        if (x == null)  return null;
        return x.xmlNode;
    }

}
