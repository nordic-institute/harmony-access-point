package eu.domibus.core.earchive.storage;

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

        Path path = getPath(location);

        storageDirectory = path.toFile();
        LOG.info("Initialized eArchiving folder on path [{}] for domain [{}]", path, this.domain);
    }

    private Path getPath(String location) {
        Path path;
        try {
            path = createLocation(location);
        } catch (FileSystemException e) {
            throw new ConfigurationException("There was an error initializing the eArchiving folder but the earchiving is activated.", e);
        }
        if (path == null) {
            throw new ConfigurationException("There was an error initializing the eArchiving folder but the earchiving is activated.");
        }
        return path;
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
    protected Path createLocation(String path) throws FileSystemException {
        FileSystemManager fileSystemManager = getVFSManager();

        try (FileObject fileObject = fileSystemManager.resolveFile(path)) {
            if (!fileObject.exists()) {
                fileObject.createFolder();
                LOG.info("The eArchiving folder [{}] has been created!", fileObject.getPath().toAbsolutePath());
            }
            if (fileObject.isSymbolicLink()) {
                try (FileObject f1 = fileSystemManager.resolveFile(Files.readSymbolicLink(fileObject.getPath()).toAbsolutePath().toString())) {
                    return returnWritablePath(f1);
                }
            }
            return returnWritablePath(fileObject);
        } catch (IOException ioEx) {
            return getTemporaryPath(path, fileSystemManager, ioEx);
        }
    }

    private Path getTemporaryPath(String path, FileSystemManager fileSystemManager, IOException ioEx) throws FileSystemException {
        LOG.error("Error creating/accessing the eArchiving folder [{}]", path, ioEx);
        try (FileObject fo = fileSystemManager.resolveFile(System.getProperty("java.io.tmpdir"))) {
            LOG.warn(WarningUtil.warnOutput("The temporary eArchiving folder " + fo.getPath().toAbsolutePath() + " has been selected!"));
            return fo.getPath();
        }
    }

    private Path returnWritablePath(FileObject fileObject) throws IOException {
        if (!fileObject.isWriteable()) {
            throw new IOException("Write permission for eArchiving folder " + fileObject.getPath().toAbsolutePath() + " is not granted.");
        }
        return fileObject.getPath();
    }

    private void close(FileObject fileObject) throws FileSystemException {
        if (fileObject != null) {
            fileObject.close();
        }
    }

    private FileSystemManager getVFSManager() {
        try {
            return VFS.getManager();
        } catch (FileSystemException e) {
            throw new IllegalStateException("VFS Manager could not be created");
        }
    }

}
