package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.MessageProperties;
import eu.domibus.api.model.Messaging;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Property;
import eu.domibus.common.model.configuration.PropertySet;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.UserMessageDefaultServiceHelper;
import eu.domibus.core.message.UserMessageServiceHelper;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.property.DomibusPropertyProviderImpl;
import eu.domibus.core.util.xml.XMLUtilImpl;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author idragusa
 * @since 4.0
 */
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

    @Mock
    private DomibusPropertyProviderImpl domibusPropertyProvider;

    private LegConfiguration legConfiguration = new LegConfiguration();

    private PropertySet propertySet = new PropertySet();


    @Test
    public void validateTest(@Injectable Messaging messaging,
                             @Injectable eu.domibus.api.model.Property property1,
                             @Injectable eu.domibus.api.model.Property property2) throws EbMS3Exception, FileNotFoundException, XMLStreamException, JAXBException, ParserConfigurationException, SAXException {
        Set<eu.domibus.api.model.Property> messageProperties = new HashSet<>();
        messageProperties.add(property1);
        messageProperties.add(property2);

        Set<Property> properties = new HashSet<>();
        properties.add(createProperty(MessageConstants.ORIGINAL_SENDER, MessageConstants.ORIGINAL_SENDER, "String", true));
        properties.add(createProperty(MessageConstants.FINAL_RECIPIENT, MessageConstants.FINAL_RECIPIENT, "String", true));
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

            messaging.getUserMessage().getMessageProperties().getProperty();
            result = messageProperties;
        }};

        propertyProfileValidator.validate(messaging, "anyKey");
    }


    @Test(expected = EbMS3Exception.class)
    public void validateMissingPropertyTest(@Injectable Messaging messaging) throws EbMS3Exception, FileNotFoundException, XMLStreamException, JAXBException, ParserConfigurationException, SAXException {
        Set<Property> properties = new HashSet<>();
        properties.add(createProperty(MessageConstants.ORIGINAL_SENDER, MessageConstants.ORIGINAL_SENDER, "String", true));
        new NonStrictExpectations(legConfiguration, propertySet) {{
            domibusConfigurationService.isFourCornerEnabled();
            result = true;

            legConfiguration.getPropertySet();
            result = propertySet;

            propertySet.getProperties();
            result = properties;
        }};

        propertyProfileValidator.validate(messaging, "anyKey");
    }

    @Test
    public void checkDuplicateMessagePropertiesTest(@Injectable Messaging messaging,
                                                    @Injectable MessageProperties messageProperties,
                                                    @Injectable Property profiledProperty,
                                                    @Injectable eu.domibus.api.model.Property messageProperty,
                                                    @Injectable eu.domibus.api.model.Property messageProperty1) throws EbMS3Exception, FileNotFoundException, XMLStreamException, JAXBException, ParserConfigurationException, SAXException {
        Set<Property> properties = new HashSet<>();
        final List<Property> modifiablePropertyList = new ArrayList<>();
        properties.add(createProperty(MessageConstants.ORIGINAL_SENDER, MessageConstants.ORIGINAL_SENDER, "String", true));
        Set<eu.domibus.api.model.Property> messagePropertiesSet = new HashSet<>();
        String duplicateMessageProperty = "originalSender";
        messagePropertiesSet.add(messageProperty);
        messagePropertiesSet.add(messageProperty1);
        modifiablePropertyList.addAll(properties);

        new Expectations() {{
            messageProperty.getName();
            result = duplicateMessageProperty;

            messageProperty1.getName();
            result = duplicateMessageProperty;

            messageProperties.getProperty();
            result = messagePropertiesSet;
        }};

        try {
            propertyProfileValidator.checkDuplicateMessageProperties(modifiablePropertyList, messageProperties);
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0052, e.getErrorCode());
            Assert.assertEquals("Duplicate Message property found for property name [originalSender]", e.getMessage());
        }
        new Verifications() {{
            messageProperties.getProperty().stream().filter(string -> string.getName().equalsIgnoreCase(profiledProperty.getKey())).count();
            times = 1;
        }};
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
