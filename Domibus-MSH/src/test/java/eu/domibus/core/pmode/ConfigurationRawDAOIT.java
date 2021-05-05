package eu.domibus.core.pmode;

import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.common.model.configuration.ConfigurationRaw;
import eu.domibus.core.dao.InMemoryDatabaseMshConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDatabaseMshConfig.class, PModeConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class ConfigurationRawDAOIT {

    public static final String LAST_DESCRIPTION = "Last and current";

    @Autowired
    private ConfigurationRawDAO configurationRawDAO;

    /* Service needed to trigger the audit */
    @Autowired
    private ConfigurationRawTestService configurationRawServiceTest;

    @Before
    public void init() {
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
    public void configurationRaw() {
        List<PModeArchiveInfo> detailedConfigurationRaw = configurationRawDAO.getDetailedConfigurationRaw();
        assertEquals(2, detailedConfigurationRaw.size());
        assertEquals(LAST_DESCRIPTION, detailedConfigurationRaw.get(0).getDescription());

        ConfigurationRaw currentRawConfiguration = configurationRawDAO.getCurrentRawConfiguration();
        assertEquals(LAST_DESCRIPTION, currentRawConfiguration.getDescription());
    }
}