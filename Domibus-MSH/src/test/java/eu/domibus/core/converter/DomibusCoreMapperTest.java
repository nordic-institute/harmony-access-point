package eu.domibus.core.converter;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.core.clustering.CommandEntity;
import eu.domibus.core.crypto.spi.DomainSpi;
import eu.domibus.core.logging.LoggingEntry;
import eu.domibus.core.plugin.routing.BackendFilterEntity;
import eu.domibus.core.plugin.routing.RoutingCriteriaEntity;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.PasswordEncryptionResultDTO;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.rest.ro.LoggingLevelRO;
import eu.domibus.web.rest.ro.MessageFilterRO;
import eu.domibus.web.rest.ro.PModeResponseRO;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * IT test for DomibusCoreMapper, AuditMapper and EventMapper
 *
 * @author Ioana Dragusanu
 * @author Catalin Enache
 * @since 5.0
 */
public class DomibusCoreMapperTest extends AbstractMapperTest {

    @Autowired
    private DomibusCoreMapper domibusCoreMapper;

    @Autowired
    private ObjectService objectService;
    
    @Test
    public void convertDomain() {
        DomainSpi toConvert = (DomainSpi) objectService.createInstance(DomainSpi.class);
        final Domain converted = domibusCoreMapper.domainSpiToDomain(toConvert);
        final DomainSpi convertedBack = domibusCoreMapper.domainToDomainSpi(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertBackendFilter() {
        BackendFilter toConvert = (BackendFilter) objectService.createInstance(BackendFilter.class);
        final MessageFilterRO converted = domibusCoreMapper.backendFilterToMessageFilterRO(toConvert);
        final BackendFilter convertedBack = domibusCoreMapper.messageFilterROToBackendFilter(converted);
        convertedBack.setActive(true);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertBackendFilterEntity() {
        BackendFilter toConvert = (BackendFilter) objectService.createInstance(BackendFilter.class);
        final BackendFilterEntity converted = domibusCoreMapper.backendFilterToBackendFilterEntity(toConvert);
        final BackendFilter convertedBack = domibusCoreMapper.backendFilterEntityToBackendFilter(converted);
        convertedBack.setActive(true);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertRoutingCriteria() {
        RoutingCriteria toConvert = (RoutingCriteria) objectService.createInstance(RoutingCriteria.class);
        final RoutingCriteriaEntity converted = domibusCoreMapper.routingCriteriaToRoutingCriteriaEntity(toConvert);
        final RoutingCriteria convertedBack = domibusCoreMapper.routingCriteriaEntityToRoutingCriteria(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }


    @Test
    public void convertEvent() {
        PModeResponseRO toConvert = (PModeResponseRO) objectService.createInstance(PModeResponseRO.class);
        final PModeArchiveInfo converted = domibusCoreMapper.pModeResponseROToPModeArchiveInfo(toConvert);
        final PModeResponseRO convertedBack = domibusCoreMapper.pModeArchiveInfoToPModeResponseRO(converted);
        convertedBack.setCurrent(true);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertDomainRO() {
        DomainRO toConvert = (DomainRO) objectService.createInstance(DomainRO.class);
        final Domain converted = domibusCoreMapper.domainROToDomain(toConvert);
        final DomainRO convertedBack = domibusCoreMapper.domainToDomainRO(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertDomainToDomainDTO() {
        Domain toConvert = (Domain) objectService.createInstance(Domain.class);
        final DomainDTO converted = domibusCoreMapper.domainToDomainDTO(toConvert);
        final Domain convertedBack = domibusCoreMapper.domainDTOToDomain(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertDomainDTOToDomain() {
        DomainDTO toConvert = (DomainDTO) objectService.createInstance(DomainDTO.class);
        final Domain converted = domibusCoreMapper.domainDTOToDomain(toConvert);
        final DomainDTO convertedBack = domibusCoreMapper.domainToDomainDTO(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }
    @Test
    public void convertPasswordEncryptionResultDTOToPasswordEncryptionResult() {
        PasswordEncryptionResultDTO toConvert = (PasswordEncryptionResultDTO) objectService.createInstance(PasswordEncryptionResultDTO.class);
        final PasswordEncryptionResult converted = domibusCoreMapper.passwordEncryptionResultDTOToPasswordEncryptionResult(toConvert);
        final PasswordEncryptionResultDTO convertedBack = domibusCoreMapper.passwordEncryptionResultToPasswordEncryptionResultDTO(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertCommand() {
        Command toConvert = (Command) objectService.createInstance(Command.class);
        final CommandEntity converted = domibusCoreMapper.commandToCommandEntity(toConvert);
        final Command convertedBack = domibusCoreMapper.commandEntityToCommand(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertLoggingEntry() {
        LoggingEntry toConvert = (LoggingEntry) objectService.createInstance(LoggingEntry.class);
        final LoggingLevelRO converted = domibusCoreMapper.loggingEntryToLoggingLevelRO(toConvert);
        final LoggingEntry convertedBack = domibusCoreMapper.loggingLevelROToLoggingEntry(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

}