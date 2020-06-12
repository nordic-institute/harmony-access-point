package eu.domibus.core.multitenancy;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.multitenancy.dao.DomainDao;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

public class DomainServiceImplTest {
    @Tested
    DomainServiceImpl domainService;

    @Injectable
    DomainDao domainDao;


    @Test
    public void testGetDatabaseSchemaAsNull(@Injectable Domain domain, @Injectable DomibusPropertyProvider domibusPropertyProvider) throws DomibusCoreException{

        final String DOMIBUS_DATABASE_SCHEMA = "domibus.database.schema";
        Domain domain1 = new Domain("domain1", "domain1");

        new Expectations() {{
            domibusPropertyProvider.getProperty(domain1, DOMIBUS_DATABASE_SCHEMA);
            result = null;
        }};

        try {
            domainService.getDatabaseSchema(domain1);
            Assert.fail();
        } catch (DomibusCoreException ex) {
            Assert.assertEquals(ex.getError(), DomibusCoreErrorCode.DOM_001);
            Assert.assertEquals(ex.getMessage(), "[DOM_001]:Database domain schema name cannot found for the property: domain1.domibus.database.schema");
        }
    }

    @Test
    public void testGetDatabaseSchema(@Injectable Domain domain, @Injectable DomibusPropertyProvider domibusPropertyProvider) throws DomibusCoreException{

        final String DOMIBUS_DATABASE_SCHEMA = "domibus.database.schema";
        Domain domain1 = new Domain("domain1", "domain1");

        new Expectations() {{
            domibusPropertyProvider.getProperty(domain1, DOMIBUS_DATABASE_SCHEMA);
            result = "domain1.domibus.database.schema";
        }};

        String databaseSchema = domainService.getDatabaseSchema(domain1);

        Assert.assertEquals(databaseSchema, "domain1.domibus.database.schema");
    }

}