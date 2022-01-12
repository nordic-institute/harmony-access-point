package eu.domibus.ext.delegate.services.payload;

import eu.domibus.core.spi.validation.UserMessageValidatorSpi;
import eu.domibus.ext.exceptions.PayloadExtException;
import eu.domibus.ext.services.PayloadExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;

public class PayloadExtDelegate implements PayloadExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadExtDelegate.class);

    protected UserMessageValidatorSpi userMessageValidatorSpi;

    public PayloadExtDelegate(@Autowired(required = false) UserMessageValidatorSpi userMessageValidatorSpi) {
        this.userMessageValidatorSpi = userMessageValidatorSpi;
    }

    @Override
    public void validatePayload(InputStream payload, String mimeType) throws PayloadExtException {
        if (!isValidatorActive()) {
            LOG.debug("Validation skipped: validator SPI is not active");
            return;
        }
        LOG.debug("Validating payload");
        userMessageValidatorSpi.validatePayload(payload, mimeType);
    }

    public boolean isValidatorActive() {
        return userMessageValidatorSpi != null;
    }
}
