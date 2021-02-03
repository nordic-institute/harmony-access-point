package eu.domibus.plugin.ws.webservice.deprecated.mapper;

import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class MessagingMapperTest {

    public static final String SUBMIT_MESSAGE_MESSAGING_XML = "submitMessage_messaging.xml";

    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @Configuration
    static public class MapperConfig {

        @Bean
        public WSPluginMessagingMapper MessagingMapper() {
            return new WSPluginMessagingMapperImpl();
        }

        @Bean
        public WSPluginUserMessageMapper UserMessageMapper() {
            return new WSPluginUserMessageMapperImpl();
        }

    }

    @Autowired
    private WSPluginMessagingMapperImpl messagingMapper;

    @Test
    public void testConversion() throws JAXBException {
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream(SUBMIT_MESSAGE_MESSAGING_XML);
        JAXBContext jaxbContext = JAXBContext.newInstance(eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging.class.getPackage().getName());

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging messagingFromFile =
                ((JAXBElement<eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging>) unmarshaller.unmarshal(xmlStream)).getValue();


        Messaging messaging = messagingMapper.messagingToEntity(messagingFromFile);

        Assert.assertNotNull(messaging.getUserMessage());

    }


}