package eu.domibus.core.crypto.spi.dss;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DssRefreshCommandTest {

   /* @Test
    public void canHandleTrue() {
        Assert.assertTrue(new DssRefreshCommand(null,null).canHandle(DssRefreshCommand.COMMAND_NAME));
    }

    @Test
    public void canHandleFalse() {
        Assert.assertFalse(new DssRefreshCommand(null,null).canHandle("test"));
    }

    @Test
    public void executeRefresh(@Mocked DomibusTSLValidationJob domibusTSLValidationJob, @Mocked DssExtensionPropertyManager dssExtensionPropertyManager) {
        new Expectations(){{
            dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.DSS_FULL_TLS_REFRESH);
            result="false";
        }};

        DssRefreshCommand dssRefreshCommand = new DssRefreshCommand(domibusTSLValidationJob, dssExtensionPropertyManager);
        dssRefreshCommand.execute(new HashMap<>());
        new Verifications(){{
            domibusTSLValidationJob.clearRepository();times=0;
            domibusTSLValidationJob.refresh();times=1;
        }};
    }

    @Test
    public void executeFullRefresh(@Mocked DomibusTSLValidationJob domibusTSLValidationJob, @Mocked DssExtensionPropertyManager dssExtensionPropertyManager){
        new Expectations(){{
            dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.DSS_FULL_TLS_REFRESH);
            result="true";
        }};
        DssRefreshCommand dssRefreshCommand = new DssRefreshCommand(domibusTSLValidationJob, dssExtensionPropertyManager);
        dssRefreshCommand.execute(new HashMap<>());
        new Verifications(){{
            domibusTSLValidationJob.clearRepository();times=1;
            domibusTSLValidationJob.refresh();times=1;
        }};
    }

    @Test
    public void initWithInvalidCacheDirectoryPath(@Mocked DomibusTSLValidationJob domibusTSLValidationJob, @Mocked DssExtensionPropertyManager dssExtensionPropertyManager) {
        new Expectations(){{
            domibusTSLValidationJob.getCacheDirectoryPath();
            result="this directory does not exists";
        }};
        DssRefreshCommand dssRefreshCommand = new DssRefreshCommand(domibusTSLValidationJob, dssExtensionPropertyManager);
        dssRefreshCommand.init();
        new Verifications(){{
            domibusTSLValidationJob.refresh();times=0;
            domibusTSLValidationJob.initRepository();times=0;
        }};
    }*/


   /* @Test @TODO weird jmockit behavior here, fix later.
    public void initWithCacheDirectoryEmpty(@Mocked DomibusTSLValidationJob domibusTSLValidationJob, @Mocked DssExtensionPropertyManager dssExtensionPropertyManager,@Mocked Path path) throws IOException {
        new Expectations(){{
            String cachePath = "This directory exists";
            domibusTSLValidationJob.getCacheDirectoryPath();
            this.result = cachePath;
            Paths.get(cachePath);
            this.result=path;
            path.toFile().exists();
            result=true;
            Files.newDirectoryStream(path).iterator().hasNext();
            result=true;
        }};
        DssRefreshCommand dssRefreshCommand = new DssRefreshCommand(domibusTSLValidationJob, dssExtensionPropertyManager);
        dssRefreshCommand.init();
        new Verifications(){{
            domibusTSLValidationJob.refresh();times=0;
            domibusTSLValidationJob.initRepository();times=0;
        }};
    }*/
}