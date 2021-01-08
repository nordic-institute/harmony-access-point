package eu.domibus.core.property.encryption;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import static eu.domibus.core.property.encryption.PasswordEncryptionServiceImpl.ENC_START;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class PasswordDecryptionHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordDecryptionHelper.class);

    public boolean isValueEncrypted(final String propertyValue) {
        if (isBlank(propertyValue)) {
            LOG.trace("[{}] is blalnk, returning false", propertyValue);
            return false;
        }

        return trim(propertyValue).startsWith(ENC_START);
    }

}
