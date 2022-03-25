package eu.domibus.core.time;

import eu.domibus.api.model.TimezoneOffset;
import eu.domibus.core.message.dictionary.AbstractDictionaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Callable;

/**
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
@Service
public class TimezoneOffsetService extends AbstractDictionaryService {

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
        Callable<TimezoneOffset> findTask = () -> timezoneOffsetDao.findTimezoneOffsetByTimezoneIdAndOffsetSeconds(timezoneId, offsetSeconds);
        Callable<TimezoneOffset> findOrCreateTask = () -> timezoneOffsetDao.findOrCreateTimezoneOffset(timezoneId, offsetSeconds);
        String entityDescription = "TimezoneOffset timezoneId=[" + timezoneId + "] offsetSeconds=[" + offsetSeconds + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

}