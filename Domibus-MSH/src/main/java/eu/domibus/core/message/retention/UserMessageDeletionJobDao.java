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
public class UserMessageDeletionJobDao extends BasicDao<UserMessageDeletionJobEntity> {


    public UserMessageDeletionJobDao() {
        super(UserMessageDeletionJobEntity.class);
    }

    public List<UserMessageDeletionJobEntity> findCurrentDeletionJobs() {
        final TypedQuery<UserMessageDeletionJobEntity> query = this.em.createNamedQuery("UserMessageDeletionJobEntity.findCurrentDeletionJobs", UserMessageDeletionJobEntity.class);
        return query.getResultList();
    }
}
