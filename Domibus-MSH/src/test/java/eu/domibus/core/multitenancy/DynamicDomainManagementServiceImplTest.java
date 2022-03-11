package eu.domibus.core.multitenancy;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainsAware;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainsAwareExt;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(JMockit.class)
public class DynamicDomainManagementServiceImplTest {

    @Tested
    DynamicDomainManagementServiceImpl dynamicDomainManagementService;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private DomainDao domainDao;

    @Injectable
    private SignalService signalService;

    @Injectable
    private List<DomainsAware> domainsAwareList = new ArrayList<>();

    @Injectable
    private List<DomainsAwareExt> externalDomainsAwareList = new ArrayList<>();

    @Injectable
    private DomibusCoreMapper coreMapper;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    List<Domain> domains, allDomains;
    Domain domain1, domain2;

    {
        domain1 = new Domain("domain1", "domain1");
        domain2 = new Domain("domain2", "domain2");
        domains = new ArrayList<>();
        domains.add(domain1);
        allDomains = Arrays.asList(domain1, domain2);


        externalDomainsAwareList.add(new DomainsAwareExt() {
            @Override
            public void onDomainAdded(DomainDTO domain) {

            }

            @Override
            public void onDomainRemoved(DomainDTO domain) {

            }
        });
    }

    @Test(expected = DomibusDomainException.class)
    public void validateAdditionInvalidName() {
        new Expectations() {{
            domainService.getDomains();
            result = domains;
            domainDao.findAll();
            result = allDomains;
        }};

        dynamicDomainManagementService.validateAddition("domain3");
    }

    @Test(expected = DomibusDomainException.class)
    public void validateAdditionAlreadyAdded() {
        new Expectations() {{
            domainService.getDomains();
            result = domains;
        }};

        dynamicDomainManagementService.validateAddition("domain1");
    }

    @Test
    public void internalAddDomain(@Injectable List<DomainsAware> domainsAwareList) {
        new Expectations() {{
            domainService.getDomains();
            result = domains;
            domibusPropertyProvider.loadProperties((Domain) any);
        }};

        dynamicDomainManagementService.internalAddDomain(domain2);

        Assert.assertTrue(domainService.getDomains().contains(domain2));
    }

    @Test(expected = DomibusDomainException.class)
    public void internalAddDomainError() throws Exception {
        domainsAwareList.add(new DomainsAware() {
            @Override
            public void onDomainAdded(Domain domain) {
                throw new DomibusCertificateException();
            }

            @Override
            public void onDomainRemoved(Domain domain) {

            }
        });

        new Expectations() {{
            domainService.getDomains();
            result = domains;
            domibusPropertyProvider.loadProperties((Domain) any);
        }};

        dynamicDomainManagementService.internalAddDomain(domain2);

        Assert.assertFalse(domainService.getDomains().contains(domain2));

        new Verifications() {{
            dynamicDomainManagementService.handleAddDomainException(domain2, (List<DomainsAware>) any, (DomainsAware) any, (Exception) any);
        }};
    }

    @Test
    public void notifyExternalModules() {
        DomainDTO domain1Dto = new DomainDTO(domain1.getCode(), domain1.getName());
        new Expectations() {{
            coreMapper.domainToDomainDTO(domain1);
            result = domain1Dto;
        }};

        dynamicDomainManagementService.notifyExternalModulesofAddition(domain1);

        new Verifications() {{
            externalDomainsAwareList.get(0).onDomainAdded(domain1Dto);
        }};
    }

}
