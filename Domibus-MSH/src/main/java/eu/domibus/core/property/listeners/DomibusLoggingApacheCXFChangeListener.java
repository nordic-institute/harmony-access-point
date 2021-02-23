package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.logging.cxf.DomibusLoggingEventSender;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.cxf.ext.logging.LoggingFeature;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @since 4.2.1
 * @author Catalin Enache
 */
public class DomibusLoggingApacheCXFChangeListener implements DomibusPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusLoggingApacheCXFChangeListener.class);

    LoggingFeature loggingFeature;

    DomibusLoggingEventSender loggingSender;

    public DomibusLoggingApacheCXFChangeListener(LoggingFeature loggingFeature, DomibusLoggingEventSender loggingSender) {
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

        switch (propertyName) {
            case DOMIBUS_LOGGING_CXF_LIMIT:
                int cxfLimit = NumberUtils.toInt(propertyValue);
                LOG.debug("Setting new value of Logging Apache CXF Limit [{}]", cxfLimit);
                loggingFeature.setLimit(cxfLimit);
                break;
            case DOMIBUS_LOGGING_PAYLOAD_PRINT:
                boolean printPayload = BooleanUtils.toBoolean(propertyValue);
                LOG.debug("Setting new value of Logging Apache CXF print payload [{}]", printPayload);
                loggingSender.setPrintPayload(printPayload);
                break;
            case DOMIBUS_LOGGING_METADATA_PRINT:
                boolean metadataPrint = BooleanUtils.toBoolean(propertyValue);
                LOG.debug("Setting new value of Logging Apache CXF metadata print [{}]", metadataPrint);
                loggingSender.setPrintMetadata(metadataPrint);
                break;
        }

    }
}
