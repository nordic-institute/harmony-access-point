package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.plugin.fs.worker.FSSendMessagesService.DEFAULT_DOMAIN;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class FSDomainServiceTest {

    @Injectable
    protected DomibusConfigurationExtService domibusConfigurationExtService;

    @Injectable
    protected DomainExtService domainExtService;

    @Injectable
    protected FSPluginProperties fsPluginProperties;

    @Injectable
    protected DomainContextExtService domainContextExtService;

    @Tested
    FSDomainService fsDomainService;

    @Test
    public void testVerifyDomainExistsNonMultitenant() {
        new Expectations() {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = false;
        }};

        boolean verifyDomainExists = fsDomainService.verifyDomainExists("domain");

        Assert.assertTrue(verifyDomainExists);
    }

    @Test
    public void testVerifyDomainExistsMultitenantOk() {
        new Expectations() {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = true;
            domainExtService.getDomain(DEFAULT_DOMAIN);
            result = new DomainDTO(DEFAULT_DOMAIN, "Default");
        }};

        boolean verifyDomainExists = fsDomainService.verifyDomainExists(DEFAULT_DOMAIN);

        Assert.assertTrue(verifyDomainExists);
    }

    @Test
    public void testVerifyDomainExistsMultitenantException() {
        new Expectations() {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = true;
            domainExtService.getDomain(DEFAULT_DOMAIN);
            result = null;
        }};

        try {
            fsDomainService.verifyDomainExists(DEFAULT_DOMAIN);
        } catch (FSSetUpException ex) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testGetFSPluginDomainNonMultitenanncy() {

        new Expectations(fsDomainService) {{
            domainContextExtService.getCurrentDomain().getCode();
            result = DEFAULT_DOMAIN;
        }};

        final String fsPluginDomain = fsDomainService.getFSPluginDomain();
        Assert.assertEquals(FSSendMessagesService.DEFAULT_DOMAIN, fsPluginDomain);
    }

    @Test
    public void testGetFSPluginDomainMultitenanncy() {
        final String mydomain = "mydomain";

        new Expectations() {{
            domainContextExtService.getCurrentDomain().getCode();
            result = mydomain;
        }};

        final String fsPluginDomain = fsDomainService.getFSPluginDomain();
        Assert.assertEquals(mydomain, fsPluginDomain);
    }

    @Test
    public void fsDomainToDomibusDomainNonMultitenancyMode() {
        String fsPluginDomain = "myDomain";

        new Expectations() {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = false;

            domainExtService.getDomain("default");
            result = new DomainDTO("default", "default");
        }};

        final DomainDTO domainDTO = fsDomainService.fsDomainToDomibusDomain(fsPluginDomain);
        Assert.assertEquals(FSSendMessagesService.DEFAULT_DOMAIN, domainDTO.getCode());

        new Verifications() {{
            domainExtService.getDomain(fsPluginDomain);
            times = 0;
        }};
    }

    @Test
    public void fsDomainToDomibusDomainMultitenancyMode() {
        String fsPluginDomain = "myDomain";

        new Expectations() {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = true;

            domainExtService.getDomain(fsPluginDomain);
            result = new DomainDTO(fsPluginDomain, fsPluginDomain);
        }};

        final DomainDTO domainDTO = fsDomainService.fsDomainToDomibusDomain(fsPluginDomain);
        Assert.assertEquals(fsPluginDomain, domainDTO.getCode());

        new Verifications() {{
            domainExtService.getDomain("default");
            times = 0;
        }};
    }
}