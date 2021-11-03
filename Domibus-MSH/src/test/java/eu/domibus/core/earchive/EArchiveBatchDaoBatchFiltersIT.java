package eu.domibus.core.earchive;

import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.spring.SpringContextProvider;
import eu.domibus.common.JPAConstants;
import eu.domibus.core.jpa.DomibusJPAConfiguration;
import eu.domibus.core.util.DatabaseUtilImpl;
import eu.domibus.test.common.DomibusTestDatasourceConfiguration;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.H2Dialect;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

import static eu.domibus.api.earchive.EArchiveBatchStatus.*;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_SCHEMA;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Test filter variations for querying the EArchiveBatchEntities
 * Because we are using @RunWith(value = Parameterized.class) the following rule ensures startup of the spring test context
 * - SpringMethodRule provides the instance-level and method-level functionality for TestContextManager.
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Transactional
@RunWith(value = Parameterized.class)
@ContextConfiguration(initializers = EArchiveBatchDaoBatchFiltersIT.PropertyOverrideContextInitializer.class,
        classes = {DomibusTestDatasourceConfiguration.class,
                EArchiveBatchDaoBatchFiltersIT.TestConfiguration.class,
                SpringContextProvider.class,
                DatabaseUtilImpl.class,
                EArchiveBatchDao.class
        })
@EnableTransactionManagement
public class EArchiveBatchDaoBatchFiltersIT {
    private static final String TEST_ENV_DATABASE_SCHEMA = "testdb";
    private static final String TEST_ENV_DOMIBUS_CONFIG_FOLDER = "target/test-classes";
    private static final String[] TEST_ENV_DOMIBUS_DB_ENTITIES_PACKAGE = new String[]{
            "eu.domibus.core.earchive",
            "eu.domibus.api.model"
    };

    /**
     * Set system properties
     */
    public static class PropertyOverrideContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            ConfigurableEnvironment configurableEnvironment = configurableApplicationContext.getEnvironment();
            configurableEnvironment.getSystemProperties().put("domibus.config.location", TEST_ENV_DOMIBUS_CONFIG_FOLDER);
            configurableEnvironment.getSystemProperties().put("domibus.database.schema", TEST_ENV_DATABASE_SCHEMA);
        }
    }

    /**
     * Set minimal test environment configuration
     */
    @Configuration
    @EnableTransactionManagement
    public static class TestConfiguration {

        @Bean
        public DomibusPropertyProvider beanDomibusPropertyProvider() {
            DomibusPropertyProvider propertyProvider = Mockito.mock(DomibusPropertyProvider.class);
            Mockito.when(propertyProvider.getProperty(DOMIBUS_DATABASE_SCHEMA)).thenReturn(TEST_ENV_DATABASE_SCHEMA);
            return propertyProvider;
        }

        @Bean
        public DomibusConfigurationService beanDomibusConfigurationService() {
            DomibusConfigurationService domibusConfigurationService = Mockito.mock(DomibusConfigurationService.class);
            Mockito.when(domibusConfigurationService.getDataBaseEngine()).thenReturn(DataBaseEngine.getDatabaseEngine(H2Dialect.class.getName()));
            return domibusConfigurationService;
        }

        // setup database configuration
        @Bean
        @DependsOn({DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE})
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource h2DataSource) {

            LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
            lef.setPersistenceUnitName(JPAConstants.PERSISTENCE_UNIT_NAME);
            lef.setDataSource(h2DataSource);
            lef.setJpaVendorAdapter(new HibernateJpaVendorAdapter() {{
                // change this to see sql on hibernate level for this tests
                // another option is to modify h2 database configuration in DomibusTestDatasourceConfiguration
                setShowSql(true);
            }});
            // scan for entities
            lef.setPackagesToScan(TEST_ENV_DOMIBUS_DB_ENTITIES_PACKAGE);
            lef.setJpaProperties(new Properties() {{
                setProperty(Environment.JDBC_TIME_ZONE, DomibusJPAConfiguration.JPA_PROPERTY_TIMEZONE_UTC);
                setProperty("hibernate.connection.driver_class", "org.h2.Driver");
                setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                setProperty("hibernate.id.new_generator_mappings", "false");
            }});
            return lef;
        }

        @Bean
        public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            final JpaTransactionManager transactionManager = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(emf);
            return transactionManager;
        }
    }


    private static final String BATCH_ID_01 = "BATCH_ID_01@"+UUID.randomUUID().toString();

    private static final String BATCH_ID_02 = "BATCH_ID_02@"+UUID.randomUUID().toString();
    private static final String BATCH_ID_03 = "BATCH_ID_03@"+UUID.randomUUID().toString();
    private static final String BATCH_ID_04 = "BATCH_ID_04@"+UUID.randomUUID().toString();


    @Parameterized.Parameters(name = "{index}: {0}")
    // test desc. result batchIds, filter
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {"With filter status queued ", singletonList(BATCH_ID_04), 1L, new EArchiveBatchFilter(singletonList(QUEUED), null, null, null, null, null, null, null)},
                {"With filter status exported ", singletonList(BATCH_ID_02), 1L, new EArchiveBatchFilter(singletonList(EXPORTED), null, null, null, null, null, null, null)},
                {"With filter status exported and reexported ", asList(BATCH_ID_02), 1L, new EArchiveBatchFilter(asList(EXPORTED), null, null, null, null, null, null, null)},
                {"With filter by type", asList(BATCH_ID_02), 1L, new EArchiveBatchFilter(asList(EXPORTED), singletonList(EArchiveRequestType.MANUAL), null, null, null, null, null, null)},
                // Note batches are ordered from latest to oldest
                {"With filter: request date", asList(BATCH_ID_04, BATCH_ID_03, BATCH_ID_02), 3L, new EArchiveBatchFilter(null, null, DateUtils.addDays(Calendar.getInstance().getTime(), -28), DateUtils.addDays(Calendar.getInstance().getTime(), -12), null, null, null, null)},
                {"With filter: get All ", asList(BATCH_ID_04, BATCH_ID_03, BATCH_ID_02, BATCH_ID_01), 4L, new EArchiveBatchFilter(null, null, null, null, null, null, null, null)},
                {"With filter: test page size", asList(BATCH_ID_04, BATCH_ID_03), 4L, new EArchiveBatchFilter(null, null, null, null, null, null, null, 2)},
                {"With filter: test page start", asList(BATCH_ID_02, BATCH_ID_01), 4L, new EArchiveBatchFilter(null, null, null, null, null, null, 1, 2)},
        });
    }

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();


    @Autowired
    EArchiveBatchDao eArchiveBatchDao;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;

    String testName;
    List<String> expectedBatchIds;
    Long total;
    EArchiveBatchFilter filter;

    public EArchiveBatchDaoBatchFiltersIT(String testName, List<String> expectedBatchIds, Long total, EArchiveBatchFilter filter) {
        this.testName = testName;
        this.expectedBatchIds = expectedBatchIds;
        this.total = total;
        this.filter = filter;
    }

    @Before
    @Transactional
    public void setup() {
        Date currentDate = Calendar.getInstance().getTime();
        // prepare database -> create batches
        create(BATCH_ID_01, DateUtils.addDays(currentDate, -30), 1L, 10L, EArchiveRequestType.CONTINUOUS, ARCHIVED);
        create(BATCH_ID_02, DateUtils.addDays(currentDate, -25), 11L, 20L, EArchiveRequestType.MANUAL, EXPORTED);
        create(BATCH_ID_03, DateUtils.addDays(currentDate, -22), 21L, 30L, EArchiveRequestType.MANUAL, EXPIRED);
        create(BATCH_ID_04, DateUtils.addDays(currentDate, -15), 31L, 40L, EArchiveRequestType.CONTINUOUS, QUEUED);
    }

    private EArchiveBatchEntity create(String batchId, Date dateRequested, Long firstPkUserMessage, Long lastPkUserMessage, EArchiveRequestType continuous, EArchiveBatchStatus status) {
        EArchiveBatchEntity batch = new EArchiveBatchEntity();
        batch.setBatchId(batchId);
        batch.setDateRequested(dateRequested);
        batch.setFirstPkUserMessage(firstPkUserMessage);
        batch.setLastPkUserMessage(lastPkUserMessage);
        batch.setRequestType(continuous);
        batch.seteArchiveBatchStatus(status);
        batch.setMessageIdsJson("{\"messageId\":\"" + UUID.randomUUID().toString() + "\"}");
        return eArchiveBatchDao.merge(batch);
    }

    @Test
    public void testGetBatchRequestListWithoutClob() {
        // given-when
        List<EArchiveBatchSummaryEntity> resultList = eArchiveBatchDao.getBatchRequestList(filter, EArchiveBatchSummaryEntity.class);
        // then
        Assert.assertEquals(expectedBatchIds.size(), resultList.size());
        Assert.assertArrayEquals(expectedBatchIds.toArray(), resultList.stream().map(eArchiveBatchEntity -> eArchiveBatchEntity.getBatchId()).collect(Collectors.toList()).toArray());
    }

    @Test
    public void testGetBatchRequestListWithClob() {
        // given-when
        List<EArchiveBatchEntity> resultList = eArchiveBatchDao.getBatchRequestList(filter, EArchiveBatchEntity.class);
        // then
        Assert.assertEquals(expectedBatchIds.size(), resultList.size());
        Assert.assertArrayEquals(expectedBatchIds.toArray(), resultList.stream().map(eArchiveBatchEntity -> eArchiveBatchEntity.getBatchId()).collect(Collectors.toList()).toArray());
        resultList.forEach(eArchiveBatchEntity -> Assert.assertNotNull(eArchiveBatchEntity.getMessageIdsJson()));
    }

    @Test
    public void testGetBatchRequestListCount() {
        // given-when
        Long count = eArchiveBatchDao.getBatchRequestListCount(filter);
        // then
        Assert.assertEquals(total, count);
    }
}