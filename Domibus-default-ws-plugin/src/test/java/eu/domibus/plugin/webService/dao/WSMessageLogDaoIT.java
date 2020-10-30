package eu.domibus.plugin.webService.dao;

import eu.domibus.plugin.webService.entity.WSMessageLogEntity;
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
@ContextConfiguration(classes = {WSPluginInMemoryDataBaseConfig.class, WSPluginDaoConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class WSMessageLogDaoIT {

    @Autowired
    private WSMessageLogDao wsMessageLogDao;

    @PersistenceContext(unitName = "domibusJTA")
    private javax.persistence.EntityManager em;

    @Test
    public void findByMessageId_notFound() {
        WSMessageLogEntity byMessageId = wsMessageLogDao.findByMessageId("");
        Assert.assertNull(byMessageId);
    }

    @Test
    @Transactional
    public void findByMessageId_findOne() {

        WSMessageLogEntity entity = new WSMessageLogEntity();
        entity.setMessageId("messageId");
        wsMessageLogDao.create(entity);
        em.flush();

        WSMessageLogEntity byMessageId = wsMessageLogDao.findByMessageId("messageId");
        Assert.assertNotNull(byMessageId);
    }
}