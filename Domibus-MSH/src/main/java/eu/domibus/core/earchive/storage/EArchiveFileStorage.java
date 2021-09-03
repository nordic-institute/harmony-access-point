package eu.domibus.core.earchive.storage;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    private Domain domain;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

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

        Path path = createLocation(location);
        if (path == null) {
            throw new ConfigurationException("There was an error initializing the eArchiving folder but the earchiving is activated.");
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
        FileSystemManager fileSystemManager = getVFSManager();

        FileObject fileObject;
        try {
            try {
                fileObject = fileSystemManager.resolveFile(path);
            } catch (FileSystemException e) {
                throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, e.getMessage(), e);
            }
            // Checks if the path exists, if not it creates it
            if (!fileObject.exists()) {
                fileObject.createFolder();
                LOG.info("The eArchiving folder [{}] has been created!", fileObject.getPath().toAbsolutePath());
            } else {
                if (fileObject.isSymbolicLink()) {
                    fileObject = fileSystemManager.resolveFile(Files.readSymbolicLink(fileObject.getPath()).toAbsolutePath().toString());
                }

                if (!fileObject.isWriteable()) {
                    throw new IOException("Write permission for eArchiving folder " + fileObject.getPath().toAbsolutePath() + " is not granted.");
                }
            }
        } catch (IOException ioEx) {
            LOG.error("Error creating/accessing the eArchiving folder [{}]", path, ioEx);

            // Takes temporary folder by default if it faces any issue while creating defined path.
            try {
                fileObject = fileSystemManager.resolveFile(System.getProperty("java.io.tmpdir"));
            } catch (FileSystemException e) {
                throw new IllegalStateException("Could not create the eArchiving location [" + path + "]");
            }
            LOG.warn(WarningUtil.warnOutput("The temporary eArchiving folder " + fileObject.getPath().toAbsolutePath() + " has been selected!"));
        }
        return fileObject.getPath();
    }

    private FileSystemManager getVFSManager() {
        try {
            return VFS.getManager();
        } catch (FileSystemException e) {
            throw new IllegalStateException("VFS Manager could not be created");
        }
    }

}
