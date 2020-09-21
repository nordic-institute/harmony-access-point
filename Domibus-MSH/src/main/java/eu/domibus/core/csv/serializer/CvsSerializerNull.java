package eu.domibus.core.csv.serializer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author François Gautier
 * @since 4.2
 */
@Service
public class CvsSerializerNull implements CvsSerializer {

    @Override
    public boolean canHandle(Object fieldValue) {
        return fieldValue == null;
    }

    @Override
    public String serialize(Object fieldValue) {
        return Objects.toString(fieldValue, StringUtils.EMPTY);
    }
}
