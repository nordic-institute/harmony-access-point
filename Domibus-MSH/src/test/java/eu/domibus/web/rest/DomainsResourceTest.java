package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.multitenancy.DynamicDomainManagementService;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.web.rest.ro.DomainRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

@RunWith(JMockit.class)
public class DomainsResourceTest {

    @Tested
    DomainsResource domainsResource;
    
    @Injectable
    DomibusCoreMapper coreMapper;

    @Injectable
    DomainService domainService;

    @Injectable
    DynamicDomainManagementService dynamicDomainManagementService;

    @Injectable
    DomainDao domainDao;

    @Test
    public void testGetDomains() {
        // Given
        final List<Domain> domainEntries = Collections.singletonList(DomainService.DEFAULT_DOMAIN);
        final DomainRO domainRO = new DomainRO();
        domainRO.setCode(DomainService.DEFAULT_DOMAIN.getCode());
        domainRO.setName(DomainService.DEFAULT_DOMAIN.getName());
        final List<DomainRO> domainROEntries = Collections.singletonList(domainRO);

        new Expectations(domainsResource) {{
            domainService.getDomains();
            result = domainEntries;

            coreMapper.domainListToDomainROList(domainEntries);
            result = domainROEntries;
        }};

        // When
        final List<DomainRO> result = domainsResource.getDomains(true);

        // Then
        Assert.assertNotNull(result);
        Assert.assertNotEquals(0, result.size());
        Assert.assertEquals(domainROEntries, result);
    }

}
