package eu.domibus.core.payload.persistence.filesystem;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ATTACHMENT_STORAGE_LOCATION;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ATTACHMENT_TEMP_STORAGE_LOCATION;

/**
 * @version 2.0
 * @author Ioana Dragusanu
 * @author Martini Federico
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PayloadFileStorage {

    public static final String ATTACHMENT_STORAGE_LOCATION = DOMIBUS_ATTACHMENT_STORAGE_LOCATION;
    public static final String TEMPORARY_ATTACHMENT_STORAGE_LOCATION = DOMIBUS_ATTACHMENT_TEMP_STORAGE_LOCATION;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadFileStorage.class);

    private File storageDirectory = null;

    private Domain domain;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    public PayloadFileStorage() {
        storageDirectory = null;
    }

    public PayloadFileStorage(File storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    public File getStorageDirectory() {
        return storageDirectory;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Domain getDomain() {
        return domain;
    }

    @PostConstruct
    public void initFileSystemStorage() {
        final String location = domibusPropertyProvider.getProperty(this.domain, ATTACHMENT_STORAGE_LOCATION);
        if (location != null && !location.isEmpty()) {
            if (storageDirectory == null) {
                Path path = createLocation(location);
                if (path != null) {
                    storageDirectory = path.toFile();
                    LOG.info("Initialized payload folder on path [{}] for domain [{}]", path, this.domain);
                } else {
                    LOG.warn("There was an error initializing the payload folder, so Domibus will be using the database");
                }
            }
        } else {
            LOG.warn("No file system storage defined. This is fine for small attachments but might lead to database issues when processing large payloads");
            storageDirectory = null;
        }
    }

    /**
     * It attempts to create the directory whenever is not present.
     * It works also when the location is a symbolic link.
     *
     * @param path
     * @return Path
     */
    protected Path createLocation(String path) {
        Path payloadPath = null;
        try {
            payloadPath = Paths.get(path).normalize();
            if (!payloadPath.isAbsolute()) {
                throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Relative path [" + payloadPath + "] is forbidden. Please provide absolute path for payload storage");
            }
            // Checks if the path exists, if not it creates it
            if (Files.notExists(payloadPath)) {
                Files.createDirectories(payloadPath);
                LOG.info("The payload folder " + payloadPath.toAbsolutePath() + " has been created!");
            } else {
                if (Files.isSymbolicLink(payloadPath)) {
                    payloadPath = Files.readSymbolicLink(payloadPath);
                }

                if (!Files.isWritable(payloadPath)) {
                    throw new IOException("Write permission for payload folder " + payloadPath.toAbsolutePath() + " is not granted.");
                }
            }
        } catch (IOException ioEx) {
            LOG.error("Error creating/accessing the payload folder [{}]", path, ioEx);

            // Takes temporary folder by default if it faces any issue while creating defined path.
            payloadPath = Paths.get(System.getProperty("java.io.tmpdir"));
            LOG.warn(WarningUtil.warnOutput("The temporary payload folder " + payloadPath.toAbsolutePath() + " has been selected!"));
        }
        return payloadPath;
    }

}
