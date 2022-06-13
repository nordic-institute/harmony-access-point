package eu.domibus.ext.delegate.mapper;


import eu.domibus.api.jms.JmsMessage;
import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;
import java.util.stream.Collectors;

/**
 *  DomibusExtMapperDecorator is for the abstract class and override the methods for JMS conversion.
 *
 * @author Joze Rihtarsic
 * @since 4.2
 */
public abstract class DomibusExtMapperDecorator implements DomibusExtMapper{

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusExtMapperDecorator.class);

    @Autowired
    @Qualifier("delegate")
    protected DomibusExtMapper delegate;


    @Override
    public JmsMessageDTO jmsMessageToJmsMessageDTO(JmsMessage jmsMessage) {
        if ( jmsMessage == null ) {
            LOG.trace("Convert 'null' JmsMessage parameter to 'null' JmsMessageDTO!");
            return null;
        }
        JmsMessageDTO jmsMessageDTO = delegate.jmsMessageToJmsMessageDTO(jmsMessage);
        jmsMessageDTO.setProperties(convert(jmsMessage.getProperties()));
        return jmsMessageDTO;
    }

    @Override
    public JmsMessage jmsMessageDTOToJmsMessage(JmsMessageDTO jmsMessageDTO) {
        if ( jmsMessageDTO == null ) {
            LOG.trace("Convert 'null' JmsMessageDTO parameter to 'null' JmsMessage!");
            return null;
        }

        JmsMessage jmsMessage = delegate.jmsMessageDTOToJmsMessage(jmsMessageDTO);
        jmsMessage.setProperties(convertDTO(jmsMessageDTO.getProperties()));
        return jmsMessage;
    }

    protected Map<String, String> convertDTO(Map<String, Object> properties) {

        LOG.debug("JmsMessageDTO convertDTO: [{}]", properties.getClass());
        return properties.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
    }

    protected Map<String, Object> convert(Map<String, String> properties) {

        LOG.debug("JmsMessage convert: [{}]", properties.getClass());
        return properties.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
