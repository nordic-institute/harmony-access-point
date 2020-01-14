package eu.domibus.common.validators;

import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.validators.ObjectWhiteListed;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Custom validator that checks that all Strings in the array do not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class ObjectPropertiesMapBlacklistValidator extends BaseBlacklistValidator<ObjectWhiteListed, ObjectPropertiesMapBlacklistValidator.Parameter> {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ObjectPropertiesMapBlacklistValidator.class);

    @PostConstruct
    public void init() {
        blacklistValidator.init();
    }

    @Autowired
    ItemsBlacklistValidator blacklistValidator;

//    ((HandlerMethod) handler).getMethodParameters()[0].parameterType.getDeclaredField("selector").getAnnotations()

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
        Class type = null;
        if (value.getTypes() != null && !value.getTypes().isEmpty()) {
            type = value.getTypes().get(0);
        }

        for (Map.Entry<String, String[]> pair : valuesMap.entrySet()) {
            CustomWhiteListed whitelistAnnotation = null;
            if (type != null) {
                String prop = pair.getKey();
                try {
                    Field field = type.getDeclaredField(prop);
                    whitelistAnnotation = field.getAnnotation(CustomWhiteListed.class);
                } catch (NoSuchFieldException e) {
                    LOG.trace("Could not find property named [{}]", prop);
                }
            }

            String[] val = pair.getValue();
            if (!blacklistValidator.isValid(val, whitelistAnnotation)) {
                LOG.debug("Forbidden character detected in the query parameter [{}]:[{}] ", pair.getKey(), val);
                return false;
            }
        }

        LOG.debug("Successfully validated values: [{}]", valuesMap);
        return true;
    }


    public static class Parameter {
        public Map<String, String[]> getValues() {
            return values;
        }

        public void setValues(Map<String, String[]> values) {
            this.values = values;
        }

        Map<String, String[]> values;

        public List<? extends Class<?>> getTypes() {
            return types;
        }

        public void setTypes(List<? extends Class<?>> types) {
            this.types = types;
        }

        List<? extends Class<?>> types;

        public Parameter(Map<String, String[]> values, List<? extends Class<?>> types) {
            this.values = values;
            this.types = types;
        }
    }
}
