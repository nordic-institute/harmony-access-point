package eu.domibus.core.util;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class JmsUtil {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(JmsUtil.class);

    private final DomainContextProvider domainContextProvider;

    public JmsUtil(DomainContextProvider domainContextProvider) {
        this.domainContextProvider = domainContextProvider;
    }

    public String getStringPropertySafely(Message message, String variable) {
        String property;
        try {
            property = message.getStringProperty(variable);
        } catch (JMSException e) {
            LOG.debug("Could not get the [{}]", variable, e);
            property = null;
        }
        return property;
    }

    public Long getLongPropertySafely(Message message, String variable) {
        Long property;
        try {
            property = Long.parseLong(message.getStringProperty(variable));
        } catch (NumberFormatException | JMSException e) {
            LOG.debug("Could not get the [{}]", variable, e);
            property = null;
        }
        return property;
    }

    public void setDomain(Message message) {
        String domainCode = getStringPropertySafely(message, MessageConstants.DOMAIN);
        if (StringUtils.isNotEmpty(domainCode)) {
            domainContextProvider.setCurrentDomain(domainCode);
        } else {
            domainContextProvider.clearCurrentDomain();
        }
    }

    public String getMessageTypeSafely(Message message) {
        String type;
        try {
            type = message.getJMSType();
        } catch (JMSException e) {
            LOG.debug("Could not get the message type!", e);
            type = null;
        }
        return type;
    }

    public boolean isMessageTypeSafely(Message message, String type) {
        return StringUtils.equals(type, getMessageTypeSafely(message));
    }
}
