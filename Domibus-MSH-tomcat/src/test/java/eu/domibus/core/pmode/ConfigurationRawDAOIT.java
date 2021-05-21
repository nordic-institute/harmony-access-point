package eu.domibus.core.pmode;

import eu.domibus.AbstractIT;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.common.model.configuration.ConfigurationRaw;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author François Gautier
 * @since 5.0
 */
public class ConfigurationRawDAOIT  extends AbstractIT {

    public static final String LAST_DESCRIPTION = "Last and current";

    @Autowired
    private ConfigurationRawDAO configurationRawDAO;

    /* Service needed to trigger the audit */
    @Autowired
    private ConfigurationRawTestService configurationRawServiceTest;

    @Before
    public void initialize() {
        configurationRawServiceTest.createConfigurationRawAudited(persistEntity("1st Created never updated"));

        configurationRawServiceTest.createConfigurationRawAudited(persistEntity(LAST_DESCRIPTION));
    }

    private ConfigurationRaw persistEntity(String description) {
        ConfigurationRaw entity = getEntity();
        entity.setDescription(description);
        entity.setConfigurationDate(new Date());
        return entity;
    }

    private ConfigurationRaw getEntity() {
        return new ConfigurationRaw();
    }

    @Test
    @Transactional
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void configurationRaw() {
        List<PModeArchiveInfo> detailedConfigurationRaw = configurationRawDAO.getDetailedConfigurationRaw();
        assertEquals(2, detailedConfigurationRaw.size());
        assertEquals(LAST_DESCRIPTION, detailedConfigurationRaw.get(0).getDescription());

        ConfigurationRaw currentRawConfiguration = configurationRawDAO.getCurrentRawConfiguration();
        assertEquals(LAST_DESCRIPTION, currentRawConfiguration.getDescription());
    }
}