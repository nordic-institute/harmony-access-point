package eu.domibus.core.csv.serializer;

import eu.domibus.common.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * @author François Gautier
 * @since 4.2
 */
@Service
public class CvsSerializerErrorCode implements CvsSerializer {

    @Override
    public boolean canHandle(Object fieldValue) {
        return fieldValue instanceof ErrorCode;
    }

    @Override
    public String serialize(Object fieldValue) {
        return ((ErrorCode) fieldValue).name();
    }
}
