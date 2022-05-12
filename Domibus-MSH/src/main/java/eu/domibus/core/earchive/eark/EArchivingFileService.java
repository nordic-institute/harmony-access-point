package eu.domibus.core.earchive.eark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.earchive.DomibusEArchiveException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.compression.DecompressionDataSource;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.PartProperty;
import eu.domibus.api.model.Property;
import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.api.payload.PartInfoService;
import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.messaging.MessageConstants.COMPRESSION_PROPERTY_KEY;
import static eu.domibus.messaging.MessageConstants.COMPRESSION_PROPERTY_VALUE;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchivingFileService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingFileService.class);

    public static final String SOAP_ENVELOPE_XML = "soap.envelope.xml";

    private final PartInfoService partInfoService;

    private final UserMessageRawEnvelopeDao userMessageRawEnvelopeDao;
    private final ObjectMapper objectMapper;

    public EArchivingFileService(PartInfoService partInfoService,
                                 UserMessageRawEnvelopeDao userMessageRawEnvelopeDao,
                                 @Qualifier("domibusJsonMapper") ObjectMapper objectMapper) {
        this.partInfoService = partInfoService;
        this.userMessageRawEnvelopeDao = userMessageRawEnvelopeDao;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    @Timer(clazz = EArchivingFileService.class, value = "earchive_getArchivingFiles")
    @Counter(clazz = EArchivingFileService.class, value = "earchive_getArchivingFiles")
    public Map<String, ArchivingFileDTO> getArchivingFiles(Long entityId) {
        HashMap<String, ArchivingFileDTO> files = new HashMap<>();

        RawEnvelopeDto rawXmlByMessageId = userMessageRawEnvelopeDao.findRawXmlByEntityId(entityId);
        if (rawXmlByMessageId != null) {
            files.put(SOAP_ENVELOPE_XML,
                    ArchivingFileDTOBuilder.getInstance()
                            .setMimeType("application/xml")
                            .setSize((long) rawXmlByMessageId.getRawMessage().length)
                            .setInputStream(rawXmlByMessageId.getRawXmlMessageAsStream())
                            .build());
        } else {
            LOG.debug("No userMessageRaw found for entityId [{}]", entityId);
        }

        final List<PartInfo> partInfos = partInfoService.findPartInfo(entityId);
        //getMimetype in properties
        for (PartInfo partInfo : partInfos) {
            Map<String, String> props = getProps(partInfo);
            String mimeType = props.get(Property.MIME_TYPE);
            // TODO: François Gautier 22-02-22  [EDELIVERY-9001] Unify Compressed flag on PartInfo
            if (BooleanUtils.isNotTrue(partInfo.getCompressed()) && messageIsCompressed(props, mimeType)) {
                files.put(
                        getBaseName(partInfo) + ".attachment" + getExtension(mimeType),
                        getDecompressdInputStream(entityId, partInfo, mimeType));
            } else {
                files.put(
                        getBaseName(partInfo) + ".attachment" + getExtension(partInfo.getMime()),
                        getArchivingFileDTO(entityId, partInfo));
            }
        }
        return files;
    }

    private ArchivingFileDTO getDecompressdInputStream(Long entityId, PartInfo partInfo, String mimeType) {
        if (partInfo.getPayloadDatahandler() == null) {
            throw new DomibusEArchiveException(DomibusCoreErrorCode.DOM_009, "Could not find attachment for [" + partInfo.getHref() + "], messageId [" + partInfo.getUserMessage().getMessageId() + "] and entityId [" + entityId + "]");
        }
        try {
            return ArchivingFileDTOBuilder.getInstance()
                    .setMimeType(mimeType)
                    .setSize(partInfo.getLength())
                    .setInputStream(new DecompressionDataSource(partInfo.getPayloadDatahandler().getDataSource(), mimeType).getInputStream())
                    .build();
        } catch (IOException e) {
            throw new DomibusEArchiveException("Error getting input stream for attachment [" + partInfo.getHref() + "], messageId [" + partInfo.getUserMessage().getMessageId() + "] and entityId [" + entityId + "]", e);
        }
    }

    private boolean messageIsCompressed(Map<String, String> props, String mimeType) {
        return StringUtils.isNotBlank(mimeType) && StringUtils.equalsIgnoreCase(props.get(COMPRESSION_PROPERTY_KEY), COMPRESSION_PROPERTY_VALUE);
    }

    private Map<String, String> getProps(PartInfo partInfo) {
        Map<String, String> props = new HashMap<>();
        if (partInfo != null) {
            for (PartProperty partProperty : partInfo.getPartProperties()) {
                props.put(partProperty.getName(), partProperty.getValue());
            }
        }
        return props;
    }

    protected ArchivingFileDTO getArchivingFileDTO(Long entityId, PartInfo partInfo) {
        if (partInfo.getPayloadDatahandler() == null) {
            throw new DomibusEArchiveException(DomibusCoreErrorCode.DOM_009, "Could not find attachment for [" + partInfo.getHref() + "], messageId [" + partInfo.getUserMessage().getMessageId() + "] and entityId [" + entityId + "]");
        }
        try {
            return ArchivingFileDTOBuilder.getInstance()
                    .setMimeType(partInfo.getMime())
                    .setSize(partInfo.getLength())
                    .setInputStream(partInfo.getPayloadDatahandler().getInputStream())
                    .build();
        } catch (IOException e) {
            throw new DomibusEArchiveException("Error getting input stream for attachment [" + partInfo.getHref() + "], messageId [" + partInfo.getUserMessage().getMessageId() + "] and entityId [" + entityId + "]", e);
        }
    }

    protected String getFileName(PartInfo info, String extension) {
        return getBaseName(info) + ".attachment" + extension;
    }

    private String getExtension(String mime) {
        try {
            return MimeTypes.getDefaultMimeTypes().forName(mime).getExtension();
        } catch (MimeTypeException e) {
            LOG.warn("Mimetype [{}] not found", mime);
            return "";
        }
    }

    private String getBaseName(PartInfo info) {
        if (StringUtils.isEmpty(info.getHref())) {
            return "bodyload";
        }
        if (!info.getHref().contains("cid:")) {
            LOG.warn("PayloadId does not contain \"cid:\" prefix [{}]", info.getHref());
            return info.getHref();
        }

        return info.getHref().replace("cid:", "");
    }

    @Timer(clazz = EArchivingFileService.class, value = "earchive24_getBatchFileJson")
    @Counter(clazz = EArchivingFileService.class, value = "earchive24_getBatchFileJson")
    public InputStream getBatchFileJson(BatchEArchiveDTO batchEArchiveDTO) {
        try {
            return new ByteArrayInputStream(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(batchEArchiveDTO).getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new DomibusEArchiveException("Could not write Batch.json " + batchEArchiveDTO, e);
        }
    }
}
