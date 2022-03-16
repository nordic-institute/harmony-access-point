package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.multitenancy.DynamicDomainManagementService;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.security.DomibusUserDetails;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.collections4.CollectionUtils;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class DomainResourceTest {

    @Tested
    private DomainResource domainsResource;

    @Injectable
    private DomibusCoreMapper coreMapper;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DynamicDomainManagementService dynamicDomainManagementService;

    @Injectable
    private DomainDao domainDao;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Test
    public void testGetDomains_IgnoringActiveFlag(@Injectable List<Domain> domainEntries,
                                                  @Injectable List<DomainRO> domainROEntries) {
        // GIVEN
        final Boolean activeFlag = null;
        new Expectations() {{
            domainService.getPotentialDomains();
            result = domainEntries;

            coreMapper.domainListToDomainROList(domainEntries);
            result = domainROEntries;
        }};

        // WHEN
        List<DomainRO> result = domainsResource.getDomains(activeFlag);

        // THEN
        new FullVerifications() { /* no unexpected interactions */
        };
        assertEquals(domainROEntries, result);
    }

    @Test
    public void testGetDomains_ActiveFlagWhenNoUserDetails() {
        // GIVEN
        new Expectations() {{
            domainService.getDomains();
            result = new ArrayList<>();
        }};

        // WHEN
        domainsResource.getDomains(true);

        // THEN
        new FullVerifications() {{
            coreMapper.domainListToDomainROList(new ArrayList<>());
        }};
    }

}
