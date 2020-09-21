package eu.domibus.core.csv.serializer;

import eu.domibus.common.ErrorCode;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author François Gautier
 * @since 4.2
 */
public class CvsSerializerErrorCode implements CvsSerializer<ErrorCode> {

    @Override
    public Predicate<Object> getCheck() {
        return fieldValue -> fieldValue instanceof ErrorCode;
    }

    @Override
    public Function<ErrorCode, String> getSerialize() {
        return Enum::name;
    }
}
