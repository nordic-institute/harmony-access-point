package eu.domibus.core.metrics;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Enumeration listing the name attributed to the metrics.
 */
public enum MetricNames {
    PRIORITY_DEFAULT,
    PRIORITY_LOW,
    PRIORITY_MEDIUM,
    PRIORITY_HIGH,
    INCOMING_USER_MESSAGE,
    INCOMING_USER_MESSAGE_RECEIPT,
    OUTGOING_USER_MESSAGE,
    INCOMING_PULL_REQUEST,
    INCOMING_PULL_REQUEST_RECEIPT,
    OUTGOING_PULL_REQUEST,
    OUTGOING_PULL_RECEIPT;

    public String getCounterName() {
        return this.name().toUpperCase() + "_counter";
    }

    public String getTimerName() {
        return this.name().toUpperCase() + "_timer";
    }


}
