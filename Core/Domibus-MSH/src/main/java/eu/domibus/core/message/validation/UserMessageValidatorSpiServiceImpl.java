package eu.domibus.core.message.validation;

import eu.domibus.api.message.compression.DecompressionDataSource;
import eu.domibus.api.message.validation.UserMessageValidatorSpiService;
import eu.domibus.api.message.validation.UserMessageValidatorServiceDelegate;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.usermessage.domain.PayloadInfo;
import eu.domibus.api.usermessage.domain.Property;
import eu.domibus.core.converter.MessageCoreMapper;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import java.util.HashSet;
import java.util.List;

@Service
public class UserMessageValidatorSpiServiceImpl implements UserMessageValidatorSpiService {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageValidatorSpiServiceImpl.class);

    protected MessageCoreMapper messageCoreMapper;
    protected UserMessageValidatorServiceDelegate userMessageValidatorServiceDelegate;
    protected CompressionService compressionService;

    public UserMessageValidatorSpiServiceImpl(MessageCoreMapper messageCoreMapper,
                                              UserMessageValidatorServiceDelegate userMessageValidatorServiceDelegate,
                                              CompressionService compressionService) {
        this.messageCoreMapper = messageCoreMapper;
        this.userMessageValidatorServiceDelegate = userMessageValidatorServiceDelegate;
        this.compressionService = compressionService;
    }

    @Override
    public void validate(UserMessage userMessage, List<PartInfo> partInfoList) {
        final eu.domibus.api.usermessage.domain.UserMessage userMessageModel = messageCoreMapper.userMessageToUserMessageApi(userMessage);

        final PayloadInfo payloadInfo = createPayloadInfo(partInfoList);
        userMessageModel.setPayloadInfo(payloadInfo);

        userMessageValidatorServiceDelegate.validate(userMessageModel);
    }

    protected PayloadInfo createPayloadInfo(List<PartInfo> partInfoList) {
        PayloadInfo result = new PayloadInfo();
        result.setPartInfo(new HashSet<>());

        for (PartInfo partInfo : partInfoList) {
            final eu.domibus.api.usermessage.domain.PartInfo partInfoModel = messageCoreMapper.convertPartInfo(partInfo);
            handleDecompression(partInfoModel);
            result.getPartInfo().add(partInfoModel);

        }
        return result;
    }

    protected void handleDecompression(eu.domibus.api.usermessage.domain.PartInfo partInfo) {
        if (!isCompressed(partInfo)) {
            LOG.debug("PartInfo [{}] is not compressed", partInfo.getHref());
            return;
        }
        LOG.debug("Handling decompression for PartInfo [{}]", partInfo.getHref());

        DecompressionDataSource dataSource = new DecompressionDataSource(partInfo.getPayloadDatahandler().getDataSource(), partInfo.getMime());
        partInfo.setPayloadDatahandler(new DataHandler(dataSource));
    }

    protected boolean isCompressed(eu.domibus.api.usermessage.domain.PartInfo partInfo) {
        if (partInfo.getPartProperties() == null) {
            return false;
        }
        if(partInfo.getPartProperties().getProperty() == null) {
            return false;
        }

        for (final Property property : partInfo.getPartProperties().getProperty()) {
            if (MessageConstants.COMPRESSION_PROPERTY_KEY.equalsIgnoreCase(property.getName()) && MessageConstants.COMPRESSION_PROPERTY_VALUE.equalsIgnoreCase(property.getValue())) {
                return true;
            }
        }
        return false;
    }
}
