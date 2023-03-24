package eu.domibus.api.scheduler;

import eu.domibus.api.model.TimezoneOffset;

import java.util.Date;

/**
 * Interface providing operations for setting and reading future attempts in the timezone of the application.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
public interface Reprogrammable {

    /**
     * Returns the date of the next attempt.
     *
     * @return date of next attempt.
     */
    Date getNextAttempt();

    /**
     * Set the date of the next attempt.
     *
     * @param nextAttempt the date at which the next attempt will be scheduled.
     */
    void setNextAttempt(Date nextAttempt);

    /**
     * Returns the timezone offset in which the next attempt is to be considered.
     *
     * @return the timezone offset to use when displaying the next attempt to the user.
     */
    TimezoneOffset getTimezoneOffset();

    /**
     * Sets the timezone offset in which the next attempt is to be considered.
     *
     * @param timezoneOffset the timezone offset to use when considering the next attempt.
     */
    void setTimezoneOffset(TimezoneOffset timezoneOffset);
}