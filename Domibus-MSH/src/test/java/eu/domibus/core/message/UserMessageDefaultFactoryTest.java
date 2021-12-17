package eu.domibus.core.message;

import eu.domibus.api.datasource.AutoCloseFileDataSource;
import eu.domibus.api.model.*;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.api.usermessage.domain.CollaborationInfo;
import eu.domibus.api.usermessage.domain.MessageInfo;
import eu.domibus.api.usermessage.domain.PartProperties;
import eu.domibus.api.usermessage.domain.PayloadInfo;
import eu.domibus.core.message.dictionary.*;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.activation.DataHandler;
import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class UserMessageDefaultFactoryTest {

    @Tested
    UserMessageDefaultFactory userMessageDefaultFactory;

    @Injectable
    protected PartPropertyDictionaryService partPropertyDictionaryService;
    @Injectable
    protected MessagePropertyDictionaryService messagePropertyDictionaryService;
    @Injectable
    protected PartyIdDictionaryService partyIdDictionaryService;
    @Injectable
    protected PartyRoleDictionaryService partyRoleDictionaryService;
    @Injectable
    protected AgreementDictionaryService agreementDictionaryService;
    @Injectable
    protected ServiceDictionaryService serviceDictionaryService;
    @Injectable
    protected ActionDictionaryService actionDictionaryService;

    @Test
    public void createUserMessageFragmentTest(@Injectable UserMessage sourceMessage,
                                              @Injectable MessageInfo messageInfo,
                                              @Injectable MessageGroupEntity messageGroupEntity,
                                              @Injectable MessageFragmentEntity messageFragmentEntity,
                                              @Injectable ActionEntity actionEntity) {
        Long fragmentNumber = 1L;
        String fragmentFile = "fragmentFile";
        String messageId = UUID.randomUUID().toString();

        new Expectations() {{
            messageInfo.getMessageId();
            result = messageId;

            sourceMessage.getMessageId();
            result = "messageId";

            sourceMessage.getRefToMessageId();
            result = "refToMessageId";

            sourceMessage.getTimestamp();
            result = new Date();

            sourceMessage.getActionValue();
            result = "action";

            sourceMessage.getConversationId();
            result = "conversationId";

            sourceMessage.getAgreementRef();
            result = "agreementRef";

            actionDictionaryService.findOrCreateAction("action");
            result = actionEntity;

            sourceMessage.getService().getValue();
            result = "service";

            sourceMessage.getService().getType();
            result = "serviceType";

            serviceDictionaryService.findOrCreateService("service", "serviceType");

        }};

        userMessageDefaultFactory.createUserMessageFragment(sourceMessage, messageGroupEntity, fragmentNumber, fragmentFile);

        new FullVerifications() {};
    }

    @Test
    public void cloneUserMessageFragmentTest(@Injectable UserMessage userMessageFragment,
                                             @Injectable MessageInfo messageInfo,
                                             @Injectable CollaborationInfo collaborationInfo,
                                             @Injectable PartyInfo partyInfo,
                                             @Injectable MessageProperty messageProperty) {
        String messageId = UUID.randomUUID().toString();
        HashSet<MessageProperty> msgProperties = new HashSet<>();
        msgProperties.add(messageProperty);

        new Expectations(userMessageDefaultFactory) {{
//            userMessageFragment.getCollaborationInfo();
//            result = collaborationInfo;
//            userMessageFragment.getMessageInfo();
//            result = messageInfo;
//            messageInfo.getMessageId();
//            result = messageId;
            userMessageFragment.getPartyInfo();
            result = partyInfo;
            userMessageFragment.getMessageProperties();
            result = msgProperties;
        }};

        userMessageDefaultFactory.cloneUserMessageFragment(userMessageFragment);

        new Verifications() {{
//            userMessageDefaultFactory.createCollaborationInfo(withCapture());
//            times = 1;
//            userMessageDefaultFactory.createMessageInfo(withCapture(), messageId);
//            times = 1;
//            userMessageDefaultFactory.createMessageProperties(messageProperties);
//            times = 1;
        }};

    }

    @Test
    public void createMessageFragmentEntityTest(@Injectable MessageGroupEntity messageGroupEntity) {
        Long fragmentNumber = 1L;
        String groupId = "groupId";

        Assert.assertNotNull(userMessageDefaultFactory.createMessageFragmentEntity(messageGroupEntity, fragmentNumber));
    }

    @Test
    public void createPayloadInfoTest(@Injectable PayloadInfo payloadInfo,
                                      @Injectable PartInfo partInfo,
                                      @Injectable DataHandler dataHandler,
                                      @Injectable AutoCloseFileDataSource autoCloseFileDataSource,
                                      @Mocked File file,
                                      @Injectable PartProperties partProperties,
                                      @Injectable Property property) {
        Long fragmentNumber = 1L;
        String fragmentFile = "fragmentFile";

        new Expectations(userMessageDefaultFactory) {{
            new File(fragmentFile).length();
            result = 2L;
        }};

        Assert.assertNotNull(userMessageDefaultFactory.createMessageFragmentPartInfo(fragmentFile, fragmentNumber));
    }

    @Test
    public void createPartyInfoTest(@Injectable PartyInfo source,
                                    @Injectable From from,
                                    @Injectable PartyId party,
                                    @Injectable To to,
                                    @Injectable PartyRole partyRole) {

        new Expectations(userMessageDefaultFactory) {{
            source.getFrom();
            result = from;

            from.getRoleValue();
            result = "FromRole";

            partyRoleDictionaryService.findOrCreateRole("FromRole");
            result = partyRole;

            from.getFromPartyId();
            result = party;

            party.getValue();
            result = "partyValue";

            party.getType();
            result = "PartyType";

            partyIdDictionaryService.findOrCreateParty("partyValue", "PartyType");
            result = party;

            source.getTo();
            result = to;

            to.getRoleValue();
            result = "ToRole";

            partyRoleDictionaryService.findOrCreateRole("ToRole");
            result = partyRole;

            to.getToPartyId();
            result = party;

            partyIdDictionaryService.findOrCreateParty("partyValue", "PartyType");
            result = party;

        }};

        Assert.assertNotNull(userMessageDefaultFactory.createPartyInfo(source));
    }

}