package eu.domibus.api.util;

import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface DateUtil {

    DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    Date fromString(String value);

    Date getStartOfDay();

    String getCurrentTime(DateTimeFormatter dateTimeFormatter);

    String getCurrentTime();

    long getDiffMinutesBetweenDates(Date date1, Date date2);
}
