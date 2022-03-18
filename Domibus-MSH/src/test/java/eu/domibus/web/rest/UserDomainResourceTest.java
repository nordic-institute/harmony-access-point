package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.multitenancy.DynamicDomainManagementService;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.security.DomibusUserDetailsImpl;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.collections4.CollectionUtils;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class UserDomainResourceTest {

    @Tested
    private UserDomainResource domainsResource;

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
    public void testGetDomains_ActiveFlagWhenNoUserDetails() {
        // GIVEN
        new Expectations() {{
            authUtils.getUserDetails();
            result = null;
        }};

        // WHEN
        domainsResource.getDomains();

        // THEN
        new FullVerifications() {{
        }};
    }


    @Test
    public void testGetDomains(@Injectable DomibusUserDetailsImpl userDetails,
                               @Injectable List<DomainRO> domainROEntries) {
        final Domain red = new Domain("red", "Red");
        final Domain yellow = new Domain("yellow", "Yellow");
        final Domain blue = new Domain("blue", "Blue");

        // GIVEN
        new Expectations() {{
            authUtils.getUserDetails();
            result = userDetails;

            userDetails.getAvailableDomainCodes();
            result = new HashSet<>(Arrays.asList("red", "yellow", "blue"));

            domainService.getDomain("red");
            result = red;
            domainService.getDomain("yellow");
            result = yellow;
            domainService.getDomain("blue");
            result = blue;

            coreMapper.domainListToDomainROList(withArgThat(
                            new CustomTypeSafeMatcher<List<Domain>>("The argument list can contain domains in any order") {
                                @Override
                                protected boolean matchesSafely(List<Domain> domains) {
                                    return CollectionUtils.containsAll(domains,
                                            Arrays.asList(red, yellow, blue));
                                }
                            }
                    )
            );
            result = domainROEntries;
        }};

        // WHEN
        final List<DomainRO> result = domainsResource.getDomains();

        // THEN
        new FullVerifications() { /* no unexpected interactions */
        };
        assertEquals(domainROEntries, result);
    }
}
