package eu.domibus.ext.delegate.converter;

import eu.domibus.api.converter.ConverterException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.monitoring.domain.MonitoringInfo;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.party.Party;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.process.Process;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.usermessage.domain.UserMessage;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.delegate.mapper.MonitoringMapper;
import eu.domibus.ext.domain.*;
import eu.domibus.ext.domain.monitoring.MonitoringInfoDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author migueti, Cosmin Baciu, idragusa, azhikso
 * @since 3.3
 */
@Component
public class DomainExtDefaultConverter implements DomainExtConverter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainExtDefaultConverter.class);

    @Autowired
    DomibusExtMapper domibusExtMapper;

    @Autowired
    MonitoringMapper monitoringMapper;

    @Override
    public <T, U> T convert(U source, final Class<T> typeOfT) {
        final String debugMessage = "Ext type converted: T=[{}] U=[{}]";
        if (typeOfT == DomainDTO.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.domainToDomainDTO((Domain) source);
        }
        if (typeOfT == Domain.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.domainDTOToDomain((DomainDTO) source);
        }
        if (typeOfT == MessageAttemptDTO.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.messageAttemptToMessageAttemptDTO((MessageAttempt) source);
        }
        if (typeOfT == MessageAttempt.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.messageAttemptDTOToMessageAttempt((MessageAttemptDTO) source);
        }
        if (typeOfT == MessageAcknowledgement.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.messageAcknowledgementDTOToMessageAcknowledgement((MessageAcknowledgementDTO) source);
        }
        if (typeOfT == MessageAcknowledgementDTO.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.messageAcknowledgementToMessageAcknowledgementDTO((MessageAcknowledgement) source);
        }

        if (typeOfT == JmsMessage.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.jmsMessageDTOToJmsMessage((JmsMessageDTO) source);
        }
        if (typeOfT == JmsMessageDTO.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.jmsMessageToJmsMessageDTO((JmsMessage) source);
        }

        if (typeOfT == UserMessage.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.userMessageDTOToUserMessage((UserMessageDTO) source);
        }
        if (typeOfT == UserMessageDTO.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.userMessageToUserMessageDTO((UserMessage) source);
        }
        if (typeOfT == PModeArchiveInfoDTO.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.pModeArchiveInfoToPModeArchiveInfoDto((PModeArchiveInfo) source);
        }
        if (typeOfT == PasswordEncryptionResultDTO.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.passwordEncryptionResultToPasswordEncryptionResultDTO((PasswordEncryptionResult) source);
        }
        if (typeOfT == DomibusPropertyMetadata.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.domibusPropertyMetadataDTOToDomibusPropertyMetadata((DomibusPropertyMetadataDTO) source);
        }
        if (typeOfT == DomibusPropertyMetadataDTO.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.domibusPropertyMetadataToDomibusPropertyMetadataDTO((DomibusPropertyMetadata) source);
        }
        if (typeOfT == MonitoringInfo.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) monitoringMapper.monitoringInfoDTOToMonitoringInfo((MonitoringInfoDTO) source);
        }
        if (typeOfT == MonitoringInfoDTO.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) monitoringMapper.monitoringInfoToMonitoringInfoDTO((MonitoringInfo) source);
        }
        if (typeOfT == ValidationIssueDTO.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.pModeIssueToPModeIssueDTO((ValidationIssue) source);
        }

        if (typeOfT == PartyDTO.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.partyToPartyDTO((Party) source);
        }

        if (typeOfT == Party.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.partyDTOToParty((PartyDTO) source);
        }

        if (typeOfT == TrustStoreDTO.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.trustStoreEntryToTrustStoreDTO((TrustStoreEntry) source);
        }

        if (typeOfT == TrustStoreEntry.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.trustStoreDTOToTrustStoreEntry((TrustStoreDTO) source);
        }

        if (typeOfT == ProcessDTO.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.processToProcessDTO((Process) source);
        }

        if (typeOfT == Process.class) {
            LOG.debug(debugMessage, typeOfT, source.getClass());
            return (T) domibusExtMapper.processDTOToProcess((ProcessDTO) source);
        }

        String errorMsg = String.format("Ext type not converted: T=[%s] U=[%s]", typeOfT, source.getClass());
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

    @Override
    public <T, U> Map<String, T> convert(Map<String, U> source, Class<T> typeOfT) {
        LOG.debug("Ext convert map to T=[{} ", typeOfT);
        if (source == null) {
            LOG.debug("Ext source map is null for T=[{}", typeOfT);
            return null;
        }
        Map<String, T> result = new HashMap<>();
        for (Map.Entry<String, U> src : source.entrySet()) {
            result.put(src.getKey(), convert(src.getValue(), typeOfT));
        }
        return result;
    }

}
