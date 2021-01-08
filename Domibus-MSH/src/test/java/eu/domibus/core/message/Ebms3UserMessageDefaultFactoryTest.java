package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.api.datasource.AutoCloseFileDataSource;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.activation.DataHandler;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RunWith(JMockit.class)
public class Ebms3UserMessageDefaultFactoryTest {

    @Tested
    UserMessageDefaultFactory userMessageDefaultFactory;

    @Test
    public void createUserMessageFragmentTest(@Mocked UserMessage sourceMessage,
                                              @Injectable MessageInfo messageInfo,
                                              @Injectable MessageGroupEntity messageGroupEntity,
                                              @Injectable MessageFragmentEntity messageFragmentEntity) {
        Long fragmentNumber = 1L;
        String fragmentFile = "fragmentFile";
        String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultFactory) {{
            sourceMessage.setSplitAndJoin(true);
            sourceMessage.getMessageInfo();
            result = messageInfo;
            messageInfo.getMessageId();
            result = messageId;

        }};

        userMessageDefaultFactory.createUserMessageFragment(sourceMessage, messageGroupEntity, fragmentNumber, fragmentFile);

        new Verifications() {{
            userMessageDefaultFactory.createMessageFragmentEntity(messageGroupEntity, fragmentNumber);
            times = 1;
        }};
    }

    @Test
    public void cloneUserMessageFragmentTest(@Injectable UserMessage userMessageFragment,
                                             @Injectable MessageInfo messageInfo,
                                             @Injectable CollaborationInfo collaborationInfo,
                                             @Injectable PartyInfo partyInfo,
                                             @Injectable MessageProperties messageProperties) {
        String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultFactory) {{
            userMessageFragment.getCollaborationInfo();
            result = collaborationInfo;
            userMessageFragment.getMessageInfo();
            result = messageInfo;
            messageInfo.getMessageId();
            result = messageId;
            userMessageFragment.getPartyInfo();
            result = partyInfo;
            userMessageFragment.getMessageProperties();
            result = messageProperties;
        }};

        userMessageDefaultFactory.cloneUserMessageFragment(userMessageFragment);

        new FullVerificationsInOrder(userMessageDefaultFactory) {{
            userMessageDefaultFactory.createCollaborationInfo(withCapture());
            times = 1;
            userMessageDefaultFactory.createMessageInfo(withCapture(), messageId);
            times = 1;
            userMessageDefaultFactory.createPartyInfo(partyInfo);
            times = 1;
            userMessageDefaultFactory.createMessageProperties(messageProperties);
            times = 1;
        }};

    }

    @Test
    public void createMessageFragmentEntityTest(@Injectable MessageGroupEntity messageGroupEntity) {
        Long fragmentNumber = 1L;
        String groupId = "groupId";

        new Expectations(userMessageDefaultFactory) {{
            messageGroupEntity.getGroupId();
            result = groupId;
        }};

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

        Assert.assertNotNull(userMessageDefaultFactory.createPayloadInfo(fragmentFile, fragmentNumber));
    }

    @Test
    public void createCollaborationInfoTest(@Injectable CollaborationInfo source,
                                            @Injectable AgreementRef agreementRef,
                                            @Injectable CollaborationInfo collaborationInfo,
                                            @Injectable Service service) {
        String action = "action";
        String conversationId = "conversationId";

        new Expectations(userMessageDefaultFactory) {{
            source.getConversationId();
            result = conversationId;
            source.getAction();
            result = action;
            source.getAgreementRef();
            result = agreementRef;
            agreementRef.getValue();
            result = anyString;
            agreementRef.getType();
            result = anyString;
            source.getService();
            result = service;
            service.getValue();
            result = anyString;
            service.getType();
            result = anyString;
        }};

        Assert.assertNotNull(userMessageDefaultFactory.createCollaborationInfo(source));
    }

    @Test
    public void createMessageInfoTest(@Injectable MessageInfo source) {
        String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultFactory) {{
            source.getTimestamp();
            result = any;
            source.getRefToMessageId();
            result = anyString;
        }};

        Assert.assertNotNull(userMessageDefaultFactory.createMessageInfo(source, messageId));
    }

    @Test
    public void createPartyInfoTest(@Injectable PartyInfo source,
                                    @Injectable From from,
                                    @Injectable PartyId party,
                                    @Injectable To to) {
        Set<PartyId> partyIds = new HashSet<>();
        partyIds.add(party);

        new Expectations(userMessageDefaultFactory) {{
            source.getFrom();
            result = from;
            from.getRole();
            result = anyString;
            source.getTo();
            result = to;
            to.getRole();
            result = anyString;
        }};

        Assert.assertNotNull(userMessageDefaultFactory.createPartyInfo(source));
    }

    @Test
    public void createMessagePropertiesTest(@Injectable MessageProperties source,
                                            @Injectable Property sourceProperty) {
        String allowed_properties = "originalSender";

        new Expectations(userMessageDefaultFactory) {{
            source.getProperty();
            result = sourceProperty;
            sourceProperty.getName();
            result = allowed_properties;
        }};

        Assert.assertNotNull(userMessageDefaultFactory.createMessageProperties(source));
    }
}