package eu.domibus.ext.domain.monitoring;

import java.io.Serializable;

/**
 * Enum that stores Monitoring Status
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public enum MonitoringStatus implements Serializable {

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
