package eu.domibus.core.csv.serializer;

import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author François Gautier
 * @since 4.2
 */
@Service
public class CvsSerializerMap implements CvsSerializer {

    @Override
    public boolean canHandle(Object fieldValue) {
        return fieldValue instanceof Map;
    }

    @Override
    public String serialize(Object fieldValue) {
        return new GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .create().toJson(fieldValue);
    }
}
