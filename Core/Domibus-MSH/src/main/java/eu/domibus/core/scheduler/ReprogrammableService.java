package eu.domibus.core.scheduler;

import eu.domibus.api.scheduler.Reprogrammable;

import java.util.Date;

/**
 * Service that allows rescheduling and resetting of future events.
 */
public interface ReprogrammableService {

    /**
     * Removes all the information related to the next attempt.
     *
     * @param reprogrammable the object which will have its next attempt reset.
     */
    void removeRescheduleInfo(Reprogrammable reprogrammable);

    /**
     * Sets all the information related to the next attempt.
     *
     * @param reprogrammable the object which will have its next attempt set.
     * @param nextAttempt the date at which to reschedule.
     */
    void setRescheduleInfo(Reprogrammable reprogrammable, Date nextAttempt);
}
