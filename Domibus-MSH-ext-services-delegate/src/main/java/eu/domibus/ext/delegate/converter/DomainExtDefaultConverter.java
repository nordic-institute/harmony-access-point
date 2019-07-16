package eu.domibus.ext.delegate.converter;

import eu.domibus.api.converter.ConverterException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.usermessage.domain.UserMessage;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author migueti, Cosmin Baciu, idragusa
 * @since 3.3
 */
@Component
public class DomainExtDefaultConverter implements DomainExtConverter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainExtDefaultConverter.class);

    @Autowired
    DomibusExtMapper domibusExtMapper;

    @Override
    public <T, U> T convert(U source, final Class<T> typeOfT) {
        if (typeOfT == DomainDTO.class) {
            LOG.debug("Ext type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusExtMapper.domainToDomainDTO((Domain) source);
        }
        if (typeOfT == Domain.class) {
            LOG.debug("Ext type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusExtMapper.domainDTOToDomain((DomainDTO) source);
        }
        if (typeOfT == MessageAttemptDTO.class) {
            LOG.debug("Ext type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusExtMapper.messageAttemptToMessageAttemptDTO((MessageAttempt) source);
        }
        if (typeOfT == MessageAttempt.class) {
            LOG.debug("Ext type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusExtMapper.messageAttemptDTOToMessageAttempt((MessageAttemptDTO) source);
        }
        if (typeOfT == MessageAcknowledgement.class) {
            LOG.debug("Ext type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusExtMapper.messageAcknowledgementDTOToMessageAcknowledgement((MessageAcknowledgementDTO) source);
        }
        if (typeOfT == MessageAcknowledgementDTO.class) {
            LOG.debug("Ext type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusExtMapper.messageAcknowledgementToMessageAcknowledgementDTO((MessageAcknowledgement) source);
        }

        if (typeOfT == JmsMessage.class) {
            LOG.debug("Ext type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusExtMapper.jmsMessageDTOToJmsMessage((JmsMessageDTO) source);
        }
        if (typeOfT == JmsMessageDTO.class) {
            LOG.debug("Ext type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusExtMapper.jmsMessageToJmsMessageDTO((JmsMessage) source);
        }

        if (typeOfT == UserMessage.class) {
            LOG.debug("Ext type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusExtMapper.userMessageDTOToUserMessage((UserMessageDTO) source);
        }
        if (typeOfT == UserMessageDTO.class) {
            LOG.debug("Ext type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusExtMapper.userMessageToUserMessageDTO((UserMessage) source);
        }
        String errorMsg = String.format("Ext type not converted: T=[{}] U=[{}]", typeOfT, source.getClass());
        LOG.error(errorMsg);
        throw new ConverterException(DomibusCoreErrorCode.DOM_008, errorMsg);

    }

    @Override
    public <T, U> List<T> convert(List<U> sourceList, final Class<T> typeOfT) {
        LOG.debug("Ext convert list to T=[{} ", typeOfT);
        if (sourceList == null) {
            LOG.debug("Ext sourceList is null for T=[{}", typeOfT);
            return null;
        }
        List<T> result = new ArrayList<>();
        for (U sourceObject : sourceList) {
            result.add(convert(sourceObject, typeOfT));

        }
        return result;
    }

}
