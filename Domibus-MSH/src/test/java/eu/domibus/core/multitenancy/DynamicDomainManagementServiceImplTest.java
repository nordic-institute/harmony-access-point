package eu.domibus.core.multitenancy;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainsAware;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.ext.services.DomainsAwareExt;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    private List<DomainsAware> domainsAwareList;

    @Injectable
    private List<DomainsAwareExt> externalDomainsAwareList;

    @Injectable
    private DomibusCoreMapper coreMapper;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    List<Domain> domains, allDomains;
    Domain domain1, domain2;

    {
        domain1 = new Domain("domain1", "domain1");
        domain2 = new Domain("domain2", "domain2");
        domains = Arrays.asList(domain1);
        allDomains = Arrays.asList(domain1, domain2);
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
}
