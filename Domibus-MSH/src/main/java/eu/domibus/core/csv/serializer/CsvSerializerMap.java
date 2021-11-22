package eu.domibus.core.csv.serializer;

import com.google.gson.GsonBuilder;
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

    @Override // TODO: François Gautier 29-10-21 GSon to be removed EDELIVERY-8617
    public String serialize(Object fieldValue) {
        return new GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .create().toJson(fieldValue);
    }
}
