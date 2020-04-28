package eu.domibus.plugin.fs.custom;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;


@Component
public class FSPluginEntityDao extends BasicDao<Domain2Entity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPluginEntityDao.class);

    public FSPluginEntityDao() {
        super(Domain2Entity.class);
    }

    public List<Domain1Entity> findAllForDomain1() {
        try {
            final TypedQuery<Domain1Entity> query = em.createNamedQuery("Domain1Entity.findAll", Domain1Entity.class);
            return query.getResultList();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message attempts");
            return null;
        }
    }

    public List<Domain2Entity> findAllForDomain2() {
        try {
            final TypedQuery<Domain2Entity> query = em.createNamedQuery("Domain2Entity.findAll", Domain2Entity.class);
            return query.getResultList();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message attempts");
            return null;
        }
    }

    public List<Domain3Entity> findAllForDomain3() {
        try {
            final TypedQuery<Domain3Entity> query = em.createNamedQuery("Domain3Entity.findAll", Domain3Entity.class);
            return query.getResultList();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message attempts");
            return null;
        }
    }

    public List<Domain4Entity> findAllForDomain4() {
        try {
            final TypedQuery<Domain4Entity> query = em.createNamedQuery("Domain4Entity.findAll", Domain4Entity.class);
            return query.getResultList();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message attempts");
            return null;
        }
    }
}
