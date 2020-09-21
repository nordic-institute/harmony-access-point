package eu.domibus.core.csv.serializer;

import com.google.gson.GsonBuilder;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author François Gautier
 * @since 4.2
 */
public class CvsSerializerMap implements CvsSerializer<Map<?, ?>> {

    @Override
    public Predicate<Object> getCheck() {
        return fieldValue -> fieldValue instanceof Map;
    }

    @Override
    public Function<Map<?, ?>, String> getSerialize() {
        return fieldValue -> new GsonBuilder().disableHtmlEscaping().create().toJson(fieldValue);
    }
}
