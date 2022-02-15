package eu.domibus.test;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.model.MessageProperty;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.dictionary.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashSet;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class UserMessageService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageService.class);

    @Autowired
    UserMessageDao userMessageDao;

    @Autowired
    AgreementDictionaryService agreementDictionaryService;
    @Autowired
    UserMessageSampleUtil userMessageSampleUtil;
    @Autowired
    MpcDictionaryService mpcDictionaryService;
    @Autowired
    PartyRoleDictionaryService partyRoleDictionaryService;
    @Autowired
    PartyIdDictionaryService partyIdDictionaryService;
    @Autowired
    ServiceDictionaryService serviceDictionaryService;
    @Autowired
    ActionDictionaryService actionDictionaryService;
    @Autowired
    MessagePropertyDictionaryService messagePropertyDictionaryService;

    @Transactional
    public UserMessage getUserMessage(){
        final UserMessage userMessage;
        try {
            userMessage = userMessageSampleUtil.getUserMessageTemplate();
        } catch (IOException e) {
            LOG.error("Could not create the userMessage", e);
            return null;
        }
        userMessage.setAction(actionDictionaryService.findOrCreateAction(userMessage.getActionValue()));
        userMessage.setService(serviceDictionaryService.findOrCreateService(userMessage.getService().getValue(), userMessage.getService().getType()));
        userMessage.setAgreementRef(agreementDictionaryService.findOrCreateAgreement(userMessage.getAgreementRef().getValue(), userMessage.getAgreementRef().getType()));
        userMessage.setMpc(mpcDictionaryService.findOrCreateMpc(StringUtils.isBlank(userMessage.getMpcValue()) ? Ebms3Constants.DEFAULT_MPC : userMessage.getMpcValue()));
        userMessage.getPartyInfo().getTo().setToPartyId(partyIdDictionaryService.findOrCreateParty("toPartyValue", "toPartyType"));
        userMessage.getPartyInfo().getTo().setToRole(partyRoleDictionaryService.findOrCreateRole("toRole"));
        userMessage.getPartyInfo().getFrom().setFromPartyId(partyIdDictionaryService.findOrCreateParty("fromPartyValue", "fromPartyType"));
        userMessage.getPartyInfo().getFrom().setFromRole(partyRoleDictionaryService.findOrCreateRole("fromRole"));

        HashSet<MessageProperty> messageProperties = new HashSet<>();
        messageProperties.add(messagePropertyDictionaryService.findOrCreateMessageProperty("name",
                "value",
                "type"));
        userMessage.setMessageProperties(messageProperties);

        userMessageDao.merge(userMessage);
        return userMessage;
    }

}
