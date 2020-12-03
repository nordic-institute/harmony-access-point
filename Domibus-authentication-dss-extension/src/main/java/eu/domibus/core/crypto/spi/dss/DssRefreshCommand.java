package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.services.CommandExtTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.tsl.service.DomibusTSLValidationJob;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
public class DssRefreshCommand implements CommandExtTask {

    public static final String COMMAND_NAME="DSS_REFRESH";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DssRefreshCommand.class);

    private DomibusTSLValidationJob domibusTSLValidationJob;

    private DssExtensionPropertyManager dssExtensionPropertyManager;

    public DssRefreshCommand(DomibusTSLValidationJob domibusTSLValidationJob,DssExtensionPropertyManager dssExtensionPropertyManager) {
        this.domibusTSLValidationJob = domibusTSLValidationJob;
        this.dssExtensionPropertyManager=dssExtensionPropertyManager;
    }

    @Override
    public boolean canHandle(String command) {
        boolean candHandle = COMMAND_NAME.equals(command);
        LOG.debug("Command with name:[{}] should be executed[{}]",command,candHandle);
        return candHandle;
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOG.info("Start DSS trusted lists refresh command");
        if (Boolean.parseBoolean(dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.DSS_FULL_TLS_REFRESH))) {
            domibusTSLValidationJob.clearRepository();
            LOG.info("DSS trusted lists cleared");
        }
        domibusTSLValidationJob.refresh();
        LOG.info("DSS trusted lists refreshed");
    }

    @PostConstruct
    public void init(){
        LOG.info("Executing command to refresh DSS trusted lists at:[{}]", LocalDateTime.now());
        String serverCacheDirectoryPath = domibusTSLValidationJob.getCacheDirectoryPath();
        Path cachePath = Paths.get(serverCacheDirectoryPath);
        if (!cachePath.toFile().exists()) {
            LOG.error("Dss cache directory[{}] should be created by the system, please check permissions", serverCacheDirectoryPath);
            return;
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(cachePath)) {
            Iterator files = ds.iterator();
            if (!files.hasNext()) {
                LOG.debug("Cache directory:[{}] is empty, refreshing trusted lists needed",serverCacheDirectoryPath);
                domibusTSLValidationJob.refresh();
            } else {
                LOG.debug("Cache directory:[{}] is not empty, loading trusted lists from disk",serverCacheDirectoryPath);
                domibusTSLValidationJob.initRepository();
            }
        } catch (IOException e) {
            LOG.error("Error while checking if cache directory:[{}] is empty", serverCacheDirectoryPath, e);
        }
    }
}
