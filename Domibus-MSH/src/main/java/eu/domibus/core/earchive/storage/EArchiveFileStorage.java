package eu.domibus.core.earchive.storage;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.util.FileSystemUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STORAGE_LOCATION;

/**
 * @author François Gautier
 * @since 5.0
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EArchiveFileStorage {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveFileStorage.class);

    private File storageDirectory = null;

    private Object storageDirectoryLock = new Object();

    private Domain domain;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;
    @Autowired
    protected FileSystemUtil fileSystemUtil;

    public EArchiveFileStorage(Domain domain) {
        this.domain = domain;
    }

    @PostConstruct
    public void init() {

        final String eArchiveActive = domibusPropertyProvider.getProperty(this.domain, DOMIBUS_EARCHIVE_ACTIVE);
        if (BooleanUtils.isNotTrue(BooleanUtils.toBooleanObject(eArchiveActive))) {
            return;
        }
        final String location = domibusPropertyProvider.getProperty(this.domain, DOMIBUS_EARCHIVE_STORAGE_LOCATION);
        if (StringUtils.isBlank(location)) {
            throw new ConfigurationException("No file system storage defined for earchiving but the earchiving is activated.");
        }

        Path path = getPath(location);

        storageDirectory = path.toFile();
        LOG.info("Initialized eArchiving folder on path [{}] for domain [{}]", path, this.domain);
    }

    private Path getPath(String location) {
        Path path = fileSystemUtil.createLocation(location);

        if (path == null) {
            throw new ConfigurationException("There was an error initializing the eArchiving folder but the earchiving is activated.");
        }
        return path;
    }

    public File getStorageDirectory() {
        if (storageDirectory == null) {
            synchronized (storageDirectoryLock) {
                if (storageDirectory == null) {
                    init();
                }
            }
        }
        return storageDirectory;
    }

    public void reset() {
        storageDirectory = null;
        init();
    }

}
