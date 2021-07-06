package eu.domibus.core.message;

import eu.domibus.api.datasource.AutoCloseFileDataSource;
import eu.domibus.api.model.From;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.PartyId;
import eu.domibus.api.model.PartyInfo;
import eu.domibus.api.model.Property;
import eu.domibus.api.model.To;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.api.usermessage.domain.*;
import eu.domibus.core.message.dictionary.*;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.activation.DataHandler;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RunWith(JMockit.class)
public class UserMessageDefaultFactoryTest {

    @Tested
    UserMessageDefaultFactory userMessageDefaultFactory;

    @Injectable
    protected PartPropertyDao partPropertyDao;

    @Injectable
    protected MessagePropertyDao messagePropertyDao;

    @Injectable
    protected PartyIdDao partyIdDao;

    @Injectable
    protected PartyRoleDao partyRoleDao;

    @Injectable
    protected AgreementDao agreementDao;

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void createUserMessageFragmentTest(@Mocked UserMessage sourceMessage,
                                              @Injectable MessageInfo messageInfo,
                                              @Injectable MessageGroupEntity messageGroupEntity,
                                              @Injectable MessageFragmentEntity messageFragmentEntity) {
        Long fragmentNumber = 1L;
        String fragmentFile = "fragmentFile";
        String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultFactory) {{
//            sourceMessage.setSplitAndJoin(true);
//            sourceMessage.getMessageInfo();
//            result = messageInfo;
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
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void cloneUserMessageFragmentTest(@Injectable UserMessage userMessageFragment,
                                             @Injectable MessageInfo messageInfo,
                                             @Injectable CollaborationInfo collaborationInfo,
                                             @Injectable PartyInfo partyInfo,
                                             @Injectable MessageProperties messageProperties) {
        String messageId = UUID.randomUUID().toString();

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
            result = messageProperties;
        }};

        userMessageDefaultFactory.cloneUserMessageFragment(userMessageFragment);

        new FullVerificationsInOrder(userMessageDefaultFactory) {{
//            userMessageDefaultFactory.createCollaborationInfo(withCapture());
//            times = 1;
//            userMessageDefaultFactory.createMessageInfo(withCapture(), messageId);
//            times = 1;
            userMessageDefaultFactory.createPartyInfo(partyInfo);
            times = 1;
//            userMessageDefaultFactory.createMessageProperties(messageProperties);
//            times = 1;
        }};

    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
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
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
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
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void createMessageInfoTest(@Injectable MessageInfo source) {
        String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultFactory) {{
            source.getTimestamp();
            result = any;
            source.getRefToMessageId();
            result = anyString;
        }};

//        Assert.assertNotNull(userMessageDefaultFactory.createMessageInfo(source, messageId));
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void createPartyInfoTest(@Injectable PartyInfo source,
                                    @Injectable From from,
                                    @Injectable PartyId party,
                                    @Injectable To to) {
        Set<PartyId> partyIds = new HashSet<>();
        partyIds.add(party);

        new Expectations(userMessageDefaultFactory) {{
            source.getFrom();
            result = from;
            from.getFromRole();
            result = anyString;
            source.getTo();
            result = to;
            to.getToRole();
            result = anyString;
        }};

        Assert.assertNotNull(userMessageDefaultFactory.createPartyInfo(source));
    }

}