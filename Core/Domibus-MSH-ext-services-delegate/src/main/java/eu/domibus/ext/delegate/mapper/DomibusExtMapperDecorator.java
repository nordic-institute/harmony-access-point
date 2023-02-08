package eu.domibus.ext.delegate.mapper;


import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.ext.domain.TrustStoreDTO;
import eu.domibus.ext.domain.TrustStoreEntryDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DomibusExtMapperDecorator is for the abstract class and override the methods for JMS conversion.
 *
 * @author Joze Rihtarsic
 * @since 4.2
 */
public abstract class DomibusExtMapperDecorator implements DomibusExtMapper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusExtMapperDecorator.class);

    @Autowired
    @Qualifier("delegate")
    protected DomibusExtMapper delegate;


    @Override
    public JmsMessageDTO jmsMessageToJmsMessageDTO(JmsMessage jmsMessage) {
        if (jmsMessage == null) {
            LOG.trace("Convert 'null' JmsMessage parameter to 'null' JmsMessageDTO!");
            return null;
        }
        JmsMessageDTO jmsMessageDTO = delegate.jmsMessageToJmsMessageDTO(jmsMessage);
        jmsMessageDTO.setProperties(convert(jmsMessage.getProperties()));
        return jmsMessageDTO;
    }

    @Override
    public JmsMessage jmsMessageDTOToJmsMessage(JmsMessageDTO jmsMessageDTO) {
        if (jmsMessageDTO == null) {
            LOG.trace("Convert 'null' JmsMessageDTO parameter to 'null' JmsMessage!");
            return null;
        }

        JmsMessage jmsMessage = delegate.jmsMessageDTOToJmsMessage(jmsMessageDTO);
        jmsMessage.setProperties(convertDTO(jmsMessageDTO.getProperties()));
        return jmsMessage;
    }

    @Override
    public List<TrustStoreEntryDTO> trustStoreEntriesToTrustStoresEntriesDTO(List<TrustStoreEntry> trustStoreEntries) {
        if (trustStoreEntries == null) {
            LOG.trace("Convert 'null' trustStoreEntries parameter to 'null' TrustStoreEntryDTO!");
            return null;
        }
        List<TrustStoreEntryDTO> list = delegate.trustStoreEntriesToTrustStoresEntriesDTO(trustStoreEntries);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setValidFrom(simpleDateFormat.format(trustStoreEntries.get(i).getValidFrom()));
            list.get(i).setValidUntil(simpleDateFormat.format(trustStoreEntries.get(i).getValidUntil()));
        }
        return list;
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
