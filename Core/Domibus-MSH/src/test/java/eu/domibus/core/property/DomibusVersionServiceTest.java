package eu.domibus.core.property;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Federico Martini
 */
@RunWith(JMockit.class)
public class DomibusVersionServiceTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusVersionServiceTest.class);

    @Tested
    DomibusVersionService service;

    @Test
    public void testDisplayVersion() throws Exception {

        DomibusVersionService service = new DomibusVersionService();

        assertEquals("harmony-MSH", service.getArtifactName());
        assertNotEquals("", service.getBuiltTime());
        assertNotEquals("", service.getArtifactVersion());

        LOG.info(service.getDisplayVersion());
    }

    @Test
    public void testVersionNumber(@Mocked Properties versionProps) throws Exception {

        new Expectations() {{
            versionProps.getProperty("Artifact-Version");
            returns("4.1-RC1", "4.0.2");
        }};

        String version = service.getVersionNumber();
        assertEquals("4.1", version);

        String version2 = service.getVersionNumber();
        assertEquals("4.0.2", version2);
    }

    @Test
    public void testGetBuildDetails() {

        String artifactName = "domibus-MSH";

        new Expectations(service) {{
            service.getArtifactName();
            result = artifactName;

        }};

        String buildDetails = service.getBuildDetails();

        assertTrue(buildDetails.contains(artifactName));
    }

    @Test
    public void getBuiltTime(@Mocked Properties versionProps) {
        Locale.setDefault(Locale.ENGLISH);
        new Expectations() {{
            versionProps.getProperty("Build-Time");
            result = "2021-02-18 09:47";
        }};

        String time = service.getBuiltTime();

        assertEquals("2021-02-18 09:47|Coordinated Universal Time", time);
    }
}
