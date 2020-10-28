package eu.domibus.core.user.ui;

import eu.domibus.core.dao.ListDao;
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

//    @Autowired
//    private UserRoleDao userRoleDao;

//    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserFilteringDao.class);

    public UserFilteringDao() {
        super(User.class);
    }

    @Override
    protected List<Predicate> getPredicates(Map<String, Object> filters, CriteriaBuilder criteriaBuilder, Root<User> userEntity) {
        List<Predicate> predicates = new ArrayList<>();

        final SetJoin<User, UserRole> userRoleJoin = userEntity.join(User_.roles);
        final Predicate x = criteriaBuilder.and(criteriaBuilder.equal(userRoleJoin.get(UserRole_.NAME), "ROLE_USER"));
        predicates.add(x);

//        for (Map.Entry<String, Object> filter : filters.entrySet()) {
//            String filterKey = filter.getKey();
//            //Object filterValue = filter.getValue();
//            if (filter.getValue() != null) {
//                if (filter.getKey().equals("roles")) {
//
//                    //Solution 1 - SetJoin
//                    final SetJoin<User, UserRole> userRoleJoin = userEntity.join(User_.roles);
//                    final Predicate x = criteriaBuilder.and(criteriaBuilder.equal(userRoleJoin.get(UserRole_.NAME), "ROLE_ADMIN"));
//                    predicates.add(x);
//
//                    /*Solution 2 - Join
//                    Join<User, UserRole> bJoin = userEntity.join("roles");
//                    bJoin.on(criteriaBuilder.equal(bJoin.get("name"), "ROLE_ADMIN"));*/
//
//                     /* Solution 3 - filtering with primary key "PK_ID"
//                     predicates.add(criteriaBuilder.and(
//                            criteriaBuilder.equal(userEntity.get("entityId"), 1)));*/
//
//                   /* Solution 4 - Adding set of User Roles to predicates
//
//                    Set<UserRole> roles  = new HashSet<>();
//                    UserRole userRole = userRoleDao.findByName("ROLE_ADMIN");
//                    roles.add(userRole);
//                    Path<Object> userField = userEntity.get("roles");
//                    predicates.add(userField.in(roles));*/
//
//                    //Solution 5 - Adding Expression set of User Roles to predicates
//
//                   /* Expression<Set<UserRole>> TargetField = userEntity.get(User_.roles);
//                    predicates.add(TargetField.in(filter.getValue()));*/
//
//                    //Solution 6 - Not null User Roles
//                    //predicates.add(criteriaBuilder.isNotNull(userEntity.get(User_.roles)));
//
//                    //Solution 7 - empty hashSet
//                   /* Path<Object> userField = userEntity.get("roles");
//                    predicates.add(userField.in(new HashSet<>()));*/
//
//
//                }
////                if (filter.getValue() instanceof String) {
////                    addStringPredicates(criteriaBuilder, userEntity, predicates, filter, filterKey);
////                } else {
////                    predicates.add(criteriaBuilder.equal(userEntity.<String>get(filterKey), filter.getValue()));
////                }
//            } else {
//                continue;
//            }
//        }
        return predicates;
    }

    private void addStringPredicates(CriteriaBuilder criteriaBuilder, Root<?> user, List<Predicate> predicates, Map.Entry<String, Object> filter, String filterKey) {
        if (StringUtils.isNotBlank(filterKey) && !filter.getValue().toString().isEmpty()) {
            predicates.add(criteriaBuilder.like(user.get(filter.getKey()), (String) filter.getValue()));
        }
    }

}
