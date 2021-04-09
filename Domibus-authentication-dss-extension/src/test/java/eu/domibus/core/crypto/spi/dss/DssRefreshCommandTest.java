package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.tsl.job.TLValidationJob;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DssRefreshCommandTest {

    @Test
    public void canHandleTrue() {
        Assert.assertTrue(new DssRefreshCommand(null, null).canHandle(DssRefreshCommand.COMMAND_NAME));
    }

    @Test
    public void canHandleFalse() {
        Assert.assertFalse(new DssRefreshCommand(null, null).canHandle("test"));
    }

    @Test
    public void executeRefresh(@Mocked TLValidationJob tlValidationJob) {
        DssRefreshCommand dssRefreshCommand = new DssRefreshCommand(tlValidationJob, null);
        dssRefreshCommand.execute(new HashMap<>());
        new Verifications(){{
            tlValidationJob.onlineRefresh();times=1;
        }};
    }



    @Test
    public void initWithNonExistingDirectory(@Mocked TLValidationJob tlValidationJob, @Mocked File cacheDirectory,@Mocked LocalDateTime localDateTime) {
        new Expectations(){{
            localDateTime.now();
            result=null;
            cacheDirectory.toPath().toFile().exists();
            result=false;
        }};
        DssRefreshCommand dssRefreshCommand = new DssRefreshCommand(tlValidationJob, cacheDirectory);
        dssRefreshCommand.loadTrustedList();
        new Verifications(){{
            tlValidationJob.onlineRefresh();times=0;
            tlValidationJob.offlineRefresh();times=0;
        }};
    }


    @Test
    public void initWithCacheDirectoryEmpty(@Mocked TLValidationJob tlValidationJob, @Mocked File cacheDirectory, @Mocked Path path, @Mocked LocalDateTime localDateTime, @Mocked Files files, @Mocked Iterator iterator) throws  IOException {
        new Expectations(){{
            localDateTime.now();
            result=null;
            cacheDirectory.toPath();
            result=path;
            path.toFile().exists();
            result=true;
            files.newDirectoryStream(path).iterator();
            result=iterator;
            iterator.hasNext();
            result=false;
        }};
        DssRefreshCommand dssRefreshCommand = new DssRefreshCommand(tlValidationJob, cacheDirectory);
        dssRefreshCommand.loadTrustedList();
        new Verifications(){{
            tlValidationJob.onlineRefresh();times=1;
        }};
    }

    @Test
    public void initWithCacheDirectoryNotEmpty(@Mocked TLValidationJob tlValidationJob, @Mocked File cacheDirectory, @Mocked Path path, @Mocked LocalDateTime localDateTime, @Mocked Files files, @Mocked Iterator iterator) throws  IOException {
        new Expectations(){{
            localDateTime.now();
            result=null;
            cacheDirectory.toPath();
            result=path;
            path.toFile().exists();
            result=true;
            files.newDirectoryStream(path).iterator();
            result=iterator;
            iterator.hasNext();
            result=true;
        }};
        DssRefreshCommand dssRefreshCommand = new DssRefreshCommand(tlValidationJob, cacheDirectory);
        dssRefreshCommand.loadTrustedList();
        new Verifications(){{
            tlValidationJob.offlineRefresh();times=1;
        }};
    }
}