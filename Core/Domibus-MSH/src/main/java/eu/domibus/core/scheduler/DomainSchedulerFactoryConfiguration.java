package eu.domibus.core.scheduler;

import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DbSchemaUtil;
import eu.domibus.core.alerts.job.AlertCleanerJob;
import eu.domibus.core.alerts.job.AlertRetryJob;
import eu.domibus.core.alerts.job.multitenancy.AlertCleanerSuperJob;
import eu.domibus.core.alerts.job.multitenancy.AlertRetrySuperJob;
import eu.domibus.core.certificate.SaveCertificateAndLogRevocationJob;
import eu.domibus.core.earchive.job.EArchivingCleanupJob;
import eu.domibus.core.earchive.job.EArchivingContinuousJob;
import eu.domibus.core.earchive.job.EArchivingSanitizerJob;
import eu.domibus.core.ebms3.sender.retry.SendRetryWorker;
import eu.domibus.core.error.ErrorLogCleanerJob;
import eu.domibus.core.message.UnsentMessageSanitizingWorker;
import eu.domibus.core.message.pull.MessagePullerJob;
import eu.domibus.core.message.pull.PullRetryWorker;
import eu.domibus.core.message.resend.MessageResendJob;
import eu.domibus.core.message.retention.OngoingMessagesSanitizingWorker;
import eu.domibus.core.message.retention.RetentionWorker;
import eu.domibus.core.message.splitandjoin.SplitAndJoinExpirationWorker;
import eu.domibus.core.monitoring.ConnectionMonitoringJob;
import eu.domibus.core.monitoring.ConnectionMonitoringSelfJob;
import eu.domibus.core.monitoring.DeleteReceivedTestMessageHistoryJob;
import eu.domibus.core.payload.temp.TemporaryPayloadCleanerJob;
import eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryLookupsJob;
import eu.domibus.core.user.multitenancy.ActivateSuspendedSuperUsersJob;
import eu.domibus.core.user.plugin.job.ActivateSuspendedPluginUsersJob;
import eu.domibus.core.user.ui.job.ActivateSuspendedUsersJob;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.DomibusEnvironmentUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Trigger;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static eu.domibus.common.TaskExecutorConstants.DOMIBUS_TASK_EXECUTOR_BEAN_NAME;

/**
 * @author Cosmin Baciu, Tiago Miguel
 * @since 4.0
 */
@Configuration
@DependsOn({"springContextProvider"})
public class DomainSchedulerFactoryConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainSchedulerFactoryConfiguration.class);

    public static final String GENERAL_SCHEDULER_NAME = "General";
    public static final String GROUP_GENERAL = "GENERAL";
    private static final Integer JOB_START_DELAY_IN_MS = 30_000;

    // Some lower priority jobs require prerequisite resources to be available upfront (e.g. a truststore that must
    // have already been persisted) and this takes a bit more time on a slow server: we ensure this is done by applying
    // a slightly longer delay of 5 minutes
    private static final Integer JOB_START_LONG_DELAY_IN_MS = 300_000;

    public static final String EARCHIVE_CONTINUOUS_JOB="eArchiveContinuousJob";
    public static final String EARCHIVE_CLEANUP_JOB="eArchivingCleanupJob";
    public static final String EARCHIVE_SANITIZER_JOB="eArchiveSanitizerJob";
    public static final String QUARTZ_JDBCJOBSTORE_STD_JDBCDELEGATE = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
    public static final String QUARTZ_JDBCJOBSTORE_WEBLOGIC_ORACLE_JDBCDELEGATE = "org.quartz.impl.jdbcjobstore.oracle.weblogic.WebLogicOracleDelegate";
    public static final String MESSAGE_RESEND_JOB="messageResendJob";

    @Autowired
    Environment environment;

    @Autowired
    @Qualifier(DOMIBUS_TASK_EXECUTOR_BEAN_NAME)
    protected Executor executor;

    @Autowired
    protected ApplicationContext applicationContext;

    @Qualifier(DataSourceConstants.DOMIBUS_JDBC_QUARTZ_DATA_SOURCE)
    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory;

    @Autowired
    protected DomainService domainService;

    @Autowired
    DbSchemaUtil dbSchemaUtil;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public SchedulerFactoryBean schedulerFactory(Optional<Domain> optionalDomain) {
        // Regardless of SCOPE_PROTOTYPE, Spring tries to create this bean without a Domain during singleton creation.
        // Spring 5.x throws when the argument is not present.
        // The solution is to make the argument optional and in case it is not present, the bean will not be created.
        if (!optionalDomain.isPresent()) {
            // General schema
            if (domibusConfigurationService.isMultiTenantAware()) {
                return schedulerFactoryGeneral();
            }
            return null;
        }

        // Domain
        Domain domain = optionalDomain.get();
        return schedulerFactoryDomain(domain);
    }

    //retention
    @Bean
    public JobDetailFactoryBean retentionWorkerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(RetentionWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean retentionWorkerTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }

        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(retentionWorkerJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean ongoingMessagesSanitizingWorkerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(OngoingMessagesSanitizingWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean ongoingMessagesSanitizingWorkerTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }

        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(ongoingMessagesSanitizingWorkerJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_ONGOING_MESSAGES_SANITIZING_WORKER_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    //retry
    @Bean
    public JobDetailFactoryBean retryWorkerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(SendRetryWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean retryWorkerTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }

        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(retryWorkerJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_MSH_RETRY_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean pullRetryWorkerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(PullRetryWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean pullRetryWorkerTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(pullRetryWorkerJob().getObject());
        String propertyValue = domibusPropertyProvider.getProperty(DOMIBUS_PULL_RETRY_CRON);
        obj.setCronExpression(propertyValue);
        LOG.debug("pullRetryWorkerTrigger configured with cronExpression [{}]", propertyValue);
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean pullRequestWorkerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(MessagePullerJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean pullRequestTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(pullRequestWorkerJob().getObject());
        String propertyValue = domibusPropertyProvider.getProperty(DOMIBUS_MSH_PULL_CRON);
        obj.setCronExpression(propertyValue);
        LOG.debug("pullRequestTrigger configured with cronExpression [{}]", propertyValue);
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean alertRetryJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(AlertRetryJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean alertRetryJSuperJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(AlertRetrySuperJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean alertRetryWorkerTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(alertRetryJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RETRY_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean alertRetrySuperWorkerTrigger() {
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(alertRetryJSuperJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RETRY_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        obj.setGroup(GROUP_GENERAL);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean alertCleanerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(AlertCleanerJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean alertCleanerSuperJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(AlertCleanerSuperJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean alertCleanerTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(alertCleanerJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CLEANER_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean alertSuperCleanerTrigger() {
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(alertCleanerSuperJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CLEANER_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        obj.setGroup(GROUP_GENERAL);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean temporaryPayloadRetentionJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(TemporaryPayloadCleanerJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean temporaryPayloadRetentionTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }

        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(temporaryPayloadRetentionJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean splitAndJoinExpirationJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(SplitAndJoinExpirationWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean splitAndJoinExpirationTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(splitAndJoinExpirationJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_SPLIT_AND_JOIN_RECEIVE_EXPIRATION_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean activateSuspendedUsersJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(ActivateSuspendedUsersJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean activateSuspendedUserTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(activateSuspendedUsersJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_ACCOUNT_UNLOCK_CRON));
        return obj;
    }

    @Bean
    public JobDetailFactoryBean activateSuspendedPluginUsersJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(ActivateSuspendedPluginUsersJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean activateSuspendedPluginUserTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(activateSuspendedPluginUsersJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_PLUGIN_ACCOUNT_UNLOCK_CRON));
        return obj;
    }

    @Bean
    public JobDetailFactoryBean activateSuspendedSuperUsersJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(ActivateSuspendedSuperUsersJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean activateSuspendedSuperUserTrigger() {
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(activateSuspendedSuperUsersJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_ACCOUNT_UNLOCK_CRON));
        obj.setGroup(GROUP_GENERAL);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean saveCertificateAndLogRevocationJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(SaveCertificateAndLogRevocationJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean saveCertificateAndLogRevocationTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(saveCertificateAndLogRevocationJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_CERTIFICATE_CHECK_CRON));
        obj.setStartDelay(JOB_START_LONG_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean connectionMonitoringJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(ConnectionMonitoringJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean connectionMonitoringTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(connectionMonitoringJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_MONITORING_CONNECTION_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean connectionMonitoringSelfJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(ConnectionMonitoringSelfJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean connectionMonitoringSelfTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(connectionMonitoringSelfJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_MONITORING_CONNECTION_SELF_CRON));
        obj.setStartDelay(JOB_START_LONG_DELAY_IN_MS);
        return obj;
    }

    // todo all these methods are similar: code can be reused
    // EDELIVERY-10150 Ion Perpegel 16/09/22
    @Bean
    public JobDetailFactoryBean deleteTestMessageHistoryJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(DeleteReceivedTestMessageHistoryJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean deleteTestMessageHistoryTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(deleteTestMessageHistoryJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_DELETE_RECEIVED_TEST_MESSAGE_HISTORY_CRON));
        obj.setStartDelay(JOB_START_LONG_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean errorLogCleanerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(ErrorLogCleanerJob.class);
        obj.setDurability(true);
        return obj;
    }


    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean errorLogCleanerTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(errorLogCleanerJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_ERRORLOG_CLEANER_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean eArchiveContinuousJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(EArchivingContinuousJob.class);
        obj.setDurability(true);
        return obj;
    }


    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean eArchiveContinuousTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(eArchiveContinuousJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean eArchiveSanitizerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(EArchivingSanitizerJob.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean eArchiveSanitizerTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null ) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(eArchiveSanitizerJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_SANITY_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean eArchivingCleanupJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(EArchivingCleanupJob.class);
        obj.setDurability(true);
        return obj;
    }


    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean eArchivingCleanupTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(eArchivingCleanupJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_RETENTION_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean stuckMessagesJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(UnsentMessageSanitizingWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean stuckMessagesTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }

        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(stuckMessagesJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_MESSAGES_STUCK_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }


    @Bean
    public JobDetailFactoryBean messageResendJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(MessageResendJob.class);
        obj.setDurability(true);
        return obj;
    }


    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean messageResendJobTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(messageResendJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_MESSAGE_RESEND_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean dynamicDiscoveryCertificatesCleanupJobTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null ) {
            return null;
        }
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(dynamicDiscoveryCertificatesCleanupJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_DYNAMICDISCOVERY_CLEAN_RETENTION_CRON));
        obj.setStartDelay(JOB_START_DELAY_IN_MS);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean dynamicDiscoveryCertificatesCleanupJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(DynamicDiscoveryLookupsJob.class);
        obj.setDurability(true);
        return obj;
    }

    /**
     * Sets the triggers only for general schema
     *
     * @return Scheduler Factory Bean changed
     */
    private SchedulerFactoryBean schedulerFactoryGeneral() {
        LOG.debug("Instantiating the scheduler factory for general schema");
        SchedulerFactoryBean schedulerFactoryBean = schedulerFactory(GENERAL_SCHEDULER_NAME, getGeneralSchemaPrefix(), null);

        final Map<String, Trigger> beansOfType = applicationContext.getBeansOfType(Trigger.class);
        List<Trigger> domibusStandardTriggerList = beansOfType.values().stream()
                .filter(trigger -> trigger instanceof CronTriggerImpl &&
                        ((CronTriggerImpl) trigger).getGroup().equalsIgnoreCase(GROUP_GENERAL))
                .collect(Collectors.toList());
        if (LOG.isDebugEnabled()) {
            for (Trigger trigger : domibusStandardTriggerList) {
                LOG.debug("Add trigger:[{}] to general scheduler factory", trigger);
            }
        }
        schedulerFactoryBean.setTriggers(domibusStandardTriggerList.toArray(new Trigger[domibusStandardTriggerList.size()]));

        return schedulerFactoryBean;
    }

    /**
     * Sets the triggers specific only for domain schema
     *
     * @param domain Domain
     * @return Scheduler Factory Bean changed
     */
    private SchedulerFactoryBean schedulerFactoryDomain(Domain domain) {
        domainContextProvider.setCurrentDomain(domain);

        LOG.debug("Instantiating the scheduler factory for domain [{}]", domain);
        SchedulerFactoryBean schedulerFactoryBean = schedulerFactory(domainService.getSchedulerName(domain), getTablePrefix(domain), domain);

        //get all the Spring Bean Triggers so that new instances with scope prototype are injected
        final Map<String, Trigger> beansOfType = applicationContext.getBeansOfType(Trigger.class);
        List<Trigger> domibusStandardTriggerList = beansOfType.values().stream()
                .filter(trigger -> !(trigger instanceof CronTriggerImpl) ||
                        !((CronTriggerImpl) trigger).getGroup().equalsIgnoreCase(GROUP_GENERAL))
                .collect(Collectors.toList());
        schedulerFactoryBean.setTriggers(domibusStandardTriggerList.toArray(new Trigger[domibusStandardTriggerList.size()]));

        return schedulerFactoryBean;
    }

    /**
     * Creates a new Scheduler Factory Bean based on schedulerName, tablePrefix and domain
     *
     * @param schedulerName Scheduler Name
     * @param tablePrefix   Table Prefix
     * @param domain        Domain
     * @return Scheduler Factory Bean
     */
    private SchedulerFactoryBean schedulerFactory(String schedulerName, String tablePrefix, Domain domain) {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setSchedulerName(schedulerName);
        scheduler.setTaskExecutor(executor);
        scheduler.setAutoStartup(false);
        scheduler.setApplicationContext(applicationContext);
        scheduler.setWaitForJobsToCompleteOnShutdown(true);
        scheduler.setOverwriteExistingJobs(true);
        scheduler.setDataSource(dataSource);
        scheduler.setTransactionManager(transactionManager);
        Properties properties = new Properties();
        properties.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", getQuartzDriverDelegateClass());
        properties.setProperty("org.quartz.jobStore.isClustered", domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_DEPLOYMENT_CLUSTERED));
        properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");
        properties.setProperty("org.quartz.jobStore.useProperties", "false");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        properties.setProperty("org.quartz.scheduler.jmx.export", "false");
        properties.setProperty("org.quartz.threadExecutor.class", DomibusQuartzThreadExecutor.class.getCanonicalName());
        // domibus uses TCCL - ThreadContextClassLoad to load plugins. Use InitThreadContextClassLoadHelper
        // to use the current init class loader which also loads the plugins. If you change this, check the
        // plugin execution on wildfly
        properties.setProperty("org.quartz.scheduler.classLoadHelper.class", "org.quartz.simpl.InitThreadContextClassLoadHelper");

        properties.setProperty("org.quartz.scheduler.instanceName", "general");
        if (StringUtils.isNotEmpty(tablePrefix)) {
            if (domain != null) {
                LOG.debug("Using the Quartz tablePrefix [{}] for domain [{}]", tablePrefix, domain);
            } else {
                LOG.debug("Using the Quartz tablePrefix [{}] for general schema", tablePrefix);
            }
            properties.setProperty("org.quartz.jobStore.tablePrefix", tablePrefix);
        }

        scheduler.setQuartzProperties(properties);
        scheduler.setJobFactory(autowiringSpringBeanJobFactory);

        return scheduler;
    }

    protected String getQuartzDriverDelegateClass() {
        String result = QUARTZ_JDBCJOBSTORE_STD_JDBCDELEGATE;
        if(DomibusEnvironmentUtil.INSTANCE.isWebLogic(environment) && domibusConfigurationService.getDataBaseEngine() == DataBaseEngine.ORACLE) {
            result = QUARTZ_JDBCJOBSTORE_WEBLOGIC_ORACLE_JDBCDELEGATE;
        }
        LOG.info("Using class [{}] for Quartz jdbcjobstore", result);

        return result;
    }

    /**
     * Returns the general schema prefix for QRTZ tables
     *
     * @return General schema prefix
     */
    protected String getGeneralSchemaPrefix() {
        if(domibusConfigurationService.isSingleTenantAware()) {
            throw new UnsupportedOperationException("There is no scheduling tables prefix for a general schema in single tenancy");
        }
        final String generalSchema = dbSchemaUtil.getGeneralSchema();
        if (StringUtils.isEmpty(generalSchema)) {
            throw new IllegalArgumentException("Could not get the general database schema");
        }
        return getSchemaPrefix(generalSchema);
    }

    /**
     * Returns the schema prefix for QRTZ tables for the domain
     *
     * @param domain Domain
     * @return Domain' schema prefix
     */
    protected String getTablePrefix(Domain domain) {
        if(domibusConfigurationService.isSingleTenantAware()) {
            LOG.debug("There is no scheduling tables prefix for a domain schema in single tenancy");
            return null;
        }
        final String databaseSchema = dbSchemaUtil.getDatabaseSchema(domain);
        if (StringUtils.isEmpty(databaseSchema)) {
            throw new IllegalArgumentException("Could not get the database schema for domain [" + domain + "]");
        }
        return getSchemaPrefix(databaseSchema);
    }

    /**
     * Returns the Schema prefix for a specific schema
     *
     * @param schema Schema
     * @return Schema prefix
     */
    private String getSchemaPrefix(String schema) {
        if (StringUtils.isEmpty(schema)) {
            throw new IllegalArgumentException("Could not get the scheduling tables prefix because the schema name is empty");
        }
        return schema + ".QRTZ_";
    }
}
