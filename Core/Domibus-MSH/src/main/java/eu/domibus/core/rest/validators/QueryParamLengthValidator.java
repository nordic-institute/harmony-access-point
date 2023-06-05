package eu.domibus.core.rest.validators;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.validation.ValidationException;
import java.util.Map;

/**
 * @author François Gautier
 * @since 5.0
 */
@Component
public class QueryParamLengthValidator {

    public static final int MAX_LENGTH_STRING_DATABASE = 255;

    public void isValid(String key, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        if (value.length() > MAX_LENGTH_STRING_DATABASE) {
            throw new ValidationException("The param [" + key + "] with value [" + value + "] has more than 255 char.");
        }
    }

    public void validate(Map<String, String[]> queryParams) {
        for (Map.Entry<String, String[]> stringEntry : queryParams.entrySet()) {
            for (String s : stringEntry.getValue()) {
                isValid(stringEntry.getKey(), s);
            }
        }
    }
}
