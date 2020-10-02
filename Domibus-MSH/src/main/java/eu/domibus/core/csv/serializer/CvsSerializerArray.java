package eu.domibus.core.csv.serializer;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class CvsSerializerArray implements CvsSerializer {

    @Override
    public boolean canHandle(Object fieldValue) {
        return fieldValue instanceof List;
    }

    @Override
    public String serialize(Object fieldValue) {
        return ((List<Object>) fieldValue).stream().map(el -> el.toString()).collect(Collectors.joining(" "));
    }
}
