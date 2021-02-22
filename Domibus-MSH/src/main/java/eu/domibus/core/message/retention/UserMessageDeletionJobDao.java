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
public class UserMessageDeletionJobDao extends BasicDao<UserMessageDeletionJob> {


    public UserMessageDeletionJobDao() {
        super(UserMessageDeletionJob.class);
    }

    public List<UserMessageDeletionJob> findCurrentDeletionJobs() {
        final TypedQuery<UserMessageDeletionJob> query = this.em.createNamedQuery("UserMessageDeletionJobDao.findCurrentDeletionJobs", UserMessageDeletionJob.class);
        return query.getResultList();
    }
}
