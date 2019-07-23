package eu.domibus.core.csv;

import eu.domibus.common.ErrorCode;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@Service
public class ErrorLogCsvServiceImpl extends CsvServiceImpl {

    @Override
    protected String serializeFieldValue(Field field, Object elem) throws IllegalAccessException {
        if (field.getName().equals("errorCode")) {
            Object fieldValue = field.get(elem);
            String res = ((ErrorCode) fieldValue).name();
            return res;
        } else {
            return super.serializeFieldValue(field, elem);
        }
    }

}
