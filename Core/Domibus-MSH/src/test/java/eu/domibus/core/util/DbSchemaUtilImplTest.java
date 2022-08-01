package eu.domibus.core.util;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.*;

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
    private DomainService domainService;

    @Mock
    private DomibusConfigurationService domibusConfigurationService;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private EntityManager entityManager;

    @Mock
    private EntityTransaction transaction;

    @Mock
    private Query query;

    @Before
    public void init() {
        Mockito.when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        dbSchemaUtilImpl = new DbSchemaUtilImpl(domainService, domibusConfigurationService, entityManagerFactory);
    }

    @Test
    public void givenDatabaseSchemaWhenDatabaseEngineIsOracleThenCorrespondingSqlIsGenerated() {
        //given
        String databaseSchema = DOMAIN_DB_SCHEMA;

        //when
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.ORACLE);
        String actual = dbSchemaUtilImpl.getSchemaChangeSQL(databaseSchema);

        //then
        Assert.assertEquals("ALTER SESSION SET CURRENT_SCHEMA = "+ databaseSchema, actual);
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
    public void givenDomainWithValidDbSchemaWhenTestingOnMySqlValidityTrueShouldBeReturned() {
        //given
        Domain domain = new Domain(DOMAIN,DOMAIN);

        //when
        Mockito.when(domainService.getDatabaseSchema(domain)).thenReturn(DOMAIN_DB_SCHEMA);
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.MYSQL);
        Mockito.when(entityManager.getTransaction()).thenReturn(transaction);
        Mockito.when(entityManager.createNativeQuery("USE " + DOMAIN_DB_SCHEMA)).thenReturn(query);
        boolean actualResult = dbSchemaUtilImpl.isDatabaseSchemaForDomainValid(domain);

        //then
        Assert.assertTrue(actualResult);
    }

    @Test
    public void givenDomainWithValidDbSchemaWhenTestingOnH2ValidityTrueShouldBeReturned() {
        //given
        Domain domain = new Domain(DOMAIN,DOMAIN);

        //when
        Mockito.when(domainService.getDatabaseSchema(domain)).thenReturn(DOMAIN_DB_SCHEMA);
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.H2);
        Mockito.when(entityManager.getTransaction()).thenReturn(transaction);
        Mockito.when(entityManager.createNativeQuery("SET SCHEMA " + DOMAIN_DB_SCHEMA)).thenReturn(query);
        boolean actualResult = dbSchemaUtilImpl.isDatabaseSchemaForDomainValid(domain);

        //then
        Assert.assertTrue(actualResult);
    }

    @Test
    public void givenDomainWithValidDbSchemaWhenTestingOnOracleValidityTrueShouldBeReturned() {
        //given
        Domain domain = new Domain(DOMAIN,DOMAIN);

        //when
        Mockito.when(domainService.getDatabaseSchema(domain)).thenReturn(DOMAIN_DB_SCHEMA);
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.ORACLE);
        Mockito.when(entityManager.getTransaction()).thenReturn(transaction);
        Mockito.when(entityManager.createNativeQuery("ALTER SESSION SET CURRENT_SCHEMA = " + DOMAIN_DB_SCHEMA)).thenReturn(query);
        boolean actualResult = dbSchemaUtilImpl.isDatabaseSchemaForDomainValid(domain);

        //then
        Assert.assertTrue(actualResult);
    }

    @Test
    public void givenDomainWithFaultyDbSchemaWhenTestingValidityFalseShouldBeReturned() {
        //given
        Domain domain = new Domain(DOMAIN,DOMAIN);

        //when
        Mockito.when(domainService.getDatabaseSchema(domain)).thenReturn(DOMAIN_DB_SCHEMA);
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.MYSQL);
        Mockito.when(entityManager.getTransaction()).thenReturn(transaction);
        Mockito.when(entityManager.createNativeQuery("USE " + DOMAIN_DB_SCHEMA)).thenReturn(query);
        Mockito.when(query.executeUpdate()).thenThrow(PersistenceException.class);
        boolean actualResult = dbSchemaUtilImpl.isDatabaseSchemaForDomainValid(domain);

        //then
        Assert.assertFalse(actualResult);
    }

}
