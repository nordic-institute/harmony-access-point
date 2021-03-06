package eu.domibus.core.property;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Properties;
import java.util.TimeZone;

/**
 * @author Federico Martini , soumya
 * <p>
 * This class is designed to retrieve the main Domibus properties defined in a file and valued using Maven resource filtering.
 * Spring will take care of the creation of this Singleton object at startup.
 */
@Service(value = "domibusPropertiesService")
public class DomibusVersionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusVersionService.class);

    private static Properties domibusProps = new Properties();

    public DomibusVersionService() {
        init();
    }

    public void init() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config/application.properties")) {
            if (is == null) {
                LOG.warn("The 'domibus.properties' has not been found!");
                return;
            }
            domibusProps.load(is);
            LOG.info("=========================================================================================================");
            LOG.info("|         " + getDisplayVersion() + "        |");
            LOG.info("=========================================================================================================");
        } catch (Exception ex) {
            LOG.warn("Error loading Domibus properties", ex);
        }
    }

    public String getArtifactVersion() {
        return domibusProps.getProperty("Artifact-Version");
    }

    public String getVersionNumber() {
        String artifactVersion = getArtifactVersion();
        String versionNumber = artifactVersion.split("-")[0];
        return versionNumber;
    }

    public String getArtifactName() {
        return domibusProps.getProperty("Artifact-Name");
    }

    public String getBuiltTime() {
        return domibusProps.getProperty("Build-Time") + "|" + TimeZone.getDefault().getDisplayName();
    }

    public String getDisplayVersion() {
        StringBuilder display = new StringBuilder();
        display.append(getArtifactName());
        display.append(" Version [");
        display.append(getArtifactVersion());
        display.append("] Build-Time [");
        display.append(getBuiltTime());
        display.append("]");
        return display.toString();
    }

    public String getBuildDetails() {
        StringBuilder display = new StringBuilder();
        display.append(" Build-Name [");
        display.append(getArtifactName());
        display.append("]");
        return display.toString();

    }
}
