package eu.domibus.core.multitenancy.dao;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JMockit.class)
public class DomainDaoImplTest {

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Tested
    DomainDaoImpl domainDao;

    @Test
    public void findAll() {
        File f1 = new File("Zdomain-domibus.properties");
        File f2 = new File("Adomain-domibus.properties");

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            domibusConfigurationService.getConfigLocation();
            result = ".";
            domibusPropertyProvider.getProperty((Domain) any, anyString);
            returns("ZZZdomain", "AAAdomain");
        }};
        new Expectations(FileUtils.class) {{
            FileUtils.listFiles((File) any, (String[]) any, false);
            result = Arrays.asList(f1, f2);
        }};

        List<Domain> domains = domainDao.findAll();

        assertEquals(2, domains.size());
        assertEquals("adomain", domains.get(0).getCode());
        assertEquals("zdomain", domains.get(1).getCode());
    }

    @Test
    public void testValidateDomain_InvalidDomain(@Injectable Domain domain) {

        final String domainCode = "DomainA&7";
        List<Domain> domains = new ArrayList<>();

        try {
            domainDao.validateDomain(domains, domainCode);
            Assert.fail();
        } catch (DomibusCoreException ex) {
            assertEquals(ex.getError(), DomibusCoreErrorCode.DOM_001);
            assertEquals(ex.getMessage(), "[DOM_001]:Invalid domain name:domaina&7");
        }
    }

    @Test
    public void testValidateDomain_DuplicateDomain(@Injectable Domain domain) {

        final String domainCode1 = "domaina";
        final String domainCode = "DomainA";
        List<Domain> domains = new ArrayList<>();
        Domain domain1 = new Domain(domainCode1, null);
        domains.add(domain1);
        try {
            domainDao.validateDomain(domains, domainCode);
            Assert.fail();
        } catch (DomibusCoreException ex) {
            assertEquals(ex.getError(), DomibusCoreErrorCode.DOM_001);
            assertEquals(ex.getMessage(), "[DOM_001]:Found duplicate domain name :domaina");
        }
    }

    @Test
    public void testValidateDomain_ValidDomain(@Injectable Domain domain) {

        final String domainCode = "domain1";
        List<Domain> domains = new ArrayList<>();
       assertTrue(domainDao.validateDomain(domains, domainCode));
    }
}