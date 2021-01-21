package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.core.property.PropertyConfig;
import eu.domibus.test.dao.InMemoryDataBaseConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class, MessageConfig.class, PropertyConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
@Transactional
public class PropertyDaoIT {

    @Autowired
    private PropertyDao propertyDao;

    @Autowired
    private MessageInfoDao messageInfoDao;

    @PersistenceContext(unitName = "domibusJTA")
    protected EntityManager em;
    private String msgId;

    @Before
    public void setUp() {
        msgId = randomUUID().toString();
        createUserMessageWithProperties(Arrays.asList(
                getProperty("prop1", "value1"),
                getProperty("prop2", "value2")),
                getMessageInfo(msgId));
    }

    @Test
    public void findUserMessageByGroupId() {
        MessagePropertiesDto userMessageByGroupId = propertyDao.findMessagePropertiesForMessageId(msgId);
        assertEquals(2, userMessageByGroupId.getProperties().size());
        assertEquals("value1", userMessageByGroupId.getProperties().get("prop1"));
        assertEquals("value2", userMessageByGroupId.getProperties().get("prop2"));
    }

    private void createUserMessageWithProperties(List<Property> properties, MessageInfo messageInfo) {
        UserMessage userMessage = new UserMessage();
        userMessage.setMessageInfo(messageInfo);
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        collaborationInfo.setConversationId(randomUUID().toString());
        userMessage.setCollaborationInfo(collaborationInfo);
        if (properties != null) {
            MessageProperties value = new MessageProperties();
            value.getProperty().addAll(properties);
            userMessage.setMessageProperties(value);
        }
        em.persist(userMessage);
    }

    private Property getProperty(String name, String value) {
        Property property = new Property();
        property.setName(name);
        property.setType("type");
        property.setValue(value);
        return property;
    }

    private MessageInfo getMessageInfo(String msgId) {
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageId(msgId);
        messageInfo.setTimestamp(new Date());
        messageInfoDao.create(messageInfo);
        return messageInfo;
    }
}