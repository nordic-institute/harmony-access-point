package eu.domibus.core.user.ui;

import eu.domibus.core.dao.ListDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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
        Predicate rolePredicate = addUserRolePredicate(criteriaBuilder, userEntity, filters);
        if (rolePredicate != null) {
            predicates.add(rolePredicate);
        }
        Predicate namePredicate = addUserNamePredicate(criteriaBuilder, userEntity, filters);
        if (namePredicate != null) {
            predicates.add(namePredicate);
        }
        Predicate userDeletedPredicate = addUserDeletedPredicate(criteriaBuilder, userEntity, filters);
        if (userDeletedPredicate != null) {
            predicates.add(userDeletedPredicate);
        }
        LOG.debug("Number of predicates added for users filtering [{}]", predicates.size());
        return predicates;
    }

    protected Predicate addUserDeletedPredicate(CriteriaBuilder criteriaBuilder, Root<User> userEntity, Map<String, Object> filters) {
        Object filterValue = filters.get(DELETED_USER);
        if (filterValue != null) {
            return criteriaBuilder.equal(userEntity.get(DELETED_USER), filterValue);
        }
        return null;
    }

    protected Predicate addUserNamePredicate(CriteriaBuilder criteriaBuilder, Root<User> userEntity, Map<String, Object> filters) {
        Object filterValue = filters.get(USER_NAME);
        if (filterValue != null) {
            return criteriaBuilder.like(userEntity.get(USER_NAME), (String) filterValue);
        }
        return null;
    }

    protected Predicate addUserRolePredicate(CriteriaBuilder criteriaBuilder, Root<User> userEntity, Map<String, Object> filters) {
        Object filterValue = filters.get(USER_ROLE);
        if (filterValue != null) {
            final SetJoin<User, UserRole> userRoleJoin = userEntity.join(User_.roles);
            return criteriaBuilder.and(criteriaBuilder.equal(userRoleJoin.get(UserRole_.NAME), filterValue));

        }
        return null;
    }
}
