package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyMetadataManager;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DomibusPropertyMetadataManagerImpl implements DomibusPropertyMetadataManager {

    /**
     * Returns all the properties that this PropertyProvider is able to handle, writable and read-only alike.
     *
     * @return a map
     * @implNote This list will be moved in the database eventually.
     */
    public Map<String, DomibusPropertyMetadata> getKnownProperties() {

        return Arrays.stream(new DomibusPropertyMetadata[]{
                //read-only properties
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.deployment.clustered"),
                new DomibusPropertyMetadata("domibus.security.key.private.password", false, true, false),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.database.general.schema"),
                new DomibusPropertyMetadata("domibus.database.schema", false, true, false),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.xa.xaDataSourceClassName"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.xa.maxLifetime"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.xa.minPoolSize"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.xa.maxPoolSize"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.xa.borrowConnectionTimeout"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.xa.reapTimeout"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.xa.maxIdleTime"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.xa.maintenanceInterval"),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.xa.property.user"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.xa.property.password"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.xa.property.url"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.xa.property.URL"),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.driverClassName"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.url"),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.user"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.password"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.maxLifetime"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.minPoolSize"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.datasource.maxPoolSize"),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.entityManagerFactory.packagesToScan"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.entityManagerFactory.jpaProperty.hibernate.connection.driver_class"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.entityManagerFactory.jpaProperty.hibernate.dialect"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.entityManagerFactory.jpaProperty.hibernate.id.new_generator_mappings"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.entityManagerFactory.jpaProperty.hibernate.format_sql"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.entityManagerFactory.jpaProperty.hibernate.transaction.factory_class"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.entityManagerFactory.jpaProperty.hibernate.transaction.jta.platform"),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.password.encryption.active"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.password.encryption.properties"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.password.encryption.key.location"),


                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.jms.queue.pull"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.jms.queue.ui.replication"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.jms.XAConnectionFactory.maxPoolSize"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.jms.queue.alert"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.taskExecutor.threadCount"),  //move the use=age from xml ?

                DomibusPropertyMetadata.getReadOnlyGlobalProperty("com.atomikos.icatch.output_dir"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("com.atomikos.icatch.log_base_dir"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("com.atomikos.icatch.default_jta_timeout"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("com.atomikos.icatch.max_timeout"), //move the use=age from xml ?

                DomibusPropertyMetadata.getReadOnlyGlobalProperty("activeMQ.broker.host"), //cannot find the usage
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("activeMQ.brokerName"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("activeMQ.embedded.configurationFile"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("activeMQ.JMXURL"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("activeMQ.connectorPort"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("activeMQ.rmiServerPort"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("activeMQ.transportConnector.uri"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("activeMQ.username"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("activeMQ.password"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("activeMQ.persistent"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("activeMQ.connection.closeTimeout"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("activeMQ.connection.connectResponseTimeout"), //move the use=age from xml ?

                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.alert.queue.concurrency"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("messageFactoryClass"), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("compressionBlacklist"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.internal.queue.concurency"), //move the use=age from xml ?

                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.metrics.jmx.reporter.enable"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.metrics.sl4j.reporter.enable"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.metrics.sl4j.reporter.period.time.unit"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.metrics.sl4j.reporter.period.number"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.metrics.monitor.memory"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.metrics.monitor.gc"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.metrics.monitor.cached.threads"),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty("domibus.metrics.monitor.jms.queues"),

                //writable properties
                new DomibusPropertyMetadata(DOMIBUS_UI_TITLE_NAME, true, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_REPLICATION_ENABLED, true, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_NAME, true, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_EMAIL, true, true),

                new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEYSTORE_LOCATION, true, false),
                new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEYSTORE_TYPE, true, false),
                new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEYSTORE_PASSWORD, true, false),
                new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS, true, false),

                new DomibusPropertyMetadata(DOMIBUS_SECURITY_TRUSTSTORE_LOCATION, true, false),
                new DomibusPropertyMetadata(DOMIBUS_SECURITY_TRUSTSTORE_TYPE, true, false),
                new DomibusPropertyMetadata(DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD, true, false),

                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_AUTH_UNSECURE_LOGIN_ALLOWED),
                new DomibusPropertyMetadata(DOMIBUS_CONSOLE_LOGIN_MAXIMUM_ATTEMPT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_CONSOLE_LOGIN_SUSPENSION_TIME, true, true),
                new DomibusPropertyMetadata(DOMIBUS_CERTIFICATE_REVOCATION_OFFSET, true, false),
                new DomibusPropertyMetadata(DOMIBUS_CERTIFICATE_CRL_EXCLUDED_PROTOCOLS, true, false),

                new DomibusPropertyMetadata(DOMIBUS_PLUGIN_LOGIN_MAXIMUM_ATTEMPT, true, false),
                new DomibusPropertyMetadata(DOMIBUS_PLUGIN_LOGIN_SUSPENSION_TIME, true, true),

                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PATTERN, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_VALIDATION_MESSAGE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_EXPIRATION, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_DEFAULT_PASSWORD_EXPIRATION, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_WARNING_BEFORE_EXPIRATION, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_DONT_REUSE_LAST, true, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD),

                new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICY_PATTERN, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICY_VALIDATION_MESSAGE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_EXPIRATION, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_DEFAULT_PASSWORD_EXPIRATION, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_DONT_REUSE_LAST, true, true),

                new DomibusPropertyMetadata(DOMIBUS_ATTACHMENT_STORAGE_LOCATION, true, false),
                new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_ENCRYPTION_ACTIVE, true, true),

                new DomibusPropertyMetadata(DOMIBUS_MSH_MESSAGEID_SUFFIX, true, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_MSH_RETRY_MESSAGE_EXPIRATION_DELAY),

                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, true, true),
                new DomibusPropertyMetadata(DOMIBUS_SMLZONE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_MODE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_PARTYID_RESPONDER_ROLE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_PARTYID_TYPE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4, true, true),

                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_LIST_PENDING_MESSAGES_MAX_COUNT),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_JMS_QUEUE_MAX_BROWSE_SIZE), //there is one place at init time that it is not refreshed
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_JMS_INTERNAL_QUEUE_EXPRESSION),

                new DomibusPropertyMetadata(DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING, true, true),
                new DomibusPropertyMetadata(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING, true, true),
                new DomibusPropertyMetadata(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING, true, true),
                new DomibusPropertyMetadata(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING, true, true),
                new DomibusPropertyMetadata(DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION, true, false),
                new DomibusPropertyMetadata(DOMIBUS_SENDER_CERTIFICATE_SUBJECT_CHECK, true, true),
                new DomibusPropertyMetadata(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN, true, false),

                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CONNECTION_TIMEOUT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_RECEIVE_TIMEOUT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_ALLOW_CHUNKING, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CHUNKING_THRESHOLD, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CONCURENCY, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_LARGE_FILES_CONCURRENCY, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CACHEABLE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CONNECTION_KEEP_ALIVE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_RETENTION_JMS_CONCURRENCY, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCH_EBMS_ERROR_UNRECOVERABLE_RETRY, true, true),

                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_ENABLED),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_HTTP_HOST),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_HTTP_PORT),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_USER),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_PASSWORD),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_NON_PROXY_HOSTS),

                new DomibusPropertyMetadata(DOMIBUS_UI_REPLICATION_SYNC_CRON_MAX_ROWS, true, true), //there is still one call from xml!!!
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PLUGIN_NOTIFICATION_ACTIVE),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_NONREPUDIATION_AUDIT_ACTIVE),
                new DomibusPropertyMetadata(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD, true, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_SEND_MESSAGE_ATTEMPT_AUDIT_ACTIVE),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_FOURCORNERMODEL_ENABLED),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_LOGGING_PAYLOAD_PRINT),     //there are still usages in xml!!!! move them?
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_LOGGING_CXF_LIMIT),         //there are still usages in xml!!!! move them?

                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ATTACHMENT_TEMP_STORAGE_LOCATION),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_CONCURRENCY, true, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_PAYLOADS_SCHEDULE_THRESHOLD, true, true),

                new DomibusPropertyMetadata(DOMAIN_TITLE, true, false),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_USER_INPUT_BLACK_LIST),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_USER_INPUT_WHITE_LIST),

                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ACCOUNT_UNLOCK_CRON),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_CERTIFICATE_CHECK_CRON),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PLUGIN_ACCOUNT_UNLOCK_CRON),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICIES_CHECK_CRON, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICIES_CHECK_CRON, true, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_CRON),
                new DomibusPropertyMetadata(DOMIBUS_MSH_RETRY_CRON, true, true),
                new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION, true, true),
                new DomibusPropertyMetadata(DOMIBUS_MSH_PULL_CRON, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_RETRY_CRON, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CLEANER_CRON, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_RETRY_CRON, true, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_CLEANER_CRON),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_RETRY_CRON),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_UI_REPLICATION_SYNC_CRON),
                new DomibusPropertyMetadata(DOMIBUS_SPLIT_AND_JOIN_RECEIVE_EXPIRATION_CRON, true, true),

                new DomibusPropertyMetadata(DOMIBUS_ALERT_ACTIVE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE, true, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_MAIL_SMTP_TIMEOUT),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_URL),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_PORT),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_USER),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_PASSWORD),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_SENDER_EMAIL, true, false),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_RECEIVER_EMAIL, true, false),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_RETRY_TIME, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS, true, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_CLEANER_ALERT_LIFETIME),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_ACTIVE),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_MAIL_SENDING_ACTIVE),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_RETRY_TIME),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_RETRY_MAX_ATTEMPTS),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_SENDER_EMAIL),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_RECEIVER_EMAIL),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT, true, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_USER_LOGIN_FAILURE_ACTIVE),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_USER_LOGIN_FAILURE_LEVEL),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_USER_LOGIN_FAILURE_MAIL_SUBJECT),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_ACTIVE),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_LEVEL),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_MOMENT),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SUPER_USER_ACCOUNT_DISABLED_SUBJECT),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_LEVEL, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_ACTIVE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_DELAY_DAYS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_FREQUENCY_DAYS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_LEVEL, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_MAIL_SUBJECT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_ACTIVE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_DELAY_DAYS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_FREQUENCY_DAYS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_LEVEL, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_MAIL_SUBJECT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_ACTIVE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_DELAY_DAYS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_FREQUENCY_DAYS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_LEVEL, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_MAIL_SUBJECT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_ACTIVE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_DELAY_DAYS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_FREQUENCY_DAYS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_LEVEL, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_MAIL_SUBJECT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_ACTIVE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_LEVEL, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_MAIL_SUBJECT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_ACTIVE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_LEVEL, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_MOMENT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_SUBJECT, true, true),

                new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_FREQUENCY_RECOVERY_TIME, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_FREQUENCY_ERROR_COUNT, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_DYNAMIC_INITIATOR, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_MULTIPLE_LEGS, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_FORCE_BY_MPC, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_MPC_INITIATOR_SEPARATOR, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_RECEIPT_QUEUE_CONCURRENCY, true, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_QUEUE_CONCURENCY, true, true),

                new DomibusPropertyMetadata(DOMIBUS_EXTENSION_IAM_AUTHENTICATION_IDENTIFIER, true, true),
                new DomibusPropertyMetadata(DOMIBUS_EXTENSION_IAM_AUTHORIZATION_IDENTIFIER, true, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_EXCEPTIONS_REST_ENABLE),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXCLUDE_REGEX),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXPIRATION),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_DIRECTORIES),

                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_INSTANCE_NAME),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

    public boolean hasKnownProperty(String name) {
        return this.getKnownProperties().containsKey(name);
    }

}
