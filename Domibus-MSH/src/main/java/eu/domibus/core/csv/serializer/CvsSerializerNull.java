package eu.domibus.core.csv.serializer;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author François Gautier
 * @since 4.2
 */
public class CvsSerializerNull implements CvsSerializer<Object> {

    @Override
    public Predicate<Object> getCheck() {
        return Objects::isNull;
    }

    @Override
    public Function<Object, String> getSerialize() {
        return fieldValue -> Objects.toString(fieldValue, StringUtils.EMPTY);
    }
}
