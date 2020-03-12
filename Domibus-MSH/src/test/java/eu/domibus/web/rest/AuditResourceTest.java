package eu.domibus.web.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.domibus.api.audit.AuditLog;
import eu.domibus.common.model.common.ModificationType;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.web.rest.ro.AuditFilterRequestRO;
import eu.domibus.web.rest.ro.AuditResponseRo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class AuditResourceTest {

    @Mock
    private AuditService auditService;

    @Mock
    private DomainCoreConverter domainConverter;

    @InjectMocks
    private AuditResource auditResource;

    @Test
    public void listAudits() throws Exception {
        AuditFilterRequestRO auditCriteria = buildCriteria();
        List<AuditLog> result = Lists.newArrayList(new AuditLog());
        when(auditService.listAudit(
                auditCriteria.getAuditTargetName(),
                Sets.newHashSet(ModificationType.ADD.name()),
                auditCriteria.getUser(),
                auditCriteria.getFrom(),
                auditCriteria.getTo(),
                auditCriteria.getStart(),
                auditCriteria.getMax())).thenReturn(result);
        auditResource.listAudits(auditCriteria);
        verify(auditService, Mockito.times(1)).listAudit(
                auditCriteria.getAuditTargetName(),
                Sets.newHashSet(ModificationType.ADD.name()),
                auditCriteria.getUser(),
                auditCriteria.getFrom(),
                auditCriteria.getTo(),
                auditCriteria.getStart(),
                auditCriteria.getMax());
        verify(domainConverter, times(1)).convert(eq(result), eq(AuditResponseRo.class));
    }

    @Test
    public void countAudits() {
        AuditFilterRequestRO auditCriteria = buildCriteria();
        auditResource.countAudits(auditCriteria);
        verify(auditService, Mockito.times(1)).countAudit(
                auditCriteria.getAuditTargetName(),
                Sets.newHashSet(ModificationType.ADD.name()),
                auditCriteria.getUser(),
                auditCriteria.getFrom(),
                auditCriteria.getTo());

    }

    private AuditFilterRequestRO buildCriteria() {
        Date from = new Date();
        Date to = new Date(from.getTime() + 1000);
        AuditFilterRequestRO auditCriteria = new AuditFilterRequestRO();
        auditCriteria.setAuditTargetName(Sets.newHashSet("User", "Pmode"));
        auditCriteria.setAction(Sets.newHashSet("Created"));
        auditCriteria.setUser(Sets.newHashSet("Admin"));
        auditCriteria.setFrom(from);
        auditCriteria.setTo(to);
        auditCriteria.setStart(0);
        auditCriteria.setMax(10);
        return auditCriteria;
    }



    @Test
    public void auditTargets() throws Exception {
        auditResource.auditTargets();
        verify(auditService, times(1)).listAuditTarget();
    }

}