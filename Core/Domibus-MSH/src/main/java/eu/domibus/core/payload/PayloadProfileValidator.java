package eu.domibus.core.payload;

import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.PartProperty;
import eu.domibus.api.model.Property;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Payload;
import eu.domibus.common.model.configuration.PayloadProfile;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class PayloadProfileValidator {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadProfileValidator.class);

    @Autowired
    private PModeProvider pModeProvider;


    public void validate(final UserMessage userMessage, List<PartInfo> partInfoList, final String pmodeKey) throws EbMS3Exception {
        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        final boolean isCompressEnabledInPmode = legConfiguration.isCompressPayloads();

        validateCompressPayloads(isCompressEnabledInPmode, userMessage, partInfoList);
        validatePayloadProfile(legConfiguration, userMessage, partInfoList);
    }

    public void validateCompressPayloads(final boolean isCompressEnabledInPmode, final UserMessage userMessage, List<PartInfo> partInfoList) throws EbMS3Exception {
        if (CollectionUtils.isEmpty(partInfoList)) {
            return;
        }
        for (final PartInfo partInfo : partInfoList) {
            validateCompressPartInfo(isCompressEnabledInPmode, partInfo, userMessage.getMessageId());
        }

    }

    protected void validateCompressPartInfo(final boolean isCompressEnabledInPmode, final PartInfo partInfo, String messageId) throws EbMS3Exception {

        if (partInfo.getPartProperties() == null) {
            if (isCompressEnabledInPmode) {
                LOG.warn("Compression is enabled in the pMode, CompressionType and MimeType properties are not present in [{}]", partInfo.getHref());
            }
            return;
        }

        boolean compress = false;
        String mimeType = null;
        for (Property property : partInfo.getPartProperties()) {
            if (MessageConstants.COMPRESSION_PROPERTY_KEY.equalsIgnoreCase(property.getName())) {
                if (!MessageConstants.COMPRESSION_PROPERTY_VALUE.equalsIgnoreCase(property.getValue())) {
                    throw EbMS3ExceptionBuilder.getInstance()
                            .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0052)
                            .message(MessageConstants.COMPRESSION_PROPERTY_VALUE + " is the only accepted value for CompressionType. Got " + property.getValue())
                            .refToMessageId(messageId)
                            .build();
                }
                compress = true;
            }
            if (Property.MIME_TYPE.equalsIgnoreCase(property.getName())) {
                mimeType = property.getValue();
            }
        }

        if (compress && mimeType == null) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0052)
                    .message("Missing MimeType property when compressions is required")
                    .refToMessageId(messageId)
                    .build();
        }
    }

    protected void validatePayloadProfile(final LegConfiguration legConfiguration, final UserMessage userMessage, List<PartInfo> partInfos) throws EbMS3Exception {
        final List<Payload> modifiableProfileList = new ArrayList<>();
        final String messageId = userMessage.getMessageId();

        final PayloadProfile profile = legConfiguration.getPayloadProfile();
        if (profile == null) {
            LOG.businessInfo(DomibusMessageCode.BUS_PAYLOAD_PROFILE_VALIDATION_SKIP, legConfiguration.getName());
            // no profile means everything is valid
            return;
        }

        modifiableProfileList.addAll(profile.getPayloads());

        for (final PartInfo partInfo : partInfos) {
            Payload profiled = null;
            final String cid = (partInfo.getHref() == null ? StringUtils.EMPTY : partInfo.getHref());
            for (final Payload p : modifiableProfileList) {
                String payloadCid = StringUtils.trimToEmpty(p.getCid());
                if (StringUtils.equalsIgnoreCase(payloadCid, cid)) {
                    profiled = p;
                    break;
                }
            }
            if (profiled == null) {
                LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_WITH_CID_MISSING, cid);
                throw EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                        .message("Payload profiling for this exchange does not include a payload with CID: " + cid)
                        .refToMessageId(userMessage.getMessageId())
                        .build();
            }
            modifiableProfileList.remove(profiled);

            String mime = null;
            if (partInfo.getPartProperties() != null) {
                final Collection<PartProperty> partProperties = partInfo.getPartProperties();
                for (final Property partProperty : partProperties) {
                    if (Property.MIME_TYPE.equalsIgnoreCase(partProperty.getName())) {
                        mime = partProperty.getValue();
                        break;
                    }
                }
            }

            if (profiled.getMimeType() != null && (!StringUtils.equalsIgnoreCase(profiled.getMimeType(), mime)) ||
                    (partInfo.isInBody() != profiled.isInBody())) {
                throw EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                        .message("Payload profiling error: expected: " + profiled + ", got " + partInfo)
                        .refToMessageId(messageId)
                        .build();
            }
        }
        for (final Payload payload : modifiableProfileList) {
            if (payload.isRequired()) {
                LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_MISSING, payload);
                throw EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                        .message("Payload profiling error, missing payload:" + payload)
                        .refToMessageId(messageId)
                        .build();

            }
        }

        LOG.businessInfo(DomibusMessageCode.BUS_PAYLOAD_PROFILE_VALIDATION, profile.getName());
    }

}
