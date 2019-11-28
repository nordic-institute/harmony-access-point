package eu.domibus.api.monitoring;

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
