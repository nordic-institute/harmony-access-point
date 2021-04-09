package eu.domibus.ext.services;

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

}
