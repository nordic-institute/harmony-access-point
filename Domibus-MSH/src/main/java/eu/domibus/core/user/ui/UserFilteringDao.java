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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository("userFilteringDao")
@Transactional
public class UserFilteringDao extends ListDao<User> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserFilteringDao.class);

    public UserFilteringDao(){
        super(User.class);
        }

    @Override
    protected List<Predicate> getPredicates(Map<String, Object> filters, CriteriaBuilder criteriaBuilder, Root<User> userEntity) {
        List<Predicate> predicates = new ArrayList<>();
       /* for (final Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() == null || StringUtils.isEmpty((String) filter.getValue()) || StringUtils.isEmpty(filter.getKey())) {
                continue;
            } else {
                predicates.add(criteriaBuilder.like(ele.<String>get(filter.getKey()), (String) filter.getValue()));
           }
        }*/
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            String filterKey = filter.getKey();
            Object filterValue = filter.getValue();
            if (filterValue != null) {
                if (filter.getKey().equals("roles")) {
                    LOG.debug("Adding predicates for users roles : [{}]  [{}]  [{}]",filterValue, filterValue.toString(), filter.getValue());
                    predicates.add(criteriaBuilder.equal(userEntity.get(filterKey), filterValue));
                   // predicates.add(criteriaBuilder.equal(userEntity.<String>get(filterKey), filterValue.toString()));
                }
                if (filterValue instanceof String) {
                    addStringPredicates(criteriaBuilder, userEntity, predicates, filter, filterKey, filterValue);
                } else {
                    predicates.add(criteriaBuilder.equal(userEntity.<String>get(filterKey), filterValue));
                }
            } else {
                continue;
            }
        }
        return predicates;
    }

    private void addStringPredicates(CriteriaBuilder criteriaBuilder, Root<?> user, List<Predicate> predicates, Map.Entry<String, Object> filter, String filterKey, Object filterValue) {
        if (StringUtils.isNotBlank(filterKey) && !filter.getValue().toString().isEmpty()) {
            predicates.add(criteriaBuilder.like(user.get(filter.getKey()), (String) filter.getValue()));
        }
    }

}
