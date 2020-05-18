package eu.domibus.core.payload;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Payload;
import eu.domibus.common.model.configuration.PayloadProfile;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class PayloadProfileValidator {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadProfileValidator.class);

    @Autowired
    private PModeProvider pModeProvider;


    public void validate(final Messaging messaging, final String pmodeKey) throws EbMS3Exception {
        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        final boolean isCompressEnabledInPmode = legConfiguration.isCompressPayloads();

        validateCompressPayloads(isCompressEnabledInPmode, messaging.getUserMessage());
        validatePayloadProfile(legConfiguration, messaging.getUserMessage());
    }

    public void validateCompressPayloads(final boolean isCompressEnabledInPmode, final UserMessage userMessage) throws EbMS3Exception {
        if (userMessage.getPayloadInfo() == null) {
            return;
        }
        for (final PartInfo partInfo : userMessage.getPayloadInfo().getPartInfo()) {
            validatePartInfo(isCompressEnabledInPmode, partInfo, userMessage.getMessageInfo().getMessageId());
        }

    }

    protected void validatePartInfo(final boolean isCompressEnabledInPmode, final PartInfo partInfo, String messageId) throws EbMS3Exception {

        if (partInfo.getPartProperties() == null) {
            if (isCompressEnabledInPmode) {
                LOG.warn("Compression is enabled in the pMode, CompressionType and MimeType properties are not present in [{}]", partInfo.getHref());
            }
            return;
        }

        boolean compress = false;
        String mimeType = null;
        for (Property property : partInfo.getPartProperties().getProperties()) {
            if (CompressionService.COMPRESSION_PROPERTY_KEY.equalsIgnoreCase(property.getName())) {
                if (!CompressionService.COMPRESSION_PROPERTY_VALUE.equalsIgnoreCase(property.getValue())) {
                    throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0052, CompressionService.COMPRESSION_PROPERTY_VALUE + " is the only accepted value for CompressionType. Got " + property.getValue(), messageId, null);
                }
                compress = true;
            }
            if (Property.MIME_TYPE.equalsIgnoreCase(property.getName())) {
                mimeType = property.getValue();
            }
        }


        if (compress && mimeType == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0052, "Missing MimeType property when compressions is required", messageId, null);
        }
    }

    protected void validatePayloadProfile(final LegConfiguration legConfiguration, final UserMessage userMessage) throws EbMS3Exception {
        final List<Payload> modifiableProfileList = new ArrayList<>();
        final String messageId = userMessage.getMessageInfo().getMessageId();

        final PayloadProfile profile = legConfiguration.getPayloadProfile();
        if (profile == null) {
            LOG.businessInfo(DomibusMessageCode.BUS_PAYLOAD_PROFILE_VALIDATION_SKIP, legConfiguration.getName());
            // no profile means everything is valid
            return;
        }

        int profileMaxSize = profile.getMaxSize();
        if (profileMaxSize < 0) {
            LOG.warn("Payload profile [{}] has a negative maxSize value [{}]", profile.getName(), profileMaxSize);
        }

        modifiableProfileList.addAll(profile.getPayloads());
        List<PartInfo> partInfos = new ArrayList<>();
        if (userMessage.getPayloadInfo() != null) {
            partInfos = userMessage.getPayloadInfo().getPartInfo();
        }
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
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload profiling for this exchange does not include a payload with CID: " + cid, userMessage.getMessageInfo().getMessageId(), null);
            }
            modifiableProfileList.remove(profiled);

            String mime = null;
            if (partInfo.getPartProperties() != null) {
                final Collection<Property> partProperties = partInfo.getPartProperties().getProperties();
                for (final Property partProperty : partProperties) {
                    if (Property.MIME_TYPE.equalsIgnoreCase(partProperty.getName())) {
                        mime = partProperty.getValue();
                        break;
                    }
                }
            }

            if (profiled.getMimeType() != null && (!StringUtils.equalsIgnoreCase(profiled.getMimeType(), mime)) ||
                    (partInfo.isInBody() != profiled.isInBody())) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload profiling error: expected: " + profiled + ", got " + partInfo, messageId, null);
            }

            //validate the size of the payload
            validatePartInfoMaxSize(profileMaxSize, partInfo, messageId);

        }
        for (final Payload payload : modifiableProfileList) {
            if (payload.isRequired()) {
                LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_MISSING, payload);
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload profiling error, missing payload:" + payload, messageId, null);

            }
        }

        LOG.businessInfo(DomibusMessageCode.BUS_PAYLOAD_PROFILE_VALIDATION, profile.getName());
    }


    protected void validatePartInfoMaxSize(int profileMaxSize, PartInfo partInfo, final String messageId) throws EbMS3Exception {
        int partInfoSize = -1;

        try {
            partInfoSize = partInfo.getPayloadDatahandler().getDataSource().getInputStream().available();
        } catch (IOException e) {
            LOG.warn("Unable to get the size of the payload [{}]", partInfo.getFileName());
        }

        if (partInfoSize > profileMaxSize) {
            //LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_MISSING, payload);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload size [" + partInfoSize + "] is greater than the maximum value defined [" + profileMaxSize + "]", messageId, null);
        }
    }
}
