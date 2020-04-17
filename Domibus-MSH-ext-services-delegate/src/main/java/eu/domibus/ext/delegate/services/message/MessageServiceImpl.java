package eu.domibus.ext.delegate.services.message;

import eu.domibus.ext.services.MessageExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author Sebastian-Ion TINCU
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
    public String sanitizePayloadName(String fileName) {
        if (fileName == null) {
            return null;
        }
        final String sanitizedFileName = FilenameUtils.getName(fileName);
        if (StringUtils.isNotBlank(sanitizedFileName) && !StringUtils.equals(fileName, sanitizedFileName)) {
            LOG.warn("{} has an improper value: [{}]", MessageConstants.PAYLOAD_PROPERTY_FILE_NAME, fileName);
            fileName = sanitizedFileName;
        }
        LOG.debug("{} will be: [{}]", MessageConstants.PAYLOAD_PROPERTY_FILE_NAME, fileName);
        return fileName;
    }
}
