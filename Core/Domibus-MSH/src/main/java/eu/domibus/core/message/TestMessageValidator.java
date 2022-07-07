package eu.domibus.core.message;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.ErrorResultImpl;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Spin-off from UserMessageHandlerServiceImpl to break a cyclic dependency
 */
@Service
public class TestMessageValidator {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TestMessageValidator.class);

    public Boolean checkTestMessage(final UserMessage message) {
        return checkTestMessage(message.getServiceValue(), message.getActionValue());
    }

    public Boolean checkTestMessage(final String service, final String action) {
        LOG.debug("Checking if the user message represented by the service [{}] and the action [{}] is a test message", service, action);

        return Ebms3Constants.TEST_SERVICE.equalsIgnoreCase(service) && Ebms3Constants.TEST_ACTION.equalsIgnoreCase(action);
    }

    public Boolean checkTestMessage(final LegConfiguration legConfiguration) {
        if (legConfiguration == null) {
            LOG.debug("No leg configuration found");
            return false;
        }

        return checkTestMessage(legConfiguration.getService().getValue(), legConfiguration.getAction().getValue());
    }

}
