package eu.domibus.ext.delegate.converter;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.domain.MessageAttemptDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author migueti, Cosmin Baciu
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
            return (T) domibusExtMapper.domainToDomainDTO((Domain) source);
        }
        if (typeOfT == Domain.class) {
            return (T) domibusExtMapper.domainDTOToDomain((DomainDTO) source);
        }
        if(typeOfT == MessageAttemptDTO.class) {
            return (T) domibusExtMapper.messageAttemptToMessageAttemptDTO((MessageAttempt)source);
        }
        if(typeOfT == MessageAttempt.class) {
            return (T) domibusExtMapper.messageAttemptDTOToMessageAttempt((MessageAttemptDTO)source);
        }
        if(typeOfT == MessageAcknowledgement.class) {
            return (T) domibusExtMapper.messageAcknowledgementDTOToMessageAcknowledgement((MessageAcknowledgementDTO) source);
        }
        if(typeOfT == MessageAcknowledgementDTO.class) {
            return (T) domibusExtMapper.messageAcknowledgementToMessageAcknowledgementDTO((MessageAcknowledgement) source);
        }

        LOG.warn("Type not converted: T=[{}] U=[{}]", typeOfT, source.getClass());

        return null;
    }

    @Override
    public <T, U> List<T> convert(List<U> sourceList, final Class<T> typeOfT) {
        if (sourceList == null) {
            return null;
        }
        List<T> result = new ArrayList<>();
        for (U sourceObject : sourceList) {
            result.add(convert(sourceObject, typeOfT));

        }
        return result;
    }

}
