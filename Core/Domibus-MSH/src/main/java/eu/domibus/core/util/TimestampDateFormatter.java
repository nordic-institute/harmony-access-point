package eu.domibus.core.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class is responsible for the generation of timestamps in an ebMS3 conformant way.
 *
 * @author Christian Koch, Stefan Mueller
 */
@Service("dateFormatter")
public class TimestampDateFormatter {

    @Qualifier("xmlDateTimeFormat")
    @Autowired
    protected SimpleDateFormat xmlDateTimeFormat;

    public String generateTimestamp() {
        final Date dateWithTruncatedMilliseconds = new Date(1000 * (new Date().getTime() / 1000));
        return generateTimestamp(dateWithTruncatedMilliseconds);
    }

    public String generateTimestamp(Date timestamp) {
        this.xmlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return this.xmlDateTimeFormat.format(timestamp);
    }
}
