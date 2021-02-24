package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.logging.cxf.DomibusLoggingEventSender;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * {@code DomibusPropertyChangeListener} implementation for Apache CXF Logging Feature properties:
 * domibus.logging.cxf.limit
 * domibus.logging.payload.print
 * domibus.logging.metadata.print
 *
 * @since 4.2.1
 * @author Catalin Enache
 */
@Service
public class DomibusLoggingApacheCXFChangeListener implements DomibusPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusLoggingApacheCXFChangeListener.class);

    LoggingFeature loggingFeature;

    DomibusLoggingEventSender loggingSender;

    public DomibusLoggingApacheCXFChangeListener(@Qualifier("loggingFeature") LoggingFeature loggingFeature,
                                                 @Qualifier("loggingSender") DomibusLoggingEventSender loggingSender) {
        this.loggingFeature = loggingFeature;
        this.loggingSender = loggingSender;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName,
                DOMIBUS_LOGGING_CXF_LIMIT,
                DOMIBUS_LOGGING_PAYLOAD_PRINT,
                DOMIBUS_LOGGING_METADATA_PRINT);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {

        String logDebugMsg = "Setting new value of property [{}] to [{}] on domain [{}]";
        switch (propertyName) {
            case DOMIBUS_LOGGING_CXF_LIMIT:
                int cxfLimit = NumberUtils.toInt(propertyValue);
                LOG.debug(logDebugMsg, propertyName, cxfLimit, domainCode);
                loggingFeature.setLimit(cxfLimit);
                break;
            case DOMIBUS_LOGGING_PAYLOAD_PRINT:
                boolean printPayload = BooleanUtils.toBoolean(propertyValue);
                LOG.debug(logDebugMsg, propertyName, printPayload, domainCode);
                loggingSender.setPrintPayload(printPayload);
                break;
            case DOMIBUS_LOGGING_METADATA_PRINT:
                boolean metadataPrint = BooleanUtils.toBoolean(propertyValue);
                LOG.debug(logDebugMsg, propertyName, metadataPrint, domainCode);
                loggingSender.setPrintMetadata(metadataPrint);
                break;
        }
    }
}
