package eu.domibus.core.earchive.eark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.earchive.DomibusEArchiveException;
import eu.domibus.core.message.PartInfoService;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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
            files.put(getFileName(partInfo), getInputStream(entityId, partInfo));
        }
        return files;
    }

    private InputStream getInputStream(Long entityId, PartInfo partInfo) {
        if (partInfo.getPayloadDatahandler() == null) {
            throw new DomibusEArchiveException("Could not find attachment for [" + partInfo.getHref() + "] and entityId [" + entityId + "]");
        }
        try {
            return partInfo.getPayloadDatahandler().getInputStream();
        } catch (IOException e) {
            throw new DomibusEArchiveException("Error getting input stream for attachment [" + partInfo.getHref() + "] and messageId [" + entityId + "]", e);
        }
    }

    protected String getFileName(PartInfo info) {
        return getBaseName(info) + ".attachment" + getExtension(info);
    }

    private String getExtension(PartInfo info) {
        try {
            return MimeTypes.getDefaultMimeTypes().forName(info.getMime()).getExtension();
        } catch (MimeTypeException e) {
            LOG.warn("Mimetype [{}] not found", info.getMime());
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
