package eu.domibus.core.csv;

import com.google.gson.GsonBuilder;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.common.ErrorCode;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static eu.domibus.core.csv.CsvServiceImpl.CSV_DATE_PATTERN;

/**
 * @author François Gautier
 * @since 4.2
 */
public enum CvsSerializer {

    NULL(
            Objects::isNull,
            object -> StringUtils.EMPTY),
    MAP(
            fieldValue -> fieldValue instanceof Map,
            CvsSerializer::parseMap),
    DATE(
            fieldValue -> fieldValue instanceof Date,
            CvsSerializer::parseDate),
    LOCAL_DATE_TIME(
            fieldValue -> fieldValue instanceof LocalDateTime,
            CvsSerializer::parseLocalDateTime),
    ERROR_CODE(
            fieldValue -> fieldValue instanceof ErrorCode,
            CvsSerializer::parseErrorCode),
    ROUTING_CRITERIA(
            fieldValue -> fieldValue instanceof RoutingCriteria,
            CvsSerializer::parseRoutingCriteria),
    DEFAULT(
            fieldValue -> true,
            CvsSerializer::parseDefault);

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CvsSerializer.class);

    private Predicate<Object> check;
    private Function<Object, String> serialize;

    public boolean check(Object o) {
        return check.test(o);
    }

    public String serialize(Object o) {
        LOG.trace("Serializer: [{}]", this);
        String result = serialize.apply(o);
        LOG.trace("Serializer: [{}] -> [{}]", this, result);
        return result;
    }

    CvsSerializer(Predicate<Object> check,
                  Function<Object, String> serialize) {
        this.check = check;
        this.serialize = serialize;
    }

    private static String parseMap(Object fieldValue) {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(fieldValue);
    }

    private static String parseDefault(Object fieldValue) {
        return Objects.toString(fieldValue, StringUtils.EMPTY);
    }

    private static String parseDate(Object fieldValue) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern(CSV_DATE_PATTERN);
        ZonedDateTime d = ZonedDateTime.ofInstant(((Date) fieldValue).toInstant(), ZoneId.systemDefault());
        return d.format(f);
    }

    private static String parseLocalDateTime(Object fieldValue) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern(CSV_DATE_PATTERN);
        ZonedDateTime d = ((LocalDateTime) fieldValue).atZone(ZoneId.systemDefault());
        return d.format(f);
    }

    private static String parseErrorCode(Object fieldValue) {
        return ((ErrorCode) fieldValue).name();
    }
    private static String parseRoutingCriteria(Object fieldValue) {
        return ((RoutingCriteria) fieldValue).getExpression();
    }
}
