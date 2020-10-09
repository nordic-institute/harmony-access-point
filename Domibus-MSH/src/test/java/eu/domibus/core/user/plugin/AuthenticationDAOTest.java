package eu.domibus.core.user.plugin;

import eu.domibus.api.security.AuthRole;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
@RunWith(JMockit.class)
public class AuthenticationDAOTest {
    @Tested
    AuthenticationDAO authenticationDAO;

    @Injectable
    AuthenticationEntity authenticationEntity;

    @Injectable
    EntityManager entityManager;

    @Test
    public void findByUser(@Injectable TypedQuery<AuthenticationEntity> query) {
        final String username = "admin";

        new Expectations() {{
            entityManager.createNamedQuery("AuthenticationEntity.findByUsername", AuthenticationEntity.class);
            result = query;
        }};

        authenticationDAO.findByUser(username);

        new Verifications() {{
            Object userNameActual;
            query.setParameter(AuthenticationEntity.USER_NAME, userNameActual = withCapture());
            Assert.assertEquals(username, userNameActual);
        }};
    }

    @Test
    public void listByUser(@Injectable TypedQuery<AuthenticationEntity> query) {
        final String username = "admin";

        new Expectations() {{
            entityManager.createNamedQuery("AuthenticationEntity.findByUsername", AuthenticationEntity.class);
            result = query;
        }};

        authenticationDAO.listByUser(username);

        new Verifications() {{
            Object userNameActual;
            query.setParameter(AuthenticationEntity.USER_NAME, userNameActual = withCapture());
            Assert.assertEquals(username, userNameActual);
        }};
    }

    @Test
    public void getRolesForUser(@Injectable TypedQuery<String> query, @Injectable AuthRole authRole) {
        final String username = "admin";
        final List<AuthRole> authRoles = new ArrayList<>();

        new Expectations(authenticationDAO) {{
            entityManager.createNamedQuery("AuthenticationEntity.getRolesForUsername", String.class);
            result = query;
            authenticationDAO.getAuthRoles(query);
            result = authRoles;
        }};

        authenticationDAO.getRolesForUser(username);

        new Verifications() {{
            query.setParameter(AuthenticationEntity.USER_NAME, username);
            times = 1;

            authenticationDAO.getAuthRoles(query);
            times = 1;
        }};
    }

    @Test
    public void findByCertificateId(@Injectable TypedQuery<AuthenticationEntity> query) {
        final String certificateId = "cert";

        new Expectations() {{
            entityManager.createNamedQuery("AuthenticationEntity.findByCertificateId", AuthenticationEntity.class);
            result = query;
        }};

        authenticationDAO.findByCertificateId(certificateId);

        new Verifications() {{
            query.setParameter(AuthenticationEntity.CERTIFICATE_ID, certificateId);
            times = 1;

            query.getSingleResult();
            times = 1;
        }};
    }

    @Test
    public void listByCertificateId(@Injectable TypedQuery<AuthenticationEntity> query) {
        final String certificateId = "cert";

        new Expectations() {{
            entityManager.createNamedQuery("AuthenticationEntity.findByCertificateId", AuthenticationEntity.class);
            result = query;
        }};

        authenticationDAO.listByCertificateId(certificateId);

        new Verifications() {{
            query.setParameter(AuthenticationEntity.CERTIFICATE_ID, certificateId);
            times = 1;

            query.getResultList();
            times = 1;
        }};
    }

    @Test
    public void getRolesForCertificateId(@Injectable TypedQuery<String> query, @Injectable AuthRole authRole) {
        final String certificateId = "cert";
        final List<AuthRole> authRoles = new ArrayList<>();

        new Expectations(authenticationDAO) {{
            entityManager.createNamedQuery("AuthenticationEntity.getRolesForCertificateId", String.class);
            result = query;

            authenticationDAO.getAuthRoles(query);
            result = authRoles;
        }};

        authenticationDAO.getRolesForCertificateId(certificateId);

        new Verifications() {{
            query.setParameter(AuthenticationEntity.CERTIFICATE_ID, certificateId);
            times = 1;

            authenticationDAO.getAuthRoles(query);
            times = 1;
        }};
    }

    @Test
    public void getAuthRoles(@Injectable TypedQuery<String> query) {
        final String rolesStr = "ROLE_ADMIN;ROLE_USER";

        new Expectations() {{
            query.getSingleResult();
            result = rolesStr;
        }};

        List<AuthRole> authRoles = authenticationDAO.getAuthRoles(query);

        Assert.assertEquals(authRoles.size(), 2);
    }

    @Test
    public void getPredicates(@Injectable CriteriaBuilder cb, @Injectable Root<AuthenticationEntity> ele, @Injectable Predicate predicate) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("authType", "CERTIFICATE");

        List<Predicate> predicates = authenticationDAO.getPredicates(filters, cb, ele);

        Assert.assertEquals(predicates.size(), 1);
    }

    @Test
    public void findWithPasswordChangedBetween(@Injectable LocalDate from, @Injectable LocalDate to, @Injectable TypedQuery<String> query) {
        final boolean withDefaultPassword = true;

        new Expectations() {{
            entityManager.createNamedQuery("AuthenticationEntity.findWithPasswordChangedBetween", AuthenticationEntity.class);
            result = query;
        }};

        authenticationDAO.findWithPasswordChangedBetween(from, to, withDefaultPassword);

        new FullVerifications() {{
            query.setParameter("START_DATE", from.atStartOfDay());
            query.setParameter("END_DATE", to.atStartOfDay());
            query.setParameter("DEFAULT_PASSWORD", withDefaultPassword);
            query.getResultList().stream().collect(Collectors.toList());
        }};
    }

    @Test
    public void update() {
        final boolean flush = true;

        authenticationDAO.update(authenticationEntity, flush);

        new Verifications() {{
            authenticationDAO.update(authenticationEntity);
            times = 1;
        }};
    }

    @Test
    public void getSuspendedUsers(@Injectable TypedQuery<AuthenticationEntity> query) {
        final Date currentTimeMinusSuspensionInterval = new Date(2323223232L);

        new Expectations() {{
            entityManager.createNamedQuery("AuthenticationEntity.findSuspendedUsers", AuthenticationEntity.class);
            result = query;
        }};

        authenticationDAO.getSuspendedUsers(currentTimeMinusSuspensionInterval);

        new Verifications() {{
            query.setParameter("SUSPENSION_INTERVAL", currentTimeMinusSuspensionInterval);
            times = 1;

            query.getResultList().stream().collect(Collectors.toList());
            times = 1;
        }};
    }

    @Test
    public void updateUsers() {
        List<AuthenticationEntity> users = new ArrayList<>();
        users.add(authenticationEntity);
        authenticationDAO.update(users);

        new Verifications() {{
            authenticationDAO.update(authenticationEntity);
            times = 1;
        }};
    }

}