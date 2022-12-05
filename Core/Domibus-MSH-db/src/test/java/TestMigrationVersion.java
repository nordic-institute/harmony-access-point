import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class TestMigrationVersion {

    public static final String TARGET_SQL_SCRIPTS = "target" + File.separator + "sql-scripts";
    public static final String INTO_TB_VERSION = "INSERT INTO TB_VERSION";
    private static File sqlScriptsFolder;

    @BeforeClass
    public static void setup() {
        sqlScriptsFolder = Paths.get(TARGET_SQL_SCRIPTS).toFile();
    }

    @Test
    public void test() {
        String[] migrationFiles = sqlScriptsFolder.list((dir, fileName) -> fileName.contains("-migration"));
        for (String fileName : migrationFiles) {
            assertHasCorrectVersion(fileName);
        }
    }

    private void assertHasCorrectVersion(String fileName) {
        try (Stream<String> stream = Files.lines(Paths.get(TARGET_SQL_SCRIPTS + File.separator + fileName))) {
            Optional<String> lineHavingVersion = stream.filter(l -> l.contains(INTO_TB_VERSION))
                    .findFirst();
            if (!lineHavingVersion.isPresent()) {
                return;
            }
            String targetVersion = getTargetVersion(fileName);
            if (targetVersion != null) {
                assertTrue(String.format("File %s should contain '%s' with version %s. Check the property DomibusVersion is set correctly in the pom.xml", fileName, INTO_TB_VERSION, targetVersion),
                        lineHavingVersion.get().contains(targetVersion));
            }
        } catch (IOException e) {
            fail("Failure reading file " + fileName + ": " + e.getMessage());
        }
    }

    private String getTargetVersion(String fileName) {
        Pattern pattern = Pattern.compile(".*-to-(\\d\\.\\d(\\.\\d)?(-SNAPSHOT)?)-.*migration\\.ddl");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        pattern = Pattern.compile("(mysql|oracle)-(\\d\\.\\d(\\.\\d)?(-SNAPSHOT)?).*\\.ddl");
        matcher = pattern.matcher(fileName);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return null;
    }

    @Test
    public void testGetTargetVersionFirstRegex() {
        assertEquals("5.0", getTargetVersion("mysql-4.2.9-to-5.0-migration.ddl"));
        assertEquals("5.0", getTargetVersion("mysql-4.2.9-to-5.0-multi-tenancy-migration.ddl"));
        assertEquals("5.0.1", getTargetVersion("mysql-5.0-to-5.0.1-migration.ddl"));
        assertEquals("5.0.2", getTargetVersion("mysql-5.0.1-to-5.0.2-migration.ddl"));
        assertEquals("5.0", getTargetVersion("oracle-4.2.9-to-5.0-migration.ddl"));
        assertEquals("5.0", getTargetVersion("oracle-4.2.9-to-5.0-multi-tenancy-migration.ddl"));
        assertEquals("5.0.1", getTargetVersion("oracle-5.0-to-5.0.1-migration.ddl"));
        assertEquals("5.0.2", getTargetVersion("oracle-5.0.1-to-5.0.2-migration.ddl"));
    }

    @Test
    public void testGetTargetVersionSecondRegex() {
        assertEquals("5.1-SNAPSHOT", getTargetVersion("mysql-5.1-SNAPSHOT-data.ddl"));
        assertEquals("5.1-SNAPSHOT", getTargetVersion("mysql-5.1-SNAPSHOT-multi-tenancy-data.ddl"));
        assertEquals("5.1-SNAPSHOT", getTargetVersion("mysql-5.1-SNAPSHOT-multi-tenancy.ddl"));
        assertEquals("5.1-SNAPSHOT", getTargetVersion("mysql-5.1-SNAPSHOT.ddl"));
        assertEquals("5.1-SNAPSHOT", getTargetVersion("oracle-5.1-SNAPSHOT-data.ddl"));
        assertEquals("5.1-SNAPSHOT", getTargetVersion("oracle-5.1-SNAPSHOT-multi-tenancy-data.ddl"));
        assertEquals("5.1-SNAPSHOT", getTargetVersion("oracle-5.1-SNAPSHOT-multi-tenancy.ddl"));
        assertEquals("5.1-SNAPSHOT", getTargetVersion("oracle-5.1-SNAPSHOT.ddl"));
    }
}
