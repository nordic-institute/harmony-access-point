package eu.domibus.core.message;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.Property;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.payload.persistence.PayloadPersistenceHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartInfoHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartInfoHelper.class);

    protected PayloadPersistenceHelper payloadPersistenceHelper;

    public PartInfoHelper(PayloadPersistenceHelper payloadPersistenceHelper) {
        this.payloadPersistenceHelper = payloadPersistenceHelper;
    }

    public void validatePayloadSizeBeforeSchedulingSave(LegConfiguration legConfiguration, List<PartInfo> partInfos) {
        for (PartInfo partInfo : partInfos) {
            payloadPersistenceHelper.validatePayloadSize(legConfiguration, partInfo.getLength(), true);
        }
    }

    /**
     * Required for AS4_TA_12
     *
     * @param userMessage the UserMessage received
     * @throws EbMS3Exception if an attachment with an invalid charset is received
     */
    public void checkPartInfoCharset(final UserMessage userMessage, List<PartInfo> partInfoList) throws EbMS3Exception {
        LOG.debug("Checking charset for attachments");
        if (partInfoList == null) {
            LOG.debug("No partInfo found");
            return;
        }

        for (final PartInfo partInfo : partInfoList) {
            if (partInfo.getPartProperties() == null) {
                continue;
            }
            for (final Property property : partInfo.getPartProperties()) {
                if (Property.CHARSET.equalsIgnoreCase(property.getName()) && !Property.CHARSET_PATTERN.matcher(property.getValue()).matches()) {
                    LOG.businessError(DomibusMessageCode.BUS_MESSAGE_CHARSET_INVALID, property.getValue(), userMessage.getMessageId());
                    throw EbMS3ExceptionBuilder.getInstance()
                            .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                            .message(property.getValue() + " is not a valid Charset")
                            .refToMessageId( userMessage.getMessageId())
                            .mshRole(MSHRole.RECEIVING)
                            .build();
                }
            }
        }
    }
}
