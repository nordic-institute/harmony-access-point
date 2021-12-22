package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.model.MessageProperty;
import eu.domibus.api.model.Messaging;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Property;
import eu.domibus.common.model.configuration.PropertySet;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.UserMessageDefaultServiceHelper;
import eu.domibus.core.message.UserMessageServiceHelper;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author idragusa
 * @since 4.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class PropertyProfileValidatorTest {

    public static final String valid4CornerMessagePath = "target/test-classes/eu/domibus/common/validators/valid4CornerMessage.xml";

    @Tested
    PropertyProfileValidator propertyProfileValidator;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    UserMessageServiceHelper userMessageDefaultServiceHelper = new UserMessageDefaultServiceHelper();

    private final LegConfiguration legConfiguration = new LegConfiguration();

    private final PropertySet propertySet = new PropertySet();


    @Test
    @Ignore("EDELIVERY-8776 MessageProperty ClassCast exception")
    public void validateTest(@Injectable UserMessage userMessage,
                             @Injectable eu.domibus.api.model.Property property1,
                             @Injectable eu.domibus.api.model.Property property2) throws EbMS3Exception, FileNotFoundException, XMLStreamException, JAXBException, ParserConfigurationException, SAXException {
        Set<eu.domibus.api.model.Property> messageProperties = new HashSet<>();
        messageProperties.add(property1);
        messageProperties.add(property2);

        Set<eu.domibus.api.model.MessageProperty> properties = new HashSet<>();
        properties.add(createMessageProperty(MessageConstants.ORIGINAL_SENDER));
        properties.add(createMessageProperty(MessageConstants.FINAL_RECIPIENT));
        new NonStrictExpectations(legConfiguration, propertySet) {{
            property1.getName();
            result = MessageConstants.ORIGINAL_SENDER;
            property1.getType();
            result = "String";

            property2.getName();
            result = MessageConstants.FINAL_RECIPIENT;
            property2.getType();
            result = "String";

            domibusConfigurationService.isFourCornerEnabled();
            result = true;

            legConfiguration.getPropertySet();
            result = propertySet;

            propertySet.getProperties();
            result = properties;

            userMessage.getMessageProperties();
            result = messageProperties;
        }};

        propertyProfileValidator.validate(userMessage, "anyKey");
    }


    @Test
    public void validateMissingPropertyTest(@Injectable UserMessage userMessage,
                                            @Injectable LegConfiguration legConfiguration,
                                            @Injectable PropertySet propertySet) throws EbMS3Exception {
        String pmodeKey = "anyKey";

        new NonStrictExpectations() {{
            pModeProvider.getLegConfiguration(pmodeKey);
            result = legConfiguration;

            legConfiguration.getPropertySet();
            result = propertySet;
        }};

        propertyProfileValidator.validate(userMessage, pmodeKey);
    }


    @Test
    public void checkDuplicateMessagePropertiesTest(@Injectable Messaging messaging,
                                                    @Injectable Property profiledProperty,
                                                    @Injectable eu.domibus.api.model.MessageProperty messageProperty,
                                                    @Injectable eu.domibus.api.model.MessageProperty messageProperty1) {
        Set<Property> properties = new HashSet<>();
        final List<Property> modifiablePropertyList = new ArrayList<>();
        properties.add(createProperty(MessageConstants.ORIGINAL_SENDER, MessageConstants.ORIGINAL_SENDER, "String", true));
        String duplicateMessageProperty = "originalSender";
        Set<eu.domibus.api.model.MessageProperty> messagePropertiesSet = new HashSet<>();
        messagePropertiesSet.add(messageProperty);
        messagePropertiesSet.add(messageProperty1);
        modifiablePropertyList.addAll(properties);

        new Expectations() {{
            messageProperty.getName();
            result = duplicateMessageProperty;

            messageProperty1.getName();
            result = duplicateMessageProperty;
        }};

        try {
            propertyProfileValidator.checkDuplicateMessageProperties(modifiablePropertyList, messagePropertiesSet);
            Assert.fail();
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0052, e.getErrorCode());
            Assert.assertEquals("Duplicate Message property found for property name [originalSender]", e.getMessage());
        }

    }

    private MessageProperty createMessageProperty(String name) {
        MessageProperty property = new MessageProperty();
        property.setName(name);

        return property;
    }

    private Property createProperty(String name, String key, String dataType, boolean required) {
        Property property = new Property();
        property.setName(name);
        property.setRequired(required);
        property.setKey(key);
        property.setDatatype(dataType);

        return property;
    }

}
