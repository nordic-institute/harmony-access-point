package eu.domibus.core.audit;


import eu.domibus.AbstractIT;
import eu.domibus.core.audit.envers.ModificationType;
import eu.domibus.core.audit.envers.RevisionLog;
import eu.domibus.core.audit.model.Audit;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Transactional
public class AuditDaoIT extends AbstractIT {

    @Autowired
    AuditDao auditDao;



    @Test
    @Transactional
    public void testListAllAuditsAndExceptSuperUsers() {
        RevisionLog revisionLog = new RevisionLog();

        // 3 domain users:
        revisionLog.addEntityAudit("1", "eu.domibus.core.multitenancy.dao.UserDomainEntity", "UserDomain", ModificationType.MOD, 1);
        revisionLog.addEntityAudit("2", "eu.domibus.core.multitenancy.dao.UserDomainEntity", "UserDomain", ModificationType.DEL, 1);
        revisionLog.addEntityAudit("13", "eu.domibus.core.multitenancy.dao.UserDomainEntity", "UserDomain", ModificationType.DEL, 1);

        // 1 super user:
        revisionLog.addEntityAudit("14", "eu.domibus.core.user.ui.User", "User", ModificationType.MOD, 1);

        revisionLog.setUserName("admin");
        revisionLog.setRevisionDate(new Date());
        em.persist(revisionLog);
        em.flush();

        List<String> superIds = Arrays.asList(new String[]{"14"});
        // no filtering from UI
        List<Audit> all = auditDao.listAudit(null, null, null, null, null, 0, 100);
        List<Audit> allExceptSupers = auditDao.listAuditExceptSuperUsers(null, null, null, null, null, 0, 100, superIds);

        assertNotNull(all);
//        assertEquals(4, all.size());

        assertNotNull(allExceptSupers);
        assertEquals(1, all.size()-allExceptSupers.size()); //the super id excluded

        List<String> justTheIds = allExceptSupers.stream().map(a -> a.getId()).collect(Collectors.toList());
        assertTrue(justTheIds.contains("1"));
        assertTrue(justTheIds.contains("2"));
        assertTrue(justTheIds.contains("13"));
        assertFalse(justTheIds.contains("14"));
    }


    @Test
    @Transactional
    public void testCountAllAuditsAndExceptSuperUsers() {
        RevisionLog revisionLog = new RevisionLog();

        // 3 domain users:
        revisionLog.addEntityAudit("1", "eu.domibus.core.multitenancy.dao.UserDomainEntity", "UserDomain", ModificationType.ADD, 1);
        revisionLog.addEntityAudit("2", "eu.domibus.core.multitenancy.dao.UserDomainEntity", "UserDomain", ModificationType.MOD, 1);
        revisionLog.addEntityAudit("13", "eu.domibus.core.multitenancy.dao.UserDomainEntity", "UserDomain", ModificationType.DEL, 1);

        // 1 super user:
        revisionLog.addEntityAudit("14", "eu.domibus.core.user.ui.User", "User", ModificationType.MOD, 1);

        em.persist(revisionLog);
        em.flush();

        List<String> superIds = Arrays.asList(new String[]{"14"});
        // no filtering from UI
        Long countAll = auditDao.countAudit(null, null, null, null, null);
        Long countExceptSupers = auditDao.countAuditExceptSuperUsers(null, null, null, null, null, superIds);

        assertNotNull(countAll);

        assertNotNull(countExceptSupers);
        assertEquals(1, countAll-countExceptSupers);

    }

}
