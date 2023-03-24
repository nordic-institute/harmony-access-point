package eu.domibus.core.time;

import eu.domibus.api.model.TimezoneOffset;
import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;

/**
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
@Repository
public class TimezoneOffsetDao extends BasicDao<TimezoneOffset> {

    public TimezoneOffsetDao() {
        super(TimezoneOffset.class);
    }

    /**
     * Returns an existing timezone offset entry that matches provided the timezone ID and offset in seconds or a newly
     * created one, when no timezone offsets are matching these.
     *
     * @param timezoneId the timezone ID (e.g. "Europe/Brussels");
     * @param offsetSeconds the timezone offset in seconds (e.g. -3600, 0, 3600, 7200).
     *
     * @return an existing timezone offset dictionary entry or a newly created one.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TimezoneOffset findOrCreateTimezoneOffset(String timezoneId, int offsetSeconds) {
        TimezoneOffset timezoneOffset = findTimezoneOffsetByTimezoneIdAndOffsetSeconds(timezoneId, offsetSeconds);
        if(timezoneOffset != null) {
            return timezoneOffset;
        }
        TimezoneOffset newTimezoneOffset = new TimezoneOffset();
        newTimezoneOffset.setNextAttemptTimezoneId(timezoneId);
        newTimezoneOffset.setNextAttemptOffsetSeconds(offsetSeconds);
        create(newTimezoneOffset);
        return newTimezoneOffset;
    }

    /**
     * Returns a timezone offset matching the provided timezone ID and offset in seconds or {@code null} if none match.
     *
     * @param timezoneId the timezone ID (e.g. "Europe/Brussels");
     * @param offsetSeconds the timezone offset in seconds (e.g. -3600, 0, 3600, 7200).
     *
     * @return the timezone offset entity if present; {@code null}, otherwise.
     */
    public TimezoneOffset findTimezoneOffsetByTimezoneIdAndOffsetSeconds(String timezoneId, int offsetSeconds) {
        TypedQuery<TimezoneOffset> query = em.createNamedQuery("TimezoneOffset.findByTimezoneIdAndOffsetSeconds", TimezoneOffset.class);
        query.setParameter("TIMEZONE_ID", timezoneId);
        query.setParameter("OFFSET_SECONDS", offsetSeconds);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}