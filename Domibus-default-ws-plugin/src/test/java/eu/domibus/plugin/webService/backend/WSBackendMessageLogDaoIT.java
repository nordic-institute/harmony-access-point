package eu.domibus.plugin.webService.backend;

import eu.domibus.plugin.webService.WSPluginDaoTestConfig;
import eu.domibus.test.dao.InMemoryDataBaseConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceContext;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class, WSPluginDaoTestConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class WSBackendMessageLogDaoIT {

    @Autowired
    private WSBackendMessageLogDao wsBackendMessageLogDao;

    @PersistenceContext(unitName = "domibusJTA")
    private javax.persistence.EntityManager em;

    @Test
    public void findByMessageId_notFound() {
        WSBackendMessageLogEntity byMessageId = wsBackendMessageLogDao.findByMessageId("");
        Assert.assertNull(byMessageId);
    }

    @Test
    @Transactional
    public void findByMessageId_findOne() {

        WSBackendMessageLogEntity entity = new WSBackendMessageLogEntity();
        entity.setMessageId("messageId");
        wsBackendMessageLogDao.create(entity);
        em.flush();

        WSBackendMessageLogEntity byMessageId = wsBackendMessageLogDao.findByMessageId("messageId");
        Assert.assertNotNull(byMessageId);
    }
}