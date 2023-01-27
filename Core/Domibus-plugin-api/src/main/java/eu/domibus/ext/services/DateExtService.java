package eu.domibus.ext.services;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
public interface DateExtService {

    /**
     * Returns the current system {@code Date}, reflected in coordinated universal time (UTC).
     *
     * @return the current system {@code Date}, reflected in coordinated universal time (UTC)
     */
    Date getUtcDate();

    /**
     * @param localDateTime
     * @return the localDateTime reflected in coordinated universal time (UTC)
     */
    LocalDateTime getUtcLocalDateTime(LocalDateTime localDateTime);

    /**
     * Parse a string date to an ID_PK
     *
     * @param date of format YYYY-MM-dd'T'HH'H' or YYYY-MM-dd
     * @return date of format YYMMDDHH0000000000
     */
    Long getIdPkDateHour(String date);

}
