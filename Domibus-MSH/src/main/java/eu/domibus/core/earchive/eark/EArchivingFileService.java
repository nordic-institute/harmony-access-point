package eu.domibus.core.earchive.eark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.PartProperty;
import eu.domibus.api.model.Property;
import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.earchive.DomibusEArchiveException;
import eu.domibus.core.message.PartInfoService;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.message.compression.DecompressionDataSource;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
    public Map<String, InputStream> getArchivingFiles(Long entityId) {
        HashMap<String, InputStream> files = new HashMap<>();

        RawEnvelopeDto rawXmlByMessageId = userMessageRawEnvelopeDao.findRawXmlByEntityId(entityId);
        if (rawXmlByMessageId != null) {
            files.put(SOAP_ENVELOPE_XML, rawXmlByMessageId.getRawXmlMessageAsStream());
        } else {
            LOG.debug("No userMessageRaw found for entityId [{}]", entityId);
        }

        final List<PartInfo> partInfos = partInfoService.findPartInfo(entityId);

        for (PartInfo partInfo : partInfos) {
            Pair<String, InputStream> file = getFile(entityId, partInfo);
            files.put(file.getLeft(), file.getRight());
        }
        return files;
    }

    protected Pair<String, InputStream> getFile(Long entityId, PartInfo partInfo) {
        if (partInfo.getPayloadDatahandler() == null) {
            throw new DomibusEArchiveException("Could not find attachment for [" + partInfo.getHref() + "] and entityId [" + entityId + "]");
        }
        try {
            Map<String, String> props = getProps(partInfo);
            String mimeType = props.get(Property.MIME_TYPE);
            if (messageIsCompressed(props, mimeType)) {
                return Pair.of(
                        getFileName(partInfo, getExtension(mimeType)),
                        new DecompressionDataSource(partInfo.getPayloadDatahandler().getDataSource(), mimeType).getInputStream());
            }
            return Pair.of(getFileName(partInfo, getExtension(partInfo.getMime())), partInfo.getPayloadDatahandler().getInputStream());
        } catch (IOException e) {
            throw new DomibusEArchiveException("Error getting input stream for attachment [" + partInfo.getHref() + "] and messageId [" + entityId + "]", e);
        }
    }

    private boolean messageIsCompressed(Map<String, String> props, String mimeType) {
        return StringUtils.isNotBlank(mimeType) && StringUtils.equalsIgnoreCase(props.get(CompressionService.COMPRESSION_PROPERTY_KEY), CompressionService.COMPRESSION_PROPERTY_VALUE);
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

    protected String getFileName(PartInfo info) {
        return getBaseName(info) + ".attachment" + getExtension(info.getMime());
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

    public InputStream getBatchFileJson(BatchEArchiveDTO batchEArchiveDTO) {
        try {
            return new ByteArrayInputStream(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(batchEArchiveDTO).getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new DomibusEArchiveException("Could not write Batch.json " + batchEArchiveDTO, e);
        }
    }
}
