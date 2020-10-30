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

@Repository("userFilteringDao")
@Transactional
public class UserFilteringDao extends ListDao<User> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserFilteringDao.class);

    public UserFilteringDao() {
        super(User.class);
    }


    @Override
    protected List<Predicate> getPredicates(Map<String, Object> filters, CriteriaBuilder criteriaBuilder, Root<User> userEntity) {
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            String filterKey = filter.getKey();
            if (filter.getValue() != null) {
                if (filter.getKey().equals("userRole")) {
                    final SetJoin<User, UserRole> userRoleJoin = userEntity.join(User_.roles);
                    final Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(userRoleJoin.get("name"), filter.getValue()));
                    predicates.add(predicate);
                }
                if (filter.getKey().equals("userName")) {
                    addStringPredicates(criteriaBuilder, userEntity, predicates, filter, filterKey);
                }
                if (filter.getKey().equals("deleted")) {
                    predicates.add(criteriaBuilder.equal(userEntity.<String>get(filterKey), filter.getValue()));
                }
            } else {
                continue;
            }
        }
        LOG.debug("Number of predicates added for users filtering [{}]", predicates.size());
        return predicates;
    }

    private void addStringPredicates(CriteriaBuilder criteriaBuilder, Root<?> user, List<Predicate> predicates, Map.Entry<String, Object> filter, String filterKey) {
        if (StringUtils.isNotBlank(filterKey) && !filter.getValue().toString().isEmpty()) {
            predicates.add(criteriaBuilder.like(user.get(filter.getKey()), (String) filter.getValue()));
        }
    }
}
