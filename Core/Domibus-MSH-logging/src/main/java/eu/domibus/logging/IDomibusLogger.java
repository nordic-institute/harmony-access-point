package eu.domibus.logging;

import eu.domibus.logging.api.MDCAccessor;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Map;

public interface IDomibusLogger extends Logger, MDCAccessor {
    String MDC_USER = "user";
    String MDC_MESSAGE_ID = "messageId";
    String MDC_MESSAGE_ENTITY_ID = "messageEntityId";
    String MDC_BATCH_ENTITY_ID = "batchEntityId";
    String MDC_DOMAIN = "domain";
    String MDC_FROM = "from";
    String MDC_TO = "to";
    String MDC_SERVICE = "service";
    String MDC_ACTION = "action";

    String MDC_PROPERTY_PREFIX = "d_";

    Marker BUSINESS_MARKER = MarkerFactory.getMarker("BUSINESS");
    Marker SECURITY_MARKER = MarkerFactory.getMarker("SECURITY");

    void businessTrace(DomibusMessageCode key, Object... args);

    void businessDebug(DomibusMessageCode key, Object... args);

    void businessInfo(DomibusMessageCode key, Object... args);

    void businessWarn(DomibusMessageCode key, Object... args);

    void businessWarn(DomibusMessageCode key, Throwable t, Object... args);

    void businessError(DomibusMessageCode key, Object... args);

    void businessError(DomibusMessageCode key, Throwable t, Object... args);

    void securityTrace(DomibusMessageCode key, Object... args);

    void securityDebug(DomibusMessageCode key, Object... args);

    void securityInfo(DomibusMessageCode key, Object... args);

    void securityWarn(DomibusMessageCode key, Object... args);

    void securityWarn(DomibusMessageCode key, Throwable t, Object... args);

    void securityError(DomibusMessageCode key, Object... args);

    void securityError(DomibusMessageCode key, Throwable t, Object... args);

    Map<String, String> getCopyOfContextMap();

    void setContextMap(Map<String, String> newContextMap);

}
