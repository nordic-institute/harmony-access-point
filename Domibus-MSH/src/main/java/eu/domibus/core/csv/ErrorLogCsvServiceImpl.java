package eu.domibus.core.csv;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@Service
public class ErrorLogCsvServiceImpl extends CsvServiceImpl {

    public ErrorLogCsvServiceImpl(DomibusPropertyProvider domibusPropertyProvider) {
        super(domibusPropertyProvider);
    }

    @Override
    protected String serializeFieldValue(Field field, Object elem) throws IllegalAccessException {
        if (field.getName().equals("errorCode")) {
            Object fieldValue = field.get(elem);
            return ((ErrorCode) fieldValue).name();
        } else {
            return super.serializeFieldValue(field, elem);
        }
    }

}
