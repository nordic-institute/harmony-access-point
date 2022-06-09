package eu.domibus.core.util;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class FileSystemUtil {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(FileSystemUtil.class);

    /**
     * It attempts to create the directory whenever is not present.
     * It works also when the location is a symbolic link.
     */
    public Path createLocation(String path) {
        Path payloadPath;
        try {
            payloadPath = Paths.get(path).normalize();
            if (!payloadPath.isAbsolute()) {
                throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Relative path [" + payloadPath + "] is forbidden. Please provide absolute path for payload storage");
            }
            // Checks if the path exists, if not it creates it
            if (Files.notExists(payloadPath)) {
                Files.createDirectories(payloadPath);
                LOG.info("The payload folder [{}] has been created!", payloadPath.toAbsolutePath());
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
