package eu.domibus.ebms3.common.validators;

import eu.domibus.common.model.configuration.BusinessProcesses;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This validator checks everything related to Leg Configuration
 *
 * @author Catalin Enache
 * @since 4.1.1
 */
@Component
@Order(3)
public class LegValidator implements ConfigurationValidator {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LegValidator.class);

    final String[] attributesToCheck = {"service", "action", "security", "defaultMpc", "receptionAwareness", "reliability",
            "errorHandling", "compressPayloads"};

    @Override
    public List<String> validate(Configuration configuration) {

        List<String> issues = new ArrayList<>();

        final BusinessProcesses businessProcesses = configuration.getBusinessProcesses();

        for (LegConfiguration legConfiguration : businessProcesses.getLegConfigurations()) {
            String message;

            for (String attribute: attributesToCheck) {
                message = validateAttributeAgainstNull(legConfiguration, attribute);
                if (StringUtils.isNotEmpty(message)) {
                    message += "for leg configuration [" + legConfiguration.getName() + "]";
                    issues.add(message);
                    LOG.debug(message);
                }
            }
        }

        return Collections.unmodifiableList(issues);
    }

    private String validateAttributeAgainstNull(Object object, String fieldName) {
        if (object == null) {
            return StringUtils.EMPTY;
        }

        Class clazz = object.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object fieldObject = field.get(object);
            if (null == fieldObject) {
                return "Invalid " + fieldName + " specified ";
            }
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
            LOG.debug("Unable to access attribute: " + fieldName);
        }
        return StringUtils.EMPTY;
    }
}
