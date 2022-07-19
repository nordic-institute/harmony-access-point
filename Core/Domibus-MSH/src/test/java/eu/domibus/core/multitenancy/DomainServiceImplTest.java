package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DbSchemaUtil;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.web.security.AuthenticationService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_SCHEMA;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DomainServiceImplTest {

    @Tested
    DomainServiceImpl domainService;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected DomainDao domainDao;

    @Injectable
    private DomibusCacheService domibusCacheService;

    @Injectable
    AuthUtils authUtils;

    @Injectable
    AuthenticationService authenticationService;

    @Injectable
    private DbSchemaUtil dbSchemaUtil;

    @Test
    public void getDatabaseSchemaWhenItIsAlreadyCached(@Injectable Map<Domain, String> domainSchemas) {
        Domain defaultDomain = DomainService.DEFAULT_DOMAIN;
        domainService.domainSchemas = domainSchemas;

        new Expectations() {{
            domainSchemas.get(defaultDomain);
            result = "defaultSchema";
        }};

        domainService.getDatabaseSchema(defaultDomain);

        new Verifications() {{
            domibusPropertyProvider.getProperty(defaultDomain, DOMIBUS_DATABASE_SCHEMA);
            times = 0;
        }};
    }

    @Test
    public void getDatabaseSchema() {
        Domain defaultDomain = DomainService.DEFAULT_DOMAIN;
        Map<Domain, String> domainSchemas = new HashMap<>();
        domainService.domainSchemas = domainSchemas;
        String defaultSchema = "defaultSchema";

        new Expectations() {{
            domibusPropertyProvider.getProperty(defaultDomain, DOMIBUS_DATABASE_SCHEMA);
            result = defaultSchema;
        }};

        //first call puts the schema in the cache
        Assert.assertEquals(defaultSchema, domainService.getDatabaseSchema(defaultDomain));

        //second call retrieves the schema in the cache
        Assert.assertEquals(defaultSchema, domainService.getDatabaseSchema(defaultDomain));

        new Verifications() {{
            domibusPropertyProvider.getProperty(defaultDomain, DOMIBUS_DATABASE_SCHEMA);
            times = 1;
        }};
    }

    @Test
    public void getGeneralSchemaWhenItIsAlreadyCached() {
        String generalSchema = "generalSchema";
        domainService.generalSchema = generalSchema;

        domainService.getGeneralSchema();

        new Verifications() {{
            domibusPropertyProvider.getProperty(DomainService.GENERAL_SCHEMA_PROPERTY);
            times = 0;
        }};
    }

    @Test
    public void getGeneralSchema() {
        String generalSchema = "generalSchema";

        new Expectations() {{
            domibusPropertyProvider.getProperty(DomainService.GENERAL_SCHEMA_PROPERTY);
            result = generalSchema;
        }};

        //first call puts the schema in the cache
        Assert.assertEquals(generalSchema, domainService.getGeneralSchema());

        //second call retrieves the schema in the cache
        Assert.assertEquals(generalSchema, domainService.getGeneralSchema());

        new Verifications() {{
            domibusPropertyProvider.getProperty(DomainService.GENERAL_SCHEMA_PROPERTY);
            times = 1;
        }};
    }
}
