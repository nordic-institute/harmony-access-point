package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
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
public class LegValidator extends AbstractPModeValidator {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LegValidator.class);

    final String[] attributesToCheck = {"service", "action", "security", "defaultMpc", "receptionAwareness", "reliability",
            "errorHandling", "compressPayloads"};

    @Override
    public List<PModeIssue> validate(Configuration configuration) {

        List<PModeIssue> issues = new ArrayList<>();

        final BusinessProcesses businessProcesses = configuration.getBusinessProcesses();

        for (LegConfiguration legConfiguration : businessProcesses.getLegConfigurations()) {
            validateLegConfiguration(issues, legConfiguration);
        }
        return Collections.unmodifiableList(issues);
    }

    /**
     * Validates a legConfiguration object
     *
     * @param issues
     * @param legConfiguration
     */
    private void validateLegConfiguration(List<PModeIssue> issues, LegConfiguration legConfiguration) {
        String message;

        for (String attribute : attributesToCheck) {
            message = validateAttributeAgainstNull(legConfiguration, attribute);
            if (StringUtils.isNotEmpty(message)) {
                message += "for leg configuration [" + legConfiguration.getName() + "]";
                issues.add(new PModeIssue(message, PModeIssue.Level.WARNING));
                LOG.debug(message);
            }
        }
    }

    /**
     * Validates the attribute of a leg
     *
     * @param legConfiguration
     * @param attribute
     * @return
     */
    private String validateAttributeAgainstNull(Object legConfiguration, String attribute) {
        if (legConfiguration == null) {
            return StringUtils.EMPTY;
        }
        Class clazz = legConfiguration.getClass();
        try {
            Field field = clazz.getDeclaredField(attribute);
            field.setAccessible(true);
            Object fieldObject = field.get(legConfiguration);
            if (null == fieldObject) {
                return "Invalid " + attribute + " specified ";
            }
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
            LOG.debug("Unable to access attribute: " + attribute, e);
        }
        return StringUtils.EMPTY;
    }
}
