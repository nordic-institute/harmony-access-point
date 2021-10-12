package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.core.jms.MessageListenerContainerInitializer;
import eu.domibus.core.message.dictionary.StaticDictionaryService;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class DynamicDomainManagementServiceImpl implements DynamicDomainManagementService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDomainManagementServiceImpl.class);

    private List<Domain> originalDomains;

    @Autowired
    private DomainService domainService;

    @Autowired
    MessageListenerContainerInitializer messageListenerContainerInitializer;

    @Autowired
    EArchiveFileStorageProvider eArchiveFileStorageProvider;

    @Autowired
    StaticDictionaryService staticDictionaryService;

    @Autowired
    PayloadFileStorageProvider payloadFileStorageProvider;

    @PostConstruct
    public void init() {
        originalDomains = domainService.getDomains();
    }

    @Override
    public void handleDomainsChaned() {
        domainService.resetDomains();
        List<Domain> currentList = domainService.getDomains();
        List<Domain> addedDomains = currentList.stream()
                .filter(el -> !originalDomains.contains(el))
                .collect(Collectors.toList());

        if (addedDomains.isEmpty()) {
            return;
        }

        messageListenerContainerInitializer.domainsChanged(addedDomains, null);
        eArchiveFileStorageProvider.domainsChanged(addedDomains, null);
        staticDictionaryService.domainsChanged(addedDomains, null);
        payloadFileStorageProvider.domainsChanged(addedDomains, null);
    }

}
