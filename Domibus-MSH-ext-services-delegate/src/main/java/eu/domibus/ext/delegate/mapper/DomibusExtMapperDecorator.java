package eu.domibus.ext.delegate.mapper;


import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.monitoring.domain.ServiceInfo;
import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.ext.domain.monitoring.DataBaseInfoDTO;
import eu.domibus.ext.domain.monitoring.JmsBrokerInfoDTO;
import eu.domibus.ext.domain.monitoring.QuartzInfoDTO;
import eu.domibus.ext.domain.monitoring.ServiceInfoDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.mapstruct.DecoratedWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  DomibusExtMapperDecorator is for the abstract class and override the methods for JMS conversion.
 *
 * @author Joze Rihtarsic
 * @since 4.2
 */
public abstract class DomibusExtMapperDecorator implements DomibusExtMapper{

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MonitoringMapperDecorator.class);

    @Autowired
    @Qualifier("delegate")
    protected DomibusExtMapper delegate;


    @Override
    public JmsMessageDTO jmsMessageToJmsMessageDTO(JmsMessage jmsMessage) {
        if ( jmsMessage == null ) {
            return null;
        }
        JmsMessageDTO jmsMessageDTO = delegate.jmsMessageToJmsMessageDTO(jmsMessage);
        jmsMessageDTO.setProperties(convert(jmsMessage.getProperties()));
        return jmsMessageDTO;
    }

    @Override
    public JmsMessage jmsMessageDTOToJmsMessage(JmsMessageDTO jmsMessageDTO) {
        if ( jmsMessageDTO == null ) {
            return null;
        }

        JmsMessage jmsMessage = delegate.jmsMessageDTOToJmsMessage(jmsMessageDTO);
        jmsMessage.setProperties(convertDTO(jmsMessageDTO.getProperties()));
        return jmsMessage;
    }

    protected Map<String, String> convertDTO(Map<String, Object> properties) {

        LOG.debug("JmsMessageDTO convertDTO: [{}]", properties.getClass());
        Map<String,String> newProperties = properties.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue()));
        return newProperties;
    }
    protected Map<String, Object> convert(Map<String, String> properties) {

        LOG.debug("JmsMessage convert: [{}]", properties.getClass());
        Map<String,Object> newProperties = properties.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
        return newProperties;
    }
}
