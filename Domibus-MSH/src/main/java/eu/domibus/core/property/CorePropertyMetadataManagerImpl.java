package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.Module;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Property metadata holder class of core properties ( common to all servers)
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class CorePropertyMetadataManagerImpl implements DomibusPropertyMetadataManagerSPI {

    private Map<String, DomibusPropertyMetadata> knownProperties = Arrays.stream(new DomibusPropertyMetadata[]{
            //read-only properties
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_CONFIG_LOCATION),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DEPLOYMENT_CLUSTERED, DomibusPropertyMetadata.Type.BOOLEAN),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD, false, DomibusPropertyMetadata.Usage.DOMAIN, false, true),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATABASE_GENERAL_SCHEMA),
            new DomibusPropertyMetadata(DOMIBUS_DATABASE_SCHEMA, false, DomibusPropertyMetadata.Usage.DOMAIN, false),

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_PROPERTY_USER),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_PROPERTY_PASSWORD, true),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_PROPERTY_url),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_PROPERTY_URL),

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_DRIVER_CLASS_NAME),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_URL),

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_USER),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_PASSWORD, true),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_MAX_LIFETIME, DomibusPropertyMetadata.Type.NUMERIC),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_MIN_POOL_SIZE, DomibusPropertyMetadata.Type.NUMERIC),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_MAX_POOL_SIZE, DomibusPropertyMetadata.Type.NUMERIC),

            new DomibusPropertyMetadata(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY, DomibusPropertyMetadata.Type.STRING, Module.MSH, false, DomibusPropertyMetadata.Usage.GLOBAL, false, false, false, true),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_PACKAGES_TO_SCAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_CONNECTION_DRIVER_CLASS),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_DIALECT),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_ID_NEW_GENERATOR_MAPPINGS, DomibusPropertyMetadata.Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_FORMAT_SQL),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_TRANSACTION_FACTORY_CLASS),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_TRANSACTION_JTA_PLATFORM),

            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_ENCRYPTION_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, false, DomibusPropertyMetadata.Usage.GLOBAL_AND_DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_ENCRYPTION_PROPERTIES, false, DomibusPropertyMetadata.Usage.GLOBAL_AND_DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION, false, DomibusPropertyMetadata.Usage.GLOBAL_AND_DOMAIN, true),

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_CONNECTION_FACTORY_SESSION_CACHE_SIZE),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_QUEUE_PULL),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_QUEUE_UI_REPLICATION), //move the use=age from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_UI_REPLICATION_QUEUE_CONCURENCY, DomibusPropertyMetadata.Type.CONCURRENCY), //move the use=age from xml ?

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_QUEUE_ALERT), //move the use=age from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_TASK_EXECUTOR_THREAD_COUNT, DomibusPropertyMetadata.Type.NUMERIC),  //move the use=age from xml ?

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ALERT_QUEUE_CONCURRENCY, DomibusPropertyMetadata.Type.CONCURRENCY), //move the use=age from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(MESSAGE_FACTORY_CLASS), //move the use=age from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(COMPRESSION_BLACKLIST),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_INTERNAL_QUEUE_CONCURENCY, DomibusPropertyMetadata.Type.CONCURRENCY), //move the use=age from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_INTERNAL_COMMAND_CONCURENCY, DomibusPropertyMetadata.Type.CONCURRENCY),

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_JMX_REPORTER_ENABLE, DomibusPropertyMetadata.Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_ENABLE, DomibusPropertyMetadata.Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_TIME_UNIT),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_NUMBER, DomibusPropertyMetadata.Type.NUMERIC),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_MEMORY, DomibusPropertyMetadata.Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_GC, DomibusPropertyMetadata.Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_CACHED_THREADS, DomibusPropertyMetadata.Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES, DomibusPropertyMetadata.Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES_REFRESH_PERIOD),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES_SHOW_DLQ_ONLY, DomibusPropertyMetadata.Type.BOOLEAN),

            new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE_PER_MPC, DomibusPropertyMetadata.Type.NUMERIC, Module.MSH, false, DomibusPropertyMetadata.Usage.DOMAIN, true, true, false, true),

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_SECURITY_EXT_AUTH_PROVIDER_ENABLED, DomibusPropertyMetadata.Type.BOOLEAN),


            //writable properties
            new DomibusPropertyMetadata(DOMIBUS_UI_TITLE_NAME, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_UI_REPLICATION_ENABLED, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_NAME, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_EMAIL, DomibusPropertyMetadata.Type.EMAIL, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_UI_CSV_MAX_ROWS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEYSTORE_LOCATION, DomibusPropertyMetadata.Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEYSTORE_TYPE, DomibusPropertyMetadata.Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEYSTORE_PASSWORD, true, DomibusPropertyMetadata.Usage.DOMAIN, false, true),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS, DomibusPropertyMetadata.Usage.DOMAIN, false),

            new DomibusPropertyMetadata(DOMIBUS_SECURITY_TRUSTSTORE_LOCATION, DomibusPropertyMetadata.Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_TRUSTSTORE_TYPE, DomibusPropertyMetadata.Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD, true, DomibusPropertyMetadata.Usage.DOMAIN, false, true),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_AUTH_UNSECURE_LOGIN_ALLOWED, DomibusPropertyMetadata.Type.BOOLEAN),
            new DomibusPropertyMetadata(DOMIBUS_CONSOLE_LOGIN_MAXIMUM_ATTEMPT, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_CONSOLE_LOGIN_SUSPENSION_TIME, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_CERTIFICATE_REVOCATION_OFFSET, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_CERTIFICATE_CRL_EXCLUDED_PROTOCOLS, DomibusPropertyMetadata.Usage.DOMAIN, false),

            new DomibusPropertyMetadata(DOMIBUS_PLUGIN_LOGIN_MAXIMUM_ATTEMPT, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_PLUGIN_LOGIN_SUSPENSION_TIME, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PATTERN, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_VALIDATION_MESSAGE, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_EXPIRATION, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_DEFAULT_PASSWORD_EXPIRATION, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_WARNING_BEFORE_EXPIRATION, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_DONT_REUSE_LAST, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD, DomibusPropertyMetadata.Type.BOOLEAN),

            new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICY_PATTERN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICY_VALIDATION_MESSAGE, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_EXPIRATION, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_DEFAULT_PASSWORD_EXPIRATION, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_DONT_REUSE_LAST, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_ATTACHMENT_TEMP_STORAGE_LOCATION, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ATTACHMENT_STORAGE_LOCATION, DomibusPropertyMetadata.Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_ENCRYPTION_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_MSH_MESSAGEID_SUFFIX, DomibusPropertyMetadata.Usage.DOMAIN, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_MSH_RETRY_MESSAGE_EXPIRATION_DELAY, DomibusPropertyMetadata.Type.NUMERIC),

            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SMLZONE, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_MODE, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_PARTYID_RESPONDER_ROLE, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_PARTYID_TYPE, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4, DomibusPropertyMetadata.Usage.DOMAIN, true),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_LIST_PENDING_MESSAGES_MAX_COUNT, DomibusPropertyMetadata.Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_JMS_QUEUE_MAX_BROWSE_SIZE, DomibusPropertyMetadata.Type.NUMERIC), //there is one place at init time where it is not refreshed
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_JMS_INTERNAL_QUEUE_EXPRESSION),

            new DomibusPropertyMetadata(DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION, DomibusPropertyMetadata.Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SENDER_CERTIFICATE_SUBJECT_CHECK, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN, DomibusPropertyMetadata.Usage.DOMAIN, false),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATE_TIME_PATTERN_ON_RECEIVING, DomibusPropertyMetadata.Type.STRING),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATE_TIME_PATTERN_ON_SENDING, DomibusPropertyMetadata.Type.STRING),

            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CONNECTION_TIMEOUT, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_RECEIVE_TIMEOUT, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_ALLOW_CHUNKING, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CHUNKING_THRESHOLD, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CONCURENCY, DomibusPropertyMetadata.Type.CONCURRENCY, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_LARGE_FILES_CONCURRENCY, DomibusPropertyMetadata.Type.CONCURRENCY, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CACHEABLE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CONNECTION_KEEP_ALIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_PRIORITY, Module.MSH, false,  DomibusPropertyMetadata.Usage.DOMAIN, false, false, false, true),
            new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_RETENTION_JMS_CONCURRENCY, DomibusPropertyMetadata.Type.CONCURRENCY, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCH_EBMS_ERROR_UNRECOVERABLE_RETRY, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_ENABLED, DomibusPropertyMetadata.Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_HTTP_HOST),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_HTTP_PORT, DomibusPropertyMetadata.Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_USER),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_PROXY_PASSWORD, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_NON_PROXY_HOSTS),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_JDBC_DATASOURCE_JNDI_NAME),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_JDBC_DATASOURCE_QUARTZ_JNDI_NAME),

            new DomibusPropertyMetadata(DOMIBUS_UI_REPLICATION_SYNC_CRON_MAX_ROWS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PLUGIN_NOTIFICATION_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_NONREPUDIATION_AUDIT_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN),
            new DomibusPropertyMetadata(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD, DomibusPropertyMetadata.Usage.DOMAIN, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_SEND_MESSAGE_ATTEMPT_AUDIT_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_FOURCORNERMODEL_ENABLED, DomibusPropertyMetadata.Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_LOGGING_PAYLOAD_PRINT, DomibusPropertyMetadata.Type.BOOLEAN),     //there are still usages in xml!!!! move them?
            new DomibusPropertyMetadata(DOMIBUS_LOGGING_EBMS3_ERROR_PRINT,  DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_LOGGING_CXF_LIMIT, DomibusPropertyMetadata.Type.NUMERIC),         //there are still usages in xml!!!! move them?

            new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXCLUDE_REGEX, DomibusPropertyMetadata.Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXPIRATION, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_DIRECTORIES, DomibusPropertyMetadata.Usage.DOMAIN, false),

            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_CONCURRENCY, DomibusPropertyMetadata.Type.CONCURRENCY, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_PAYLOADS_SCHEDULE_THRESHOLD, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMAIN_TITLE, DomibusPropertyMetadata.Usage.DOMAIN, false),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_USER_INPUT_BLACK_LIST),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_USER_INPUT_WHITE_LIST),

            new DomibusPropertyMetadata(DOMIBUS_ACCOUNT_UNLOCK_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_CERTIFICATE_CHECK_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PLUGIN_ACCOUNT_UNLOCK_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICIES_CHECK_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICIES_CHECK_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_MSH_RETRY_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_MSH_PULL_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_RETRY_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CLEANER_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_RETRY_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_UI_REPLICATION_SYNC_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SPLIT_AND_JOIN_RECEIVE_EXPIRATION_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_MONITORING_CONNECTION_CRON, DomibusPropertyMetadata.Type.CRON, DomibusPropertyMetadata.Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED, DomibusPropertyMetadata.Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_ALERT_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_MAIL_SMTP_TIMEOUT, DomibusPropertyMetadata.Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_URL),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_PORT, DomibusPropertyMetadata.Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_USER),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_PASSWORD),

            new DomibusPropertyMetadata(DOMIBUS_ALERT_SENDER_EMAIL, DomibusPropertyMetadata.Type.EMAIL, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, false),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_RECEIVER_EMAIL, DomibusPropertyMetadata.Type.EMAIL, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, false),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_RETRY_TIME, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_LEVEL, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_SUBJECT, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_LEVEL, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_DELAY_DAYS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_FREQUENCY_DAYS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_LEVEL, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_MAIL_SUBJECT, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_DELAY_DAYS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_FREQUENCY_DAYS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_LEVEL, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_MAIL_SUBJECT, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_DELAY_DAYS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_FREQUENCY_DAYS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_LEVEL, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_MAIL_SUBJECT, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_DELAY_DAYS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_FREQUENCY_DAYS, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_LEVEL, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_MAIL_SUBJECT, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_LEVEL, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_MAIL_SUBJECT, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_LEVEL, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_MOMENT, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_SUBJECT, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_ACTIVE, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_LEVEL, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_SUBJECT, DomibusPropertyMetadata.Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_FREQUENCY_RECOVERY_TIME, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_FREQUENCY_ERROR_COUNT, DomibusPropertyMetadata.Type.NUMERIC, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_DYNAMIC_INITIATOR, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_MULTIPLE_LEGS, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_FORCE_BY_MPC, DomibusPropertyMetadata.Type.BOOLEAN, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_MPC_INITIATOR_SEPARATOR, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_RECEIPT_QUEUE_CONCURRENCY, DomibusPropertyMetadata.Type.CONCURRENCY, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_QUEUE_CONCURENCY, DomibusPropertyMetadata.Type.CONCURRENCY, DomibusPropertyMetadata.Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_EXTENSION_IAM_AUTHENTICATION_IDENTIFIER, DomibusPropertyMetadata.Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EXTENSION_IAM_AUTHORIZATION_IDENTIFIER, DomibusPropertyMetadata.Usage.DOMAIN, true),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_EXCEPTIONS_REST_ENABLE, DomibusPropertyMetadata.Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_INSTANCE_NAME),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_FILE_UPLOAD_MAX_SIZE, DomibusPropertyMetadata.Type.NUMERIC),
    }).collect(Collectors.toMap(x -> x.getName(), x -> x));

    /**
     * Returns all the properties that this PropertyProvider is able to handle, writable and read-only alike.
     *
     * @return a map
     * @implNote This list will be moved in the database eventually.
     */
    @Override
    public Map<String, DomibusPropertyMetadata> getKnownProperties() {
        return knownProperties;
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return this.getKnownProperties().containsKey(name);
    }

}
