package eu.domibus.core.csv.serializer;

import eu.domibus.api.routing.RoutingCriteria;
import org.springframework.stereotype.Service;

/**
 * @author François Gautier
 * @since 4.2
 */
@Service
public class CvsSerializerRoutingCriteria implements CvsSerializer {

    @Override
    public boolean canHandle(Object fieldValue) {
        return fieldValue instanceof RoutingCriteria;
    }

    @Override
    public String serialize(Object fieldValue) {
        return ((RoutingCriteria) fieldValue).getExpression();
    }
}
