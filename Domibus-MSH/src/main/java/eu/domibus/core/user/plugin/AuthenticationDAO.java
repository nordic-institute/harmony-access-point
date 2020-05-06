package eu.domibus.core.user.plugin;

import eu.domibus.api.security.AuthRole;
import eu.domibus.core.dao.ListDao;
import eu.domibus.core.user.UserDaoBase;
import eu.domibus.core.user.UserEntityBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository("securityAuthenticationDAO")
@Transactional
public class AuthenticationDAO extends ListDao<AuthenticationEntity> implements UserDaoBase<AuthenticationEntity> {

    public AuthenticationDAO() {
        super(AuthenticationEntity.class);
    }

    public AuthenticationEntity findByUser(final String username) {
        final TypedQuery<AuthenticationEntity> query = this.em.createNamedQuery("AuthenticationEntity.findByUsername", AuthenticationEntity.class);
        query.setParameter("USERNAME", username);

        return query.getSingleResult();
    }

    public List<AuthenticationEntity> listByUser(final String username) {
        final TypedQuery<AuthenticationEntity> query = this.em.createNamedQuery("AuthenticationEntity.findByUsername", AuthenticationEntity.class);
        query.setParameter("USERNAME", username);

        return query.getResultList();
    }

    public List<AuthRole> getRolesForUser(final String username) {
        final TypedQuery<String> query = this.em.createNamedQuery("AuthenticationEntity.getRolesForUsername", String.class);
        query.setParameter("USERNAME", username);

        return getAuthRoles(query);
    }

    public AuthenticationEntity findByCertificateId(final String certificateId) {
        final TypedQuery<AuthenticationEntity> query = this.em.createNamedQuery("AuthenticationEntity.findByCertificateId", AuthenticationEntity.class);
        query.setParameter("CERTIFICATE_ID", certificateId);

        return query.getSingleResult();
    }

    public List<AuthenticationEntity> listByCertificateId(final String certificateId) {
        final TypedQuery<AuthenticationEntity> query = this.em.createNamedQuery("AuthenticationEntity.findByCertificateId", AuthenticationEntity.class);
        query.setParameter("CERTIFICATE_ID", certificateId);

        return query.getResultList();
    }

    public List<AuthRole> getRolesForCertificateId(final String certificateId) {
        final TypedQuery<String> query = this.em.createNamedQuery("AuthenticationEntity.getRolesForCertificateId", String.class);
        query.setParameter("CERTIFICATE_ID", certificateId);

        return getAuthRoles(query);
    }

    private List<AuthRole> getAuthRoles(TypedQuery<String> query) {
        List<AuthRole> authRoles = new ArrayList<>();
        String rolesStr = query.getSingleResult();
        String[] roles = StringUtils.split(rolesStr, ';');
        for (String role : roles) {
            authRoles.add(AuthRole.valueOf(StringUtils.strip(role)));
        }
        return authRoles;
    }

    protected List<Predicate> getPredicates(Map<String, Object> filters, CriteriaBuilder cb, Root<AuthenticationEntity> ele) {
        List<Predicate> predicates = new ArrayList<>();
        for (final Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() == null || StringUtils.isEmpty((String) filter.getValue()) || StringUtils.isEmpty(filter.getKey())) {
                continue;
            }

            if (filter.getKey().equals("authType")) {
                if (filter.getValue().equals("CERTIFICATE")) {
                    predicates.add(cb.isNotNull(ele.<String>get("certificateId")));
                } else {
                    predicates.add(cb.isNull(ele.<String>get("certificateId")));
                }
            } else {
                predicates.add(cb.like(ele.<String>get(filter.getKey()), (String) filter.getValue()));
            }
        }
        return predicates;
    }

    public List<UserEntityBase> findWithPasswordChangedBetween(LocalDate from, LocalDate to, boolean withDefaultPassword) {
        TypedQuery<AuthenticationEntity> namedQuery = em.createNamedQuery("AuthenticationEntity.findWithPasswordChangedBetween", AuthenticationEntity.class);
        namedQuery.setParameter("START_DATE", from.atStartOfDay());
        namedQuery.setParameter("END_DATE", to.atStartOfDay());
        namedQuery.setParameter("DEFAULT_PASSWORD", withDefaultPassword);
        return namedQuery.getResultList().stream().collect(Collectors.toList());
    }

    @Override
    public void update(AuthenticationEntity user, boolean flush) {
        super.update(user);
    }

    @Override
    public List<UserEntityBase> getSuspendedUsers(Date currentTimeMinusSuspensionInterval) {
        TypedQuery<AuthenticationEntity> namedQuery = em.createNamedQuery("AuthenticationEntity.findSuspendedUsers", AuthenticationEntity.class);
        namedQuery.setParameter("SUSPENSION_INTERVAL", currentTimeMinusSuspensionInterval);
        return namedQuery.getResultList().stream().collect(Collectors.toList());
    }

    @Override
    public void update(final List<AuthenticationEntity> users) {
        for (final AuthenticationEntity u : users) {
            super.update(u);
        }
    }

    @Override
    public List<AuthenticationEntity> findByRole(String roleName) {
        throw new NotImplementedException("findByRole");
    }

    @Override
    public UserEntityBase findByUserName(String userName) {
        return findByUser(userName);
    }

    @Override
    public boolean existsWithId(String userId) {
        return CollectionUtils.isNotEmpty(listByUser(userId))
                || CollectionUtils.isNotEmpty(listByCertificateId(userId));
    }
}
