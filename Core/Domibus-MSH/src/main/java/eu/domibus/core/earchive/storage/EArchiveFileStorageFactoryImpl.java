package eu.domibus.core.earchive.storage;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.util.FileSystemUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchiveFileStorageFactoryImpl implements EArchiveFileStorageFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveFileStorageFactoryImpl.class);

    private final ObjectProvider<EArchiveFileStorage> eArchiveFileStorages;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final FileSystemUtil fileSystemUtil;

    public EArchiveFileStorageFactoryImpl(ObjectProvider<EArchiveFileStorage> eArchiveFileStorages,
                                          DomibusPropertyProvider domibusPropertyProvider,
                                          FileSystemUtil fileSystemUtil) {
        this.eArchiveFileStorages = eArchiveFileStorages;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.fileSystemUtil = fileSystemUtil;
    }

    @Override
    public EArchiveFileStorage create(Domain domain) {
        LOG.debug("Creating the eArchiveStorageFactory for domain [{}]", domain);
        return eArchiveFileStorages.getObject(domain, domibusPropertyProvider, fileSystemUtil);
    }
}
