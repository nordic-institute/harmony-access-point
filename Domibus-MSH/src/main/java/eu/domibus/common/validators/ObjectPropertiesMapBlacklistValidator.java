package eu.domibus.common.validators;

import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.validators.ObjectWhiteListed;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Custom validator that checks that all Strings in the map do not contain any char from the blacklist
 * Uses the custom annotation if it is declared on the fields of the type class
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Component
public class ObjectPropertiesMapBlacklistValidator extends BaseBlacklistValidator<ObjectWhiteListed, ObjectPropertiesMapBlacklistValidator.Parameter> {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ObjectPropertiesMapBlacklistValidator.class);

    @PostConstruct
    public void init() {
        listValidator.init();
    }

    @Autowired
    ItemsBlacklistValidator listValidator;

    @Override
    protected String getErrorMessage() {
        return ObjectWhiteListed.MESSAGE;
    }

    @Override
    public boolean isValid(ObjectPropertiesMapBlacklistValidator.Parameter value, CustomWhiteListed customAnnotation) {
        Map<String, String[]> valuesMap = value.getValues();
        if (valuesMap == null || valuesMap.isEmpty()) {
            LOG.debug("Parameters map is empty, exiting");
            return true;
        }

        MethodParameter parameterInfo = value.getParameterInfo();
        for (Map.Entry<String, String[]> pair : valuesMap.entrySet()) {
            CustomWhiteListed whitelistAnnotation = null;
            // use custom whitelist chars, if declared on the parameter or corresponding field
            if (parameterInfo != null) {
                whitelistAnnotation = parameterInfo.getParameterAnnotation(CustomWhiteListed.class);
                String prop = pair.getKey();
                try {
                    Field field = parameterInfo.getParameterType().getDeclaredField(prop);
                    CustomWhiteListed fieldWhitelistAnnotation = field.getAnnotation(CustomWhiteListed.class);
                    if (fieldWhitelistAnnotation != null) {
                        // field annotation takes precedence
                        whitelistAnnotation = fieldWhitelistAnnotation;
                    }
                } catch (NoSuchFieldException e) {
                    LOG.trace("Could not find property named [{}] in metadata class", prop);
                }
            }

            String[] val = pair.getValue();
            if (!listValidator.isValid(val, whitelistAnnotation)) {
                LOG.debug("Forbidden character detected in the query parameter [{}]:[{}] ", pair.getKey(), val);
                return false;
            }
        }

        LOG.debug("Successfully validated values: [{}]", valuesMap);
        return true;
    }

    public static class Parameter {
        private Map<String, String[]> values;
        private MethodParameter parameterInfo;

        public Map<String, String[]> getValues() {
            return values;
        }

        public void setValues(Map<String, String[]> values) {
            this.values = values;
        }

        public MethodParameter getParameterInfo() {
            return parameterInfo;
        }

        public void setParameterInfo(MethodParameter parameterInfo) {
            this.parameterInfo = parameterInfo;
        }

        public Parameter(Map<String, String[]> values, MethodParameter parameterInfo) {
            this.values = values;
            this.parameterInfo = parameterInfo;
        }
    }
}
