package eu.domibus.common.metrics;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Enumeration listing the name attributed to the metrics.
 */
public enum MetricNames {
    INCOMING_USER_MESSAGE,
    SERVLET_INCOMING_USER_MESSAGE,
    INCOMING_USER_MESSAGE_RECEIPT,
    OUTGOING_USER_MESSAGE,
    INCOMING_PULL_REQUEST,
    INCOMING_PULL_REQUEST_RECEIPT,
    OUTGOING_PULL_REQUEST,
    OUTGOING_PULL_RECEIPT,
    SUBMITTED_MESSAGES,
    VERIFY_TRUST,
    AUTHORIZE_MESSAGE,
    SERVER_POLICY_IN,
    TRUST_SENDER,
    ON_MESSAGE;

    public String getCounterName() {
        return this.name().toUpperCase() + "_counter";
    }

    public String getTimerName() {
        return this.name().toUpperCase() + "_timer";
    }


}
