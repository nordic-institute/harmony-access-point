package eu.domibus.core.plugin.routing.dao;

import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.plugin.routing.BackendFilterEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Repository
@Transactional
public class BackendFilterDao extends BasicDao<BackendFilterEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendFilterDao.class);

    public BackendFilterDao() {
        super(BackendFilterEntity.class);
    }

    public void create(final List<BackendFilterEntity> filters) {
        filters.forEach(filter -> super.create(filter));
    }

    public void update(final List<BackendFilterEntity> filters) {
        filters.forEach(filter -> super.update(filter));
    }

    public void delete(final List<BackendFilterEntity> filters) {
        super.deleteAll(filters);
    }

    public List<BackendFilterEntity> findAll() {
        final TypedQuery<BackendFilterEntity> query = em.createNamedQuery("BackendFilter.findEntriesOrderedByPriority", BackendFilterEntity.class);
        try {
            return query.getResultList();
        } catch (final NoResultException nrEx) {
            LOG.debug("Query BackendFilterEntity.findEntries did not find any result", nrEx);
            return new ArrayList<>();
        }
    }

}
