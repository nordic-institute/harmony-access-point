package eu.domibus.core.user.ui;

import eu.domibus.core.dao.ListDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Filtering users from database based on search criteria's
 *
 * @author Soumya Chandran
 * @since 4.2
 */
@Repository
@Transactional
public class UserFilteringDao extends ListDao<User> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserFilteringDao.class);
    protected static final String USER_NAME = "userName";
    protected static final String USER_ROLE = "userRole";
    protected static final String DELETED_USER = "deleted";

    public UserFilteringDao() {
        super(User.class);
    }


    @Override
    protected List<Predicate> getPredicates(Map<String, Object> filters, CriteriaBuilder criteriaBuilder, Root<User> userEntity) {
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                addUserRolePredicate(criteriaBuilder, userEntity, predicates, filter);
                addUserNamePredicate(criteriaBuilder, userEntity, predicates, filter);
                addUserDeletedPredicate(criteriaBuilder, userEntity, predicates, filter);
            }
        }
        LOG.debug("Number of predicates added for users filtering [{}]", predicates.size());
        return predicates;
    }

    protected void addUserDeletedPredicate(CriteriaBuilder criteriaBuilder, Root<User> userEntity, List<Predicate> predicates, Map.Entry<String, Object> filter) {
        if (StringUtils.equals(filter.getKey(), DELETED_USER)) {
            predicates.add(criteriaBuilder.equal(userEntity.<String>get(filter.getKey()), filter.getValue()));
        }
    }

    protected void addUserNamePredicate(CriteriaBuilder criteriaBuilder, Root<User> userEntity, List<Predicate> predicates, Map.Entry<String, Object> filter) {
        String filterKey = filter.getKey();
        if (StringUtils.equals(filterKey, USER_NAME)) {
            addStringPredicates(criteriaBuilder, userEntity, predicates, filter);
        }
    }

    protected void addUserRolePredicate(CriteriaBuilder criteriaBuilder, Root<User> userEntity, List<Predicate> predicates, Map.Entry<String, Object> filter) {
        if (StringUtils.equals(filter.getKey(), USER_ROLE)) {
            final SetJoin<User, UserRole> userRoleJoin = userEntity.join(User_.roles);
            final Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(userRoleJoin.get(UserRole_.NAME), filter.getValue()));
            predicates.add(predicate);
        }
    }

    protected void addStringPredicates(CriteriaBuilder criteriaBuilder, Root<?> user, List<Predicate> predicates, Map.Entry<String, Object> filter) {
        if (StringUtils.isNotBlank(filter.getKey()) && StringUtils.isNotBlank(filter.getValue().toString())) {
            predicates.add(criteriaBuilder.like(user.get(filter.getKey()), (String) filter.getValue()));
        }
    }
}
