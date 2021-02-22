package eu.domibus.core.message.retention;

import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;


/**
 * This class is responsible for the state of a deletion job
 *
 * @author idragusa
 * @since 4.2.1
 */
@Repository
public class DeletionJobDao extends BasicDao<DeletionJob> {


    public DeletionJobDao() {
        super(DeletionJob.class);
    }

    public List<DeletionJob> findCurrentDeletionJobs() {
        final TypedQuery<DeletionJob> query = this.em.createNamedQuery("DeletionJobDao.findCurrentDeletionJobs", DeletionJob.class);
        return query.getResultList();
    }
}
