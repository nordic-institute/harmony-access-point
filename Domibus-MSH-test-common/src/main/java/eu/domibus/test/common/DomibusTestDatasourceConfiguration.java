package eu.domibus.test.common;

import com.zaxxer.hikari.HikariDataSource;
import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_SCHEMA;
import static org.junit.Assert.fail;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class DomibusTestDatasourceConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusTestDatasourceConfiguration.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Bean
    public AnnotationConfigWebApplicationContext annotationConfigWebApplicationContext() {
        return new AnnotationConfigWebApplicationContext();
    }

    @Primary
    @Bean(name = {DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE, DataSourceConstants.DOMIBUS_JDBC_QUARTZ_DATA_SOURCE}, destroyMethod = "close")
    public DataSource domibusDatasource() {
        HikariDataSource dataSource = createDataSource();
        return dataSource;
    }

    private HikariDataSource createDataSource() {
        JdbcDataSource h2DataSource = createH2Datasource();

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl(h2DataSource.getUrl());
        dataSource.setUsername(h2DataSource.getUser());
        dataSource.setPassword(h2DataSource.getPassword());
        dataSource.setAutoCommit(false);

        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.setMaxLifetime(5 * 1000L);
        dataSource.setConnectionTimeout(5 * 1000L);
        dataSource.setIdleTimeout(5 * 1000L);
        dataSource.setMaximumPoolSize(100);
        return dataSource;
    }

    private JdbcDataSource createH2Datasource() {
        JdbcDataSource result = new JdbcDataSource();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        String createSchemaScriptFullPath = writeScriptFromClasspathToLocalDirectory("create_schema.sql", "config/database");
        String domibusH2ScriptFullPath = writeScriptFromClasspathToLocalDirectory("domibus-h2.sql", "test-sql-scripts");
        String domibusH2DataScriptFullPath = writeScriptFromClasspathToLocalDirectory("domibus-h2-data.sql", "test-sql-scripts");
        String schemaH2ScriptFullPath = writeScriptFromClasspathToLocalDirectory("schema-h2.sql", "config/database");

        final String databaseSchema = domibusPropertyProvider.getProperty(DOMIBUS_DATABASE_SCHEMA);
        //Enable logs for H2 with ';TRACE_LEVEL_FILE=4' at the end of databaseUrlTemplate
        final String databaseUrlTemplate = "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=DAY,VALUE;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_LOCK_TIMEOUT=3000;INIT=runscript from '" + createSchemaScriptFullPath + "'\\;runscript from '" + domibusH2ScriptFullPath + "'\\;runscript from '" + domibusH2DataScriptFullPath + "'\\;runscript from '" + schemaH2ScriptFullPath + "'";
        String databaseUrl = String.format(databaseUrlTemplate, databaseSchema);

        LOG.info("Using database URL [{}]", databaseUrl);

        result.setUrl(databaseUrl);
        result.setUser("sa");
        result.setPassword("");
        return result;
    }

    private String writeScriptFromClasspathToLocalDirectory(String scriptName, String scriptDirectory) {
        String sourceScriptPath = scriptDirectory + "/" + scriptName;

        final File testSqlScriptsDirectory = new File("target/test-sql-scripts");
        final File domibusScript = new File(testSqlScriptsDirectory, scriptName);
        String scriptFullPath= null;
        try {
            scriptFullPath = domibusScript.getCanonicalPath();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            fail("Could not the full path for script [" + domibusScript + "]");
        }


        try (InputStream inputStream = new ClassPathResource(sourceScriptPath).getInputStream()) {
            LOG.debug("Database: Writing file [{}]", domibusScript);
            final byte[] data = IOUtils.toByteArray(inputStream);
            FileUtils.writeByteArrayToFile(domibusScript, data);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            fail("Could not write script from classpath [" + sourceScriptPath + "] to the local file [" + domibusScript + "]");
        }
        return scriptFullPath;
    }


}
