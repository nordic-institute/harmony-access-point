package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.plugin.fs.FSMessage;
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
 * @since 4, 1
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
    public void testGetFSPluginDomainNonMultitenanncy(@Injectable FSMessage fsMessage) {
        String service = "myservice";
        String action = "myaction";

        new Expectations(fsDomainService) {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = false;
        }};

        final String fsPluginDomain = fsDomainService.getFSPluginDomain();
        Assert.assertEquals(FSSendMessagesService.DEFAULT_DOMAIN, fsPluginDomain);
    }

    @Test
    public void testGetFSPluginDomainMultitenanncy(@Injectable FSMessage fsMessage) {
        final String mydomain = "mydomain";
        String service = "myservice";
        String action = "myaction";

        new Expectations() {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = true;

            domainContextExtService.getCurrentDomain().getCode();
            result = mydomain;
        }};

        final String fsPluginDomain = fsDomainService.getFSPluginDomain();
        Assert.assertEquals(mydomain, fsPluginDomain);
    }

    @Test
    public void getDomainPattern() {
    }

//    @Test
//    public void testResolveDomain_1() {
//        String serviceDomain1 = "ODRDocumentInvoiceService123";
//        String actionDomain1 = "PrintA";
//
//        final List<String> domains = new ArrayList<>();
//        domains.add("DOMAIN1");
//
//        new Expectations(1, fsDomainService) {{
//            fsPluginProperties.getDomainsOrdered();
//            result = domains;
//
//            fsPluginProperties.getExpression("DOMAIN1");
//            result = "ODRDocumentInvoiceService.*#Print.?";
//        }};
//
//        String result = fsDomainService.resolveFSPluginDomain(serviceDomain1, actionDomain1);
//        Assert.assertEquals("DOMAIN1", result);
//    }

//    @Test
//    public void testResolveDomain_2() {
//        String serviceDomain2 = "BRISReceptionService";
//        String actionDomain2 = "SendEmailAction";
//        String actionDomain2a = "ReceiveBillAction";
//
//        final List<String> domains = new ArrayList<>();
//        domains.add("DOMAIN1");
//        domains.add("DOMAIN2");
//
//        new Expectations(1, fsDomainService) {{
//            fsPluginProperties.getDomainsOrdered();
//            result = domains;
//
//            fsPluginProperties.getExpression("DOMAIN2");
//            result = "BRISReceptionService#.*";
//        }};
//
//        String result = fsDomainService.resolveFSPluginDomain(serviceDomain2, actionDomain2);
//        Assert.assertEquals("DOMAIN2", result);
//
//        result = fsDomainService.resolveFSPluginDomain(serviceDomain2, actionDomain2a);
//        Assert.assertEquals("DOMAIN2", result);
//    }

//    @Test
//    public void testResolveDomain_WithoutMatch() {
//        String serviceDomain1 = "ODRDocumentInvoiceService123";
//        String actionDomain1 = "PrintA";
//
//        String serviceWithoutMatch = "FSService123";
//        String actionWithoutMatch = "SomeAction";
//
//        final List<String> domains = new ArrayList<>();
//        domains.add("DOMAIN1");
//        domains.add("DOMAIN2");
//
//        new Expectations(1, fsDomainService) {{
//            fsPluginProperties.getDomainsOrdered();
//            result = domains;
//
//            fsPluginProperties.getExpression("DOMAIN1");
//            result = "ODRDocumentInvoiceService.*#Print.?";
//
//            fsPluginProperties.getExpression("DOMAIN2");
//            result = "BRISReceptionService#.*";
//        }};
//
//        String result = fsDomainService.resolveFSPluginDomain(serviceWithoutMatch, actionWithoutMatch);
//        Assert.assertNull(result);
//
//        result = fsDomainService.resolveFSPluginDomain(serviceDomain1, actionWithoutMatch);
//        Assert.assertNull(result);
//
//        result = fsDomainService.resolveFSPluginDomain(serviceWithoutMatch, actionDomain1);
//        Assert.assertNull(result);
//    }

//    @Test
//    public void testResolveDomain_bdxNoprocessTC1Leg1() {
//        String service = "bdx:noprocess";
//        String action = "TC1Leg1";
//
//        final List<String> domains = new ArrayList<>();
//        domains.add("DOMAIN1");
//
//        new Expectations(1, fsDomainService) {{
//            fsPluginProperties.getDomainsOrdered();
//            result = domains;
//
//            fsPluginProperties.getExpression("DOMAIN1");
//            result = "bdx:noprocess#TC1Leg1";
//        }};
//
//        String result = fsDomainService.resolveFSPluginDomain(service, action);
//        Assert.assertEquals("DOMAIN1", result);
//    }

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