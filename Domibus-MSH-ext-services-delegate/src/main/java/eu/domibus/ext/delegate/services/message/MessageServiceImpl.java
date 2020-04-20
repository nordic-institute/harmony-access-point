package eu.domibus.ext.delegate.services.message;

import eu.domibus.ext.services.MessageExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author Sebastian-Ion TINCU
 * @author Catalin Enache
 * @since 4.1
 */
@Service
public class MessageServiceImpl implements MessageExtService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageServiceImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String cleanMessageIdentifier(String messageId) {
        return StringUtils.trimToEmpty(messageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sanitizeMessagePropertyFileName(String propertyName, String propertyValue) {
        final String sanitizedValue = FilenameUtils.getName(propertyValue);
        if (StringUtils.isBlank(sanitizedValue)) {
            LOG.debug("Unable to sanitize {} which has the value [{}]", propertyName, propertyValue);
            return null;
        }
        if (!StringUtils.equals(propertyName, sanitizedValue)) {
            LOG.warn("{} value=[{}] will be sanitized to=[{}]", propertyName, propertyValue, sanitizedValue);
            propertyValue = sanitizedValue;
        }
        return propertyValue;
    }
}
