package eu.domibus.core.csv.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author François Gautier
 * @since 4.2
 */
@Service
public class CsvSerializerMap implements CsvSerializer {

    @Override
    public boolean canHandle(Object fieldValue) {
        return fieldValue instanceof Map;
    }

    @Override
    public String serialize(Object fieldValue) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper.writeValueAsString(fieldValue);
    }
}
