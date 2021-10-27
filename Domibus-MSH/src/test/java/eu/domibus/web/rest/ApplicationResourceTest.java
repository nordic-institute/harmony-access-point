package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.property.DomibusVersionService;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.rest.ro.DomibusInfoRO;
import eu.domibus.web.rest.ro.SupportTeamInfoRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

/**
 * @author Tiago Miguel, Catalin Enache
 * @since 3.3
 */
@RunWith(JMockit.class)
public class ApplicationResourceTest {

    private static final String DOMIBUS_VERSION = "Domibus Unit Tests";
    private static final String DOMIBUS_CUSTOMIZED_NAME = "Domibus Customized Name";

    @Tested
    ApplicationResource applicationResource;

    @Injectable
    DomibusVersionService domibusVersionService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    DomainService domainService;

    @Injectable
    DomibusCoreMapper coreMapper;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    AuthUtils authUtils;

    @Injectable
    DomainTaskExecutor domainTaskExecutor;

    @Injectable
    DomibusCacheService domibusCacheService;

    @Test
    public void testGetDomibusInfo() {
        // Given
        new Expectations() {{
            domibusVersionService.getDisplayVersion();
            result = DOMIBUS_VERSION;
        }};

        // When
        DomibusInfoRO domibusInfo = applicationResource.getDomibusInfo();

        // Then
        Assert.assertNotNull(domibusInfo);
        Assert.assertEquals(DOMIBUS_VERSION, domibusInfo.getVersion());
    }

    public void testDomibusName(String name) {
        // Given
        new Expectations(applicationResource) {{
            domibusPropertyProvider.getProperty(DomainService.DEFAULT_DOMAIN, ApplicationResource.DOMIBUS_CUSTOM_NAME);
            result = name;
        }};

        // When
        final String domibusName = applicationResource.getDomibusName();

        // Then
        Assert.assertEquals(name, domibusName);
    }

    @Test
    public void testGetDomibusCustomName() {
        testDomibusName(DOMIBUS_CUSTOMIZED_NAME);
    }

    @Test
    public void testGetDomibusDefaultName() {
        testDomibusName("Domibus");
    }

    @Test
    public void testGetMultiTenancy() {
        // Given
        new Expectations(applicationResource) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
        }};

        // When
        final Boolean isMultiTenancy = applicationResource.getMultiTenancy();

        // Then
        Assert.assertEquals(true, isMultiTenancy);
    }

    @Test
    public void testGetFourCornerEnabled() {

        new Expectations() {{
            domibusConfigurationService.isFourCornerEnabled();
            result = false;
        }};

        //tested method
        boolean isFourCornerEnabled = applicationResource.getFourCornerModelEnabled();

        Assert.assertFalse(isFourCornerEnabled);
    }

    @Test
    public void testGetSupportTeamInfo() {
        final String supportTeamName = "The Avengers";
        final String supportTeamEmail = "ironman@avengers.com";
        new Expectations() {{
            domibusPropertyProvider.getProperty(ApplicationResource.SUPPORT_TEAM_NAME_KEY);
            result = supportTeamName;

            domibusPropertyProvider.getProperty(ApplicationResource.SUPPORT_TEAM_EMAIL_KEY);
            result = supportTeamEmail;
        }};

        //tested method
        SupportTeamInfoRO supportTeamInfoRO = applicationResource.getSupportTeamInfo();

        Assert.assertNotNull(supportTeamInfoRO);
        Assert.assertEquals(supportTeamName, supportTeamInfoRO.getName());
        Assert.assertEquals(supportTeamEmail, supportTeamInfoRO.getEmail());
    }
}
