package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class DomibusPropertyValidatorService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyValidatorService.class);

    private final DomibusPropertyProvider domibusPropertyProvider;

    public DomibusPropertyValidatorService(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    public void enforceValidation() {
        validationEArchiveAndRetention();
    }

    private void validationEArchiveAndRetention() {
        Boolean earchive = domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_ACTIVE);
        Boolean deleteOnSuccess = domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD);

        if(BooleanUtils.isTrue(earchive) && BooleanUtils.isTrue(deleteOnSuccess)){
            LOG.warn(WarningUtil.warnOutput("Delete payload on success is active AND Earchive is active -> force disable Delete payload on success "));
            domibusPropertyProvider.setProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD, "false");
        }
    }
}
