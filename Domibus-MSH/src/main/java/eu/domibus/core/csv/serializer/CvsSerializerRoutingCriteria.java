package eu.domibus.core.csv.serializer;

import eu.domibus.api.routing.RoutingCriteria;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author François Gautier
 * @since 4.2
 */
public class CvsSerializerRoutingCriteria implements CvsSerializer<RoutingCriteria> {

    @Override
    public Predicate<Object> getCheck() {
        return fieldValue -> fieldValue instanceof RoutingCriteria;
    }

    @Override
    public Function<RoutingCriteria, String> getSerialize() {
        return RoutingCriteria::getExpression;
    }
}
