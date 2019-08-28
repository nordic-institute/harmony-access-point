package eu.domibus.ebms3.common.validators;

import eu.domibus.common.model.configuration.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** This validator checks everything related to Leg Configuration
 * @author Catalin Enache
 * @since 4.1.1
 */
@Component
@Order(3)
public class LegValidator implements ConfigurationValidator {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LegValidator.class);

    @Override
    public List<String> validate(Configuration configuration) {

        List<String> issues = new ArrayList<>();

        final BusinessProcesses businessProcesses = configuration.getBusinessProcesses();

        for (LegConfiguration legConfiguration: businessProcesses.getLegConfigurations()) {
               Service service = legConfiguration.getService();
               String message;
               if (null == service) {
                   message = "Invalid service specified for leg configuration [" + legConfiguration.getName() + ']';
                   issues.add(message);
                   LOG.debug(message);
               }
               Action action = legConfiguration.getAction();
            if (null == action) {
                message = "Invalid action specified for leg configuration [" + legConfiguration.getName() + ']';
                issues.add(message);
                LOG.debug(message);
            }
        }

        return Collections.unmodifiableList(issues);
    }
}
