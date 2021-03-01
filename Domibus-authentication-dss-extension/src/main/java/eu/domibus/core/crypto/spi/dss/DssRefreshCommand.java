package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.services.CommandExtTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.tsl.cache.CacheCleaner;
import eu.europa.esig.dss.tsl.job.TLValidationJob;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Thomas Dussart
 * @since 4.2
 *
 * This command triggers a refresh of the DSS trusted list on the node it is executed.
 */
public class DssRefreshCommand implements CommandExtTask {

    public static final String COMMAND_NAME="DSS_REFRESH";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DssRefreshCommand.class);

    private TLValidationJob domibusTSLValidationJob;

    private DssExtensionPropertyManager dssExtensionPropertyManager;

    private File cacheDirectory;

    private CacheCleaner cacheCleaner;

    public DssRefreshCommand(TLValidationJob domibusTSLValidationJob, DssExtensionPropertyManager dssExtensionPropertyManager, File cacheDirectory) {
        this.domibusTSLValidationJob = domibusTSLValidationJob;
        this.dssExtensionPropertyManager=dssExtensionPropertyManager;
        this.cacheDirectory=cacheDirectory;
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
        domibusTSLValidationJob.onlineRefresh();
        LOG.info("DSS trusted lists refreshed");
    }



    @PostConstruct
    public void init(){
        //TODO please refer to the following ticket EDELIVERY-7555 and refactor the code.
        LOG.info("Executing command to refresh DSS trusted lists at:[{}]", LocalDateTime.now());
        Path cachePath = cacheDirectory.toPath();
        String serverCacheDirectoryPath = cachePath.toString();
        if (!cachePath.toFile().exists()) {
            LOG.error("Dss cache directory[{}] should be created by the system, please check permissions", serverCacheDirectoryPath);
            return;
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(cachePath)) {
            Iterator files = ds.iterator();
            if (!files.hasNext()) {
                LOG.debug("Cache directory:[{}] is empty, refreshing trusted lists needed",serverCacheDirectoryPath);
                domibusTSLValidationJob.onlineRefresh();
            } else {
                //todo find how init
                LOG.debug("Cache directory:[{}] is not empty, loading trusted lists from disk",serverCacheDirectoryPath);
                domibusTSLValidationJob.offlineRefresh();
            }
        } catch (IOException e) {
            LOG.error("Error while checking if cache directory:[{}] is empty", serverCacheDirectoryPath, e);
        }
    }
}
