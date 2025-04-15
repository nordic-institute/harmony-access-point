package eu.domibus.core.certificate;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

import java.security.KeyStore;

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

    @Injectable
    private MultiDomainCryptoServiceImpl multiDomainCertificateProvider;

    @Test
    public void executeJob(@Mocked JobExecutionContext context, @Mocked Domain domain, @Mocked KeyStore trustStore, @Mocked KeyStore keyStore) {

        new Expectations() {{
            multiDomainCertificateProvider.getTrustStore(domain);
            result = trustStore;
            multiDomainCertificateProvider.getKeyStore(domain);
            result = keyStore;
        }};
        saveCertificateAndLogRevocationJob.executeJob(context, domain);

        new FullVerifications() {{
            certificateService.saveCertificateAndLogRevocation(trustStore, keyStore);
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