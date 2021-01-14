package eu.domibus.core.property;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Federico Martini
 */
@RunWith(JMockit.class)
public class DomibusPropertiesServiceTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertiesServiceTest.class);

    @Tested
    DomibusVersionService service;

    @Test
    public void testDisplayVersion() throws Exception {

        DomibusVersionService service = new DomibusVersionService();

        assertEquals("domibus-MSH", service.getArtifactName());
        assertNotEquals("", service.getBuiltTime());
        assertNotEquals("", service.getArtifactVersion());

        LOG.info(service.getDisplayVersion());
    }

    @Test
    public void testVersionNumber(@Mocked Properties domibusProps) throws Exception {

        new Expectations() {{
            domibusProps.getProperty("Artifact-Version");
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
}
