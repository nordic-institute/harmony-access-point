package eu.domibus.ext.domain;

import java.io.Serializable;

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
