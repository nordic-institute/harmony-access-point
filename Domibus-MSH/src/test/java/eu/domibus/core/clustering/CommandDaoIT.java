package eu.domibus.core.clustering;

import eu.domibus.core.dao.InMemoryDataBaseConfig;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class, CommandDaoConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class CommandDaoIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(CommandDaoIT.class);

    @Autowired
    private CommandDao commandDao;

    @Autowired
    protected DataSource dataSource;

    @PersistenceContext(unitName = "domibusJTA")
    protected EntityManager em;

    @Before
    public void setup() {
        LOG.putMDC(DomibusLogger.MDC_USER, "test_user");
    }

    @Test
    public void createCommand() {
        CommandEntity entity = new CommandEntity();
        entity.setCreationTime(new Date());
        entity.setServerName("ms1");
        entity.setCommandName("command1");

        commandDao.create(entity);
    }

    @Test
    @Transactional
    public void deleteCommandAndProperties() {
        CommandEntity entity = new CommandEntity();
        entity.setCreationTime(new Date());
        entity.setServerName("ms1");
        entity.setCommandName("command1");

        HashMap<String, String> commandProperties = new HashMap<>();
        commandProperties.put("key1", "value1");
        commandProperties.put("key2", "value2");
        entity.setCommandProperties(commandProperties);
        commandDao.create(entity);
        em.flush();

        // Check the TB_COMMAND_PROPERTY rows were properly generated
        Assert.assertEquals(2, em.createNativeQuery("SELECT * FROM TB_COMMAND_PROPERTY").getResultList().size());
        List<CommandEntity> ms1 = commandDao.findCommandsByServerName("ms1");

        //Delete of TB_COMMAND should delete TB_COMMAND_PROPERTY related
        commandDao.delete(ms1.get(0));

        em.flush();
        Assert.assertEquals(0, em.createNativeQuery("SELECT * FROM TB_COMMAND_PROPERTY").getResultList().size());
        Assert.assertEquals(0, commandDao.findCommandsByServerName("ms1").size());
    }
}