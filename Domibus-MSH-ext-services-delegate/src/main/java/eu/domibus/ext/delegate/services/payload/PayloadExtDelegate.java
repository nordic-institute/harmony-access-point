package eu.domibus.ext.delegate.services.payload;

import eu.domibus.core.spi.validation.UserMessageValidatorSpi;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.PayloadExtException;
import eu.domibus.ext.services.PayloadExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class PayloadExtDelegate implements PayloadExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadExtDelegate.class);

    protected UserMessageValidatorSpi userMessageValidatorSpi;

    public PayloadExtDelegate(@Autowired(required = false) UserMessageValidatorSpi userMessageValidatorSpi) {
        this.userMessageValidatorSpi = userMessageValidatorSpi;
    }

    @Override
    public void validatePayload(InputStream payload, String mimeType) throws PayloadExtException {
        if (!isValidatorActive()) {
            throw new PayloadExtException(DomibusErrorCode.DOM_005, "Validation skipped: validator SPI is not active");
        }
        LOG.debug("Validating payload");
        userMessageValidatorSpi.validatePayload(payload, mimeType);
    }

    public boolean isValidatorActive() {
        return userMessageValidatorSpi != null;
    }
}
