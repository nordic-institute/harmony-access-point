package eu.domibus.core.earchive;

import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.core.message.PartInfoService;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.stereotype.Service;

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
public class EArchivingService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingService.class);

    public static final String SOAP_ENVELOPE_XML = "soap.envelope.xml";

    private final UserMessageService userMessageService;

    private final PartInfoService partInfoService;

    private final UserMessageRawEnvelopeDao userMessageRawEnvelopeDao;

    public EArchivingService(UserMessageService userMessageService, PartInfoService partInfoService, UserMessageRawEnvelopeDao userMessageRawEnvelopeDao) {
        this.userMessageService = userMessageService;
        this.partInfoService = partInfoService;
        this.userMessageRawEnvelopeDao = userMessageRawEnvelopeDao;
    }

    public Map<String, InputStream> getArchivingFiles(String messageId) {
        HashMap<String, InputStream> files = new HashMap<>();
        RawEnvelopeDto rawXmlByMessageId = userMessageRawEnvelopeDao.findRawXmlByMessageId(messageId);
        if(rawXmlByMessageId != null) {
            files.put(SOAP_ENVELOPE_XML, new ByteArrayInputStream(rawXmlByMessageId.getRawMessage()));
        }

        UserMessage userMessage = userMessageService.getByMessageId(messageId);

        final List<PartInfo> partInfos = partInfoService.findPartInfo(userMessage);

        for (PartInfo partInfo : partInfos) {
            if (partInfo.getPayloadDatahandler() == null) {
                throw new DomibusEArchiveException("Could not find attachment for [" + partInfo.getHref() + "] and messageId [" + messageId + "]");
            }
            try {
                files.put(getFileName(partInfo), partInfo.getPayloadDatahandler().getInputStream());
            } catch (IOException e) {
                throw new DomibusEArchiveException("Error getting input stream for attachment [" + partInfo.getHref() + "] and messageId [" + messageId + "]", e);
            }
        }
        return files;
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
        // TODO: François Gautier 07-09-21 to be done
        return new ByteArrayInputStream("batch.json content".getBytes(StandardCharsets.UTF_8));
    }
}
