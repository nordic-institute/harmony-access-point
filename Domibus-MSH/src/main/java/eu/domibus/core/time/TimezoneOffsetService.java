package eu.domibus.core.time;

import eu.domibus.api.model.TimezoneOffset;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
@Service
public class TimezoneOffsetService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TimezoneOffsetService.class);

    private final TimezoneOffsetDao timezoneOffsetDao;

    public TimezoneOffsetService(TimezoneOffsetDao timezoneOffsetDao) {
        this.timezoneOffsetDao = timezoneOffsetDao;
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
    public TimezoneOffset getTimezoneOffset(String timezoneId, int offsetSeconds) {
        return timezoneOffsetDao.findOrCreateTimezoneOffset(timezoneId, offsetSeconds);
    }
}