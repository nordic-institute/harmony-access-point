package eu.domibus.core.certificate;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DatabaseUtil;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
@RunWith(JMockit.class)
public class SaveCertificateAndLogRevocationJobTest {
    @Tested
    SaveCertificateAndLogRevocationJob saveCertificateAndLogRevocationJob;

    @Injectable
    private CertificateService certificateService;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private DatabaseUtil databaseUtil;


    @Test
    public void executeJob(@Mocked JobExecutionContext context, @Mocked Domain domain) {

        saveCertificateAndLogRevocationJob.executeJob(context, domain);

        new FullVerifications() {{
            certificateService.saveCertificateAndLogRevocation(domain);
            certificateService.sendCertificateAlerts();
        }};
    }

    @Test
    public void setQuartzJobSecurityContext() {

        saveCertificateAndLogRevocationJob.setQuartzJobSecurityContext();

        new FullVerifications() {{
            authUtils.setAuthenticationToSecurityContext("domibus-quartz", "domibus-quartz", AuthRole.ROLE_AP_ADMIN);
        }};
    }
}