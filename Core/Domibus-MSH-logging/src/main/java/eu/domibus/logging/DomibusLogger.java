package eu.domibus.logging;

import eu.domibus.logging.api.CategoryLogger;
import eu.domibus.logging.api.MDCAccessor;
import eu.domibus.logging.api.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Map;


/**
 * A custom SLF4J logger specialized in logging using business and security events using specific Domibus message codes
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DomibusLogger extends CategoryLogger implements Logger, MDCAccessor {
    public static final String MDC_USER = "user";
    public static final String MDC_BUSINESS_CODE = "businessCode";
    public static final String MDC_MESSAGE_ID = "messageId";
    public static final String MDC_CONVERSATION_ID = "conversationId";
    public static final String MDC_MESSAGE_ROLE = "messageMSHRole";
    public static final String MDC_MESSAGE_ENTITY_ID = "messageEntityId";
    public static final String MDC_DOMAIN = "domain";
    public static final String MDC_FROM = "from";
    public static final String MDC_TO = "to";
    public static final String MDC_SERVICE = "service";
    public static final String MDC_ACTION = "action";

    public static final String MDC_PROPERTY_PREFIX = "d_";

    public static final Marker BUSINESS_MARKER = MarkerFactory.getMarker("BUSINESS");
    public static final Marker SECURITY_MARKER = MarkerFactory.getMarker("SECURITY");

    public DomibusLogger(Logger logger, MessageConverter messageConverter) {
        super(logger, DomibusLogger.class.getName(), messageConverter, MDC_PROPERTY_PREFIX);
    }

    public DomibusLogger(Logger logger) {
        this(logger, new DefaultMessageConverter());
    }

    public void businessTrace(DomibusMessageCode key, Object... args) {
        putMDC(MDC_BUSINESS_CODE, key.getCode());
        markerTrace(BUSINESS_MARKER, key, null, args);
        removeMDC(MDC_BUSINESS_CODE);
    }

    public void businessDebug(DomibusMessageCode key, Object... args) {
        putMDC(MDC_BUSINESS_CODE, key.getCode());
        markerDebug(BUSINESS_MARKER, key, null, args);
        removeMDC(MDC_BUSINESS_CODE);
    }

    public void businessInfo(DomibusMessageCode key, Object... args) {
        putMDC(MDC_BUSINESS_CODE, key.getCode());
        markerInfo(BUSINESS_MARKER, key, null, args);
        removeMDC(MDC_BUSINESS_CODE);
    }

    public void businessWarn(DomibusMessageCode key, Object... args) {
        businessWarn(key, null, args);
    }

    public void businessWarn(DomibusMessageCode key, Throwable t, Object... args) {
        putMDC(MDC_BUSINESS_CODE, key.getCode());
        markerWarn(BUSINESS_MARKER, key, t, args);
        removeMDC(MDC_BUSINESS_CODE);
    }

    public void businessError(DomibusMessageCode key, Object... args) {
        businessError(key, null, args);
    }

    public void businessError(DomibusMessageCode key, Throwable t, Object... args) {
        putMDC(MDC_BUSINESS_CODE, key.getCode());
        markerError(BUSINESS_MARKER, key, t, args);
        removeMDC(MDC_BUSINESS_CODE);
    }

    public void securityTrace(DomibusMessageCode key, Object... args) {
        markerTrace(SECURITY_MARKER, key, null, args);
    }

    public void securityDebug(DomibusMessageCode key, Object... args) {
        markerDebug(SECURITY_MARKER, key, null, args);
    }

    public void securityInfo(DomibusMessageCode key, Object... args) {
        markerInfo(SECURITY_MARKER, key, null, args);
    }

    public void securityWarn(DomibusMessageCode key, Object... args) {
        securityWarn(key, null, args);
    }

    public void securityWarn(DomibusMessageCode key, Throwable t, Object... args) {
        markerWarn(SECURITY_MARKER, key, t, args);
    }

    public void securityError(DomibusMessageCode key, Object... args) {
        securityError(key, null, args);
    }

    public void securityError(DomibusMessageCode key, Throwable t, Object... args) {
        markerError(SECURITY_MARKER, key, t, args);
    }

    protected void markerTrace(Marker marker, DomibusMessageCode key, Throwable t, Object... args) {
        // log with no marker and stacktrace (if there is one)
        trace(null, key, t, true, args);

        //log with marker and without stacktrace
        trace(marker, key, t, false, args);
    }

    protected void markerDebug(Marker marker, DomibusMessageCode key, Throwable t, Object... args) {
        // log with no marker and stacktrace (if there is one)
        debug(null, key, t, true, args);

        //log with marker and without stacktrace
        debug(marker, key, t, false, args);
    }

    protected void markerInfo(Marker marker, DomibusMessageCode key, Throwable t, Object... args) {
        // log with no marker and stacktrace (if there is one)
        info(null, key, t, true, args);

        //log with marker and without stacktrace
        info(marker, key, t, false, args);
    }

    protected void markerWarn(Marker marker, DomibusMessageCode key, Throwable t, Object... args) {
        // log with no marker and stacktrace (if there is one)
        warn(null, key, t, true, args);

        //log with marker and without stacktrace
        warn(marker, key, t, false, args);
    }

    protected void markerError(Marker marker, DomibusMessageCode key, Throwable t, Object... args) {
        // log with no marker and stacktrace (if there is one)
        error(null, key, t, true, args);

        //log with marker and without stacktrace
        error(marker, key, t, false, args);
    }

    public Map<String, String> getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * Sets the MDC context map with the provided one. For more info see javadoc {@link MDC#setContextMap(java.util.Map)}
     *
     * @param newContextMap
     */
    public void setContextMap(Map<String, String> newContextMap) {
        MDC.setContextMap(newContextMap);
    }
}
