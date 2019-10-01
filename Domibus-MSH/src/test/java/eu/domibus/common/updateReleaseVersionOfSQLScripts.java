package eu.domibus.common;

import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This test is to check that the db scripts files has the same version of Domibus Artifact Version.
 *
 * @author Soumya Chandran
 * @since 4.1.2
 */
@RunWith(JUnit4.class)
public class updateReleaseVersionOfSQLScripts {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CheckReleaseSQLScriptsGenerationIT.class);

    private static final String SQL_SCRIPTS_DIRECTORY_PATH = "../Domibus-MSH-db/src/main/resources/db/scripts";


    /**
     * This  test fetches the current artifact version of Domibus and verifies that the
     * db.scripts directory has the correct version scripts for the current release.
     *
     * @throws IOException
     */
    @Test
    public void checkPresenceOfSQLScriptDDLsForRelease() throws IOException {

        String domibusArtifactVersion = retrieveDomibusArtifactVersion();
        File sqlScriptsDirectory = locateDomibusSqlScriptsDirectory();
        checkPresenceOfFile(domibusArtifactVersion, sqlScriptsDirectory);
    }


    protected void preVerifications(String domibusArtifactVersion, File sqlScriptsDirectory) {
        Assert.assertNotNull("Domibus Artefact Version should be initialized from properties file!", domibusArtifactVersion);
        Assert.assertNotNull(sqlScriptsDirectory);
        Assert.assertTrue("target/sql-scripts directory should be present!", sqlScriptsDirectory.isDirectory());
    }

    protected boolean checkPresenceOfFile(String artifactVersion, File sqlScriptsDirectory) throws IOException {
        boolean fileNameVersionFlag = false;
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(sqlScriptsDirectory.toPath());
        String domibusArtifactVersionNoSnapshot = StringUtils.stripEnd(artifactVersion, "-SNAPSHOT");
        LOG.debug("DomibusArtifactVersion Without Snapshot:---[{}]", domibusArtifactVersionNoSnapshot);
        File dir = new File(String.valueOf(sqlScriptsDirectory.toPath()));

        if (dir.isDirectory()) { // make sure it's a directory
            for (final File f : dir.listFiles()) {
                String fileName = f.getName();
                if (!StringUtils.contains(fileName, domibusArtifactVersionNoSnapshot)) {
                    fileNameVersionFlag = false;
                    LOG.info("Script Versions is not Matching with Domibus Artifact Version:- [{}] ", f.getAbsolutePath());
                    Assert.assertTrue("Scripts Versions are not matching with Domibus Artifact Version", fileNameVersionFlag);
                } else {
                    fileNameVersionFlag = true;
                    Assert.assertTrue("Scripts Versions are matching with Domibus Artifact Version", fileNameVersionFlag);
                }
            }
        }
        return fileNameVersionFlag;
    }

    protected File locateDomibusSqlScriptsDirectory() {
        /*Verify that SQL resources/db/scripts Directory is exists*/
        File sqlScriptsDirectory = new File(SQL_SCRIPTS_DIRECTORY_PATH);
        LOG.debug("sqlScriptsDirectory.getAbsolutePath: [{}]", sqlScriptsDirectory.getAbsolutePath());
        LOG.debug("sqlScriptsDirectory.exists: [{}]", sqlScriptsDirectory.exists());

        Assert.assertTrue("Check if Directory exists", sqlScriptsDirectory.exists());
        Assert.assertTrue("Check if db/scripts is a directory", sqlScriptsDirectory.isDirectory());
        return sqlScriptsDirectory;
    }


    protected String retrieveDomibusArtifactVersion() {
        /*During Maven compile phase the domibus.properties file in the target folder with the artifact version copied from the POM*/
        DomibusPropertiesService domibusPropertiesService = new DomibusPropertiesService();
        String domibusArtifactVersion = domibusPropertiesService.getArtifactVersion();
        if (StringUtils.isBlank(domibusArtifactVersion)) {
            LOG.error("Domibus artefact version could not be loaded!!!");
            Assert.fail("Domibus artefact version could not be loaded!!!");
        }
        LOG.debug("Artefact Version loaded from the domibus.properties: [{}]", domibusArtifactVersion);

        return domibusArtifactVersion;
    }
}
