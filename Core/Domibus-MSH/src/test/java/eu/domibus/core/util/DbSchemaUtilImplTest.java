package eu.domibus.core.util;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
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

    @Test
    public void givenDatabaseSchemaWhenDatabaseEngineIsOracleThenCorrespondingSqlIsGenerated() {
        //given
        String databaseSchema = "domibus";

        //when
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.ORACLE);
        String actual = dbSchemaUtilImpl.getSchemaChangeSQL(databaseSchema);

        //then
        Assert.assertEquals("ALTER SESSION SET CURRENT_SCHEMA = domibus", actual);
    }

    @Test
    public void givenDatabaseSchemaWhenDatabaseEngineIsNotOracleThenCorrespondingSqlIsGenerated() {
        //given
        String databaseSchema = "domibus";

        //when
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.MYSQL);
        String actual = dbSchemaUtilImpl.getSchemaChangeSQL(databaseSchema);

        //then
        Assert.assertEquals("USE domibus", actual);
    }

    @Test
    public void givenDomainWithValidDbSchemaWhenTestingValidityTrueShouldBeReturned() {
        //given
        Domain domain = new Domain("domain_2","domain_2");

        //when
        Mockito.when(domainService.getDatabaseSchema(domain)).thenReturn("domibus_domain_2");
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.MYSQL);
        Mockito.when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        Mockito.when(entityManager.getTransaction()).thenReturn(transaction);
        Mockito.when(entityManager.createNativeQuery("USE domibus_domain_2")).thenReturn(query);
        boolean actualResult = dbSchemaUtilImpl.isDatabaseSchemaForDomainValid(domain);

        //then
        Assert.assertEquals(true, actualResult);
    }

    @Test
    public void givenDomainWithFaultyDbSchemaWhenTestingValidityFalseShouldBeReturned() {
        //given
        Domain domain = new Domain("domain_2","domain_2");

        //when
        Mockito.when(domainService.getDatabaseSchema(domain)).thenReturn("domibus_domain_3");
        Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.MYSQL);
        Mockito.when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        Mockito.when(entityManager.getTransaction()).thenReturn(transaction);
        Mockito.when(entityManager.createNativeQuery("USE domibus_domain_3")).thenReturn(query);
        Mockito.when(query.executeUpdate()).thenThrow(PersistenceException.class);
        boolean actualResult = dbSchemaUtilImpl.isDatabaseSchemaForDomainValid(domain);

        //then
        Assert.assertEquals(false, actualResult);
    }

}
