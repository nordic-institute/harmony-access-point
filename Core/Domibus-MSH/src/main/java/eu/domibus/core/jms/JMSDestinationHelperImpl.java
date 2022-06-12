package eu.domibus.core.jms;

import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.RegexUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_INTERNAL_QUEUE_EXPRESSION;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@Component
public class JMSDestinationHelperImpl implements JMSDestinationHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSDestinationHelperImpl.class);

    private static final String INTERNALQUEUE_EXPRESSION = DOMIBUS_JMS_INTERNAL_QUEUE_EXPRESSION;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    RegexUtil regexUtil;

    @Override
    public boolean isInternal(String name) {
        String internalQueueExpression = domibusPropertyProvider.getProperty(INTERNALQUEUE_EXPRESSION);
        LOG.debug("Property [{}], value [{}]", INTERNALQUEUE_EXPRESSION, internalQueueExpression);
        return regexUtil.matches(internalQueueExpression, name);
    }
}
