package eu.domibus.core.util;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.FaultyDatabaseSchemaNameException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.scheduling.SchedulingTaskExecutor;

import javax.persistence.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_SCHEMA;

/**
 * @author Lucian FURCA
 * @since 5.1
 */
@RunWith(MockitoJUnitRunner.class)
public class DbSchemaUtilImplTest {

    private static final String DOMAIN = "domain";

    private static final String DOMAIN_DB_SCHEMA = "domibus_domain";

    private DbSchemaUtilImpl dbSchemaUtilImpl;

    @Mock
    private DataSource dataSource;

    @Mock
    private DomibusConfigurationService domibusConfigurationService;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Mock
    protected SchedulingTaskExecutor schedulingTaskExecutor;

    @Mock
    private EntityManager entityManager;

    @Mock
    private EntityTransaction transaction;

    @Mock
    private Query query;

    @Mock
    Map<Domain, String> domainSchemas;

    @Mock
    Connection connection;

    @Mock
    Statement statement;

    @Before
    public void init() {
        Mockito.when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        dbSchemaUtilImpl = new DbSchemaUtilImpl(dataSource, domibusConfigurationService,
                domibusPropertyProvider, schedulingTaskExecutor);
    }

    @Test
    public void givenDatabaseSchemaWhenDatabaseEngineIsOracleThenCorrespondingSqlIsGenerated() {
        //given
        String databaseSchema = DOMAIN_DB_SCHEMA;

        //when
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.ORACLE);
        String actual = dbSchemaUtilImpl.getSchemaChangeSQL(databaseSchema);

        //then
        Assert.assertEquals("ALTER SESSION SET CURRENT_SCHEMA = " + databaseSchema, actual);
    }

    @Test
    public void givenDatabaseSchemaWhenDatabaseEngineIsMySqlThenCorrespondingSqlIsGenerated() {
        //given
        String databaseSchema = DOMAIN_DB_SCHEMA;

        //when
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.MYSQL);
        String actual = dbSchemaUtilImpl.getSchemaChangeSQL(databaseSchema);

        //then
        Assert.assertEquals("USE " + databaseSchema, actual);
    }

    @Test
    public void givenDatabaseSchemaWhenDatabaseEngineIsH2ThenCorrespondingSqlIsGenerated() {
        //given
        String databaseSchema = DOMAIN_DB_SCHEMA;

        //when
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.H2);
        String actual = dbSchemaUtilImpl.getSchemaChangeSQL(databaseSchema);

        //then
        Assert.assertEquals("SET SCHEMA " + databaseSchema, actual);
    }

    @Test
    public void givenDomainWithValidDbSchemaWhenTestingOnMySqlValidityTrueShouldBeReturned() throws SQLException {
        Domain domain = new Domain(DOMAIN, DOMAIN);
        dbSchemaUtilImpl.domainSchemas = domainSchemas;

        //when
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(domainSchemas.get(domain)).thenReturn(DOMAIN_DB_SCHEMA);
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.MYSQL);
        Mockito.when(statement.execute("USE " + DOMAIN_DB_SCHEMA)).thenReturn(true);
        boolean actualResult = dbSchemaUtilImpl.doIsDatabaseSchemaForDomainValid(domain);

        //then
        Assert.assertTrue(actualResult);
    }

    @Test
    public void givenDomainWithValidDbSchemaWhenTestingOnH2ValidityTrueShouldBeReturned() throws SQLException {
        Domain domain = new Domain(DOMAIN, DOMAIN);
        dbSchemaUtilImpl.domainSchemas = domainSchemas;

        //when
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(domainSchemas.get(domain)).thenReturn(DOMAIN_DB_SCHEMA);
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.H2);
        Mockito.when(statement.execute("SET SCHEMA " + DOMAIN_DB_SCHEMA)).thenReturn(true);
        boolean actualResult = dbSchemaUtilImpl.doIsDatabaseSchemaForDomainValid(domain);

        //then
        Assert.assertTrue(actualResult);
    }

    @Test
    public void givenDomainWithValidDbSchemaWhenTestingOnOracleValidityTrueShouldBeReturned() throws SQLException {
        //given
        Domain domain = new Domain(DOMAIN, DOMAIN);
        dbSchemaUtilImpl.domainSchemas = domainSchemas;

        //when
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(domainSchemas.get(domain)).thenReturn(DOMAIN_DB_SCHEMA);
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.ORACLE);
        Mockito.when(statement.execute("ALTER SESSION SET CURRENT_SCHEMA = " + DOMAIN_DB_SCHEMA)).thenReturn(true);
        boolean actualResult = dbSchemaUtilImpl.doIsDatabaseSchemaForDomainValid(domain);

        //then
        Assert.assertTrue(actualResult);
    }

    @Test
    public void givenDomainWithFaultyDbSchemaWhenTestingValidityFalseShouldBeReturned() throws SQLException {
        //given
        Domain domain = new Domain(DOMAIN, DOMAIN);
        dbSchemaUtilImpl.domainSchemas = domainSchemas;

        //when
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(domainSchemas.get(domain)).thenReturn(DOMAIN_DB_SCHEMA);
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.MYSQL);
        Mockito.when(statement.execute("USE " + DOMAIN_DB_SCHEMA)).thenThrow(PersistenceException.class);
        boolean actualResult = dbSchemaUtilImpl.doIsDatabaseSchemaForDomainValid(domain);

        //then
        Assert.assertFalse(actualResult);
    }

    @Test(expected = FaultyDatabaseSchemaNameException.class)
    public void givenDomainWithDbSchemaNameThatFailsSanityCheckWhenTestingThenFaultyDatabaseSchemaNameExceptionShouldBeThrown() {
        String dbSchemaName = "default'; select * from tb_user";
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.valueOf("MYSQL"));
        dbSchemaUtilImpl.getSchemaChangeSQL(dbSchemaName);
    }

    @Test
    public void getDatabaseSchemaWhenItIsAlreadyCached(@Injectable Map<Domain, String> domainSchemas) {
        Domain defaultDomain = DomainService.DEFAULT_DOMAIN;
        dbSchemaUtilImpl.domainSchemas = domainSchemas;

        new Expectations() {{
            domainSchemas.get(defaultDomain);
            result = "defaultSchema";
        }};

        dbSchemaUtilImpl.getDatabaseSchema(defaultDomain);

        new Verifications() {{
            domibusPropertyProvider.getProperty(defaultDomain, DOMIBUS_DATABASE_SCHEMA);
            times = 0;
        }};
    }

    @Test
    public void getDatabaseSchema() {
        Domain defaultDomain = DomainService.DEFAULT_DOMAIN;
        Map<Domain, String> domainSchemas = new HashMap<>();
        dbSchemaUtilImpl.domainSchemas = domainSchemas;
        String defaultSchema = "defaultSchema";

        new Expectations(dbSchemaUtilImpl) {{
            dbSchemaUtilImpl.getDBSchemaFromPropertyFile(defaultDomain);
            result = defaultSchema;
        }};

        //first call puts the schema in the cache
        Assert.assertEquals(defaultSchema, dbSchemaUtilImpl.getDatabaseSchema(defaultDomain));

        //second call retrieves the schema in the cache
        Assert.assertEquals(defaultSchema, dbSchemaUtilImpl.getDatabaseSchema(defaultDomain));

        new Verifications() {{
            dbSchemaUtilImpl.getDBSchemaFromPropertyFile(defaultDomain);
            times = 1;
        }};
    }

    @Test
    public void getGeneralSchemaWhenItIsAlreadyCached() {
        String generalSchema = "generalSchema";
        dbSchemaUtilImpl.generalSchema = generalSchema;

        dbSchemaUtilImpl.getGeneralSchema();

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
        Assert.assertEquals(generalSchema, dbSchemaUtilImpl.getGeneralSchema());

        //second call retrieves the schema in the cache
        Assert.assertEquals(generalSchema, dbSchemaUtilImpl.getGeneralSchema());

        new Verifications() {{
            domibusPropertyProvider.getProperty(DomainService.GENERAL_SCHEMA_PROPERTY);
            times = 1;
        }};
    }

    @Test
    public void givenDbSchemaNameWithFaultyCharactersWhenSanityCheckingThenNameCheckShouldFail() {
        String dbSchemaName = "default' and select * from tb_user";
        Assert.assertFalse(dbSchemaUtilImpl.isDatabaseSchemaNameSane(dbSchemaName));
    }

    @Test
    public void givenDbSchemaNameInCorrectFormatWhenSanityCheckingThenCheckNameShouldSucceed() {
        String dbSchemaName = "blue_default_scheme";
        Assert.assertTrue(dbSchemaUtilImpl.isDatabaseSchemaNameSane(dbSchemaName));
    }
}
