package eu.domibus.core.converter;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.process.Process;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.core.crypto.spi.DomainSpi;
import eu.domibus.core.message.attempt.MessageAttemptEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.routing.BackendFilterEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class DomainCoreDefaultConverter implements DomainCoreConverter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCoreDefaultConverter.class);

    @Autowired
    DomibusCoreMapper domibusCoreMapper;

    @Override
    public <T, U> T convert(U source, final Class<T> typeOfT) {
        if(typeOfT == Process.class) {
            return (T)domibusCoreMapper.processToProcessAPI((eu.domibus.common.model.configuration.Process)source);
        }

        if(typeOfT == eu.domibus.common.model.configuration.Process.class) {
            return (T)domibusCoreMapper.processAPIToProcess((Process)source);
        }

        if(typeOfT == Domain.class) {
            return (T)domibusCoreMapper.domainSpiToDomain((DomainSpi) source);
        }
        if(typeOfT == DomainSpi.class) {
            return (T)domibusCoreMapper.domainToDomainSpi((Domain)source);
        }

        if(typeOfT == BackendFilter.class) {
            return (T)domibusCoreMapper.backendFilterEntityToBackendFilter((BackendFilterEntity) source);
        }
        if(typeOfT == BackendFilterEntity.class) {
            return (T)domibusCoreMapper.backendFilterToBackendFilterEntity((BackendFilter) source);
        }

        if(typeOfT == MessageAttempt.class) {
            return (T)domibusCoreMapper.messageAttemptEntityToMessageAttempt((MessageAttemptEntity) source);
        }
        if(typeOfT == MessageAttemptEntity.class) {
            return (T)domibusCoreMapper.messageAttemptToMessageAttemptEntity((MessageAttempt) source);
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
