package eu.domibus.ext.delegate.services.payload;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.payload.PartInfoService;
import eu.domibus.api.usermessage.UserMessageLogService;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.core.spi.validation.UserMessageValidatorSpi;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.PartInfoDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.PayloadExtException;
import eu.domibus.ext.services.PayloadExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class PayloadExtDelegate implements PayloadExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadExtDelegate.class);

    protected UserMessageValidatorSpi userMessageValidatorSpi;
    protected PartInfoService partInfoService;
    protected DomibusExtMapper domibusExtMapper;
    protected UserMessageService userMessageService;
    protected UserMessageLogService userMessageLogService;
    protected UserMessageSecurityService userMessageSecurityService;

    public PayloadExtDelegate(@Autowired(required = false) UserMessageValidatorSpi userMessageValidatorSpi,
                              PartInfoService partInfoService,
                              DomibusExtMapper domibusExtMapper,
                              UserMessageService userMessageService,
                              UserMessageLogService userMessageLogService,
                              UserMessageSecurityService userMessageSecurityService) {
        this.userMessageValidatorSpi = userMessageValidatorSpi;
        this.partInfoService = partInfoService;
        this.domibusExtMapper = domibusExtMapper;
        this.userMessageService = userMessageService;
        this.userMessageLogService = userMessageLogService;
        this.userMessageSecurityService = userMessageSecurityService;
    }

    @Override
    public void validatePayload(InputStream payload, String mimeType) throws PayloadExtException {
        if (!isValidatorActive()) {
            throw new PayloadExtException(DomibusErrorCode.DOM_005, "Validation skipped: validator SPI is not active");
        }
        LOG.debug("Validating payload");
        userMessageValidatorSpi.validatePayload(payload, mimeType);
    }

    @Override
    public PartInfoDTO getPayload(Long messageEntityId, String cid) throws PayloadExtException {
        final UserMessage userMessage = userMessageService.getMessageEntity(messageEntityId);
        if (userMessage == null) {
            throw new PayloadExtException(DomibusErrorCode.DOM_005, "Could not find message with entity id [" + messageEntityId + "]");
        }
        return getPartInfoDTO(userMessage, cid);
    }

    @Override
    public PartInfoDTO getPayload(String messageId, String cid) throws PayloadExtException {
        final UserMessage userMessage = userMessageService.getMessageEntity(messageId);
        if (userMessage == null) {
            throw new PayloadExtException(DomibusErrorCode.DOM_005, "Could not find message with message id [" + messageId + "]");
        }
        return getPartInfoDTO(userMessage, cid);
    }

    protected PartInfoDTO getPartInfoDTO(UserMessage userMessage, String cid) {
        MessageStatus messageStatus = userMessageLogService.getMessageStatus(userMessage.getEntityId());
        if(MessageStatus.DELETED == messageStatus) {
            throw new PayloadExtException(DomibusErrorCode.DOM_005, "The payloads for message [" + userMessage.getMessageId() + "] have been deleted");
        }

        userMessageSecurityService.checkMessageAuthorization(userMessage);

        final PartInfo partInfo = partInfoService.findPartInfo(userMessage.getEntityId(), cid);
        return domibusExtMapper.partInfoToDto(partInfo);
    }

    public boolean isValidatorActive() {
        return userMessageValidatorSpi != null;
    }
}
