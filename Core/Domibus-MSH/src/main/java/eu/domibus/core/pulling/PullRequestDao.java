package eu.domibus.core.pulling;

import eu.domibus.common.JPAConstants;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * @author Thomas Dussart
 * @since 5.0
 */

@Repository("pullRequestDAO")
public class PullRequestDao {

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;

    public void savePullRequest(PullRequest pullRequest) {
        em.persist(pullRequest);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deletePullRequest(String pullRequestId) {
        Query query = this.em.createNamedQuery("PullRequest.delete");
        query.setParameter("UUID",pullRequestId);
        query.executeUpdate();
    }

    public Long countPendingPullRequest() {
        final TypedQuery<Long> query = this.em.createNamedQuery("PullRequest.count", Long.class);
        return query.getSingleResult();
    }
}
