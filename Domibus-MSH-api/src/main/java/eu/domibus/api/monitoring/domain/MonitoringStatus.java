package eu.domibus.api.monitoring.domain;

/**
 * Stores Monitoring Service Status
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public enum MonitoringStatus {

    /**
     * Domibus DB, JMS Broker are Up and accessible.
     */
    NORMAL,

    /**
     * To notify the ERROR status of Domibus DB/ JMS Broker.
     */
    ERROR,

    /**
     * Another instance of the quartz trigger is already executing for the trigger's stateful job
     */
    BLOCKED

}
