package eu.domibus.core.csv.serializer;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author François Gautier
 * @since 4.2
 */
public interface CvsSerializer<T> {
    default boolean isValid(Object fieldValue){
        return getCheck().test(fieldValue);
    }
    default String serialize(Object fieldValue){
        return getSerialize().apply((T) fieldValue);
    }
    Predicate<Object> getCheck();
    Function<T, String> getSerialize();
}
