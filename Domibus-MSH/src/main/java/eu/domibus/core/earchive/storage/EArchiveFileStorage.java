package eu.domibus.core.earchive.storage;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EArchiveFileStorage {

    public static final String E_ARCHIVE_STORAGE_LOCATION = DOMIBUS_EARCHIVE_STORAGE_LOCATION;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveFileStorage.class);

    private File storageDirectory = null;

    private Domain domain;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    public EArchiveFileStorage(Domain domain) {
        this.domain = domain;
    }

    @PostConstruct
    public void init() {

        final String location = domibusPropertyProvider.getProperty(this.domain, E_ARCHIVE_STORAGE_LOCATION);
        if (StringUtils.isBlank(location)) {
            LOG.warn("No file system storage defined. This is fine for small attachments but might lead to database issues when processing large eArchivings");
            return;
        }

        Path path = createLocation(location);
        if (path == null) {
            LOG.warn("There was an error initializing the eArchiving folder, so Domibus will be using the database");
            return;
        }

        storageDirectory = path.toFile();
        LOG.info("Initialized eArchiving folder on path [{}] for domain [{}]", path, this.domain);
    }

    public File getStorageDirectory() {
        return storageDirectory;
    }

    public void reset() {
        storageDirectory = null;
        init();
    }

    /**
     * It attempts to create the directory whenever is not present.
     * It works also when the location is a symbolic link.
     */
    protected Path createLocation(String path) {
        Path eArchivePath;
        try {
            eArchivePath = Paths.get(path).normalize();
            if (!eArchivePath.isAbsolute()) {
                throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Relative path [" + eArchivePath + "] is forbidden. Please provide absolute path for eArchiving storage");
            }
            // Checks if the path exists, if not it creates it
            if (Files.notExists(eArchivePath)) {
                Files.createDirectories(eArchivePath);
                LOG.info("The eArchiving folder [{}] has been created!", eArchivePath.toAbsolutePath());
            } else {
                if (Files.isSymbolicLink(eArchivePath)) {
                    eArchivePath = Files.readSymbolicLink(eArchivePath);
                }

                if (!Files.isWritable(eArchivePath)) {
                    throw new IOException("Write permission for eArchiving folder " + eArchivePath.toAbsolutePath() + " is not granted.");
                }
            }
        } catch (IOException ioEx) {
            LOG.error("Error creating/accessing the eArchiving folder [{}]", path, ioEx);

            // Takes temporary folder by default if it faces any issue while creating defined path.
            eArchivePath = Paths.get(System.getProperty("java.io.tmpdir"));
            LOG.warn(WarningUtil.warnOutput("The temporary eArchiving folder " + eArchivePath.toAbsolutePath() + " has been selected!"));
        }
        return eArchivePath;
    }

}
