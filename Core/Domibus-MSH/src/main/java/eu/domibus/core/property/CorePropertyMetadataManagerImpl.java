package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyMetadata.Type;
import eu.domibus.api.property.DomibusPropertyMetadata.Usage;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.Module;
import eu.domibus.plugin.environment.DomibusEnvironmentConstants;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Property metadata holder class of core properties (common to all servers)
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class CorePropertyMetadataManagerImpl implements DomibusPropertyMetadataManagerSPI {

    private Map<String, DomibusPropertyMetadata> knownProperties = Arrays.stream(new DomibusPropertyMetadata[]{
            //read-only properties
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DomibusEnvironmentConstants.DOMIBUS_ENVIRONMENT_SERVER_NAME, Type.STRING),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_CONFIG_LOCATION, Type.URI),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DEPLOYMENT_CLUSTERED, Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_SCHEDULER_BOOTSTRAP_SYNCHRONIZED, Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATABASE_GENERAL_SCHEMA),

            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD, Type.PASSWORD, true, Usage.DOMAIN, false, true),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_PASSWORD, Type.PASSWORD, true, Usage.DOMAIN, false, true),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_SIGN_PASSWORD, Type.PASSWORD, true, Usage.DOMAIN, false, true),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_DECRYPT_PASSWORD, Type.PASSWORD, true, Usage.DOMAIN, false, true),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_PASSWORD, Type.PASSWORD, true, Usage.DOMAIN, false, true),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_SIGN_PASSWORD, Type.PASSWORD, true, Usage.DOMAIN, false, true),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_DECRYPT_PASSWORD, Type.PASSWORD, true, Usage.DOMAIN, false, true),

            new DomibusPropertyMetadata(DOMIBUS_DATABASE_SCHEMA, false, Usage.DOMAIN, false),

            new DomibusPropertyMetadata(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY, Type.CLASS, Module.MSH, false, Usage.GLOBAL, false, false, false, true),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_PACKAGES_TO_SCAN, Type.CLASS),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_CONNECTION_DRIVER_CLASS, Type.CLASS),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_JDBC_FETCH, Type.NUMERIC),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_DIALECT, Type.CLASS),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_ID_NEW_GENERATOR_MAPPINGS, Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_FORMAT_SQL, Type.BOOLEAN),

            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_ENCRYPTION_ACTIVE, Type.BOOLEAN, false, Usage.GLOBAL_AND_DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_ENCRYPTION_PROPERTIES, false, Usage.GLOBAL_AND_DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION, Type.URI, false, Usage.GLOBAL_AND_DOMAIN, false),

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_CONNECTION_FACTORY_SESSION_CACHE_SIZE, Type.NUMERIC),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_QUEUE_PULL, Type.JNDI),

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_QUEUE_ALERT, Type.JNDI),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_TASK_EXECUTOR_THREAD_COUNT, Type.NUMERIC),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_MSH_TASK_EXECUTOR_THREAD_COUNT, Type.NUMERIC),


            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ALERT_QUEUE_CONCURRENCY, Type.CONCURRENCY),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(MESSAGE_FACTORY_CLASS, Type.CLASS),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(COMPRESSION_BLACKLIST),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_INTERNAL_QUEUE_CONCURENCY, Type.CONCURRENCY),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_INTERNAL_COMMAND_CONCURENCY, Type.CONCURRENCY),

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_JMX_REPORTER_ENABLE, Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_ENABLE, Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_TIME_UNIT),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_NUMBER, Type.NUMERIC),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_MEMORY, Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_GC, Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_CACHED_THREADS, Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES, Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES_REFRESH_PERIOD, Type.NUMERIC),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES_SHOW_DLQ_ONLY, Type.BOOLEAN),

            new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE_PER_MPC, Type.NUMERIC, Module.MSH, false, Usage.DOMAIN, true, true, false, true),

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_SECURITY_EXT_AUTH_PROVIDER_ENABLED, Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_SCHEMAFACTORY, Type.CLASS),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_UI_SESSION_SECURE, Type.BOOLEAN),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_UI_SESSION_TIMEOUT, Type.NUMERIC),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_UI_SESSION_SAME_SITE),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_UI_SESSION_JVMROUTE),

            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_TIMEOUT, Type.NUMERIC, false, Usage.DOMAIN, true),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_CONNECTION_CXF_SSL_OFFLOAD_ENABLE, Type.BOOLEAN),

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_HTTP_SECURITY_STRICT_TRANSPORT_SECURITY, Type.NUMERIC),

            //writable properties
            new DomibusPropertyMetadata(DOMIBUS_UI_TITLE_NAME, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_NAME, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_EMAIL, Type.EMAIL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_UI_CSV_MAX_ROWS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_UI_MESSAGE_LOGS_COUNT_LIMIT, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_UI_MESSAGE_LOGS_DEFAULT_INTERVAL, Type.NUMERIC, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEYSTORE_LOCATION, Type.URI, true, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEYSTORE_TYPE, Type.STRING, true, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEYSTORE_PASSWORD, Type.PASSWORD, true, Usage.DOMAIN, false, true),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_PROFILE_ORDER, Type.COMMA_SEPARATED_LIST, Usage.DOMAIN, false),

            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_ALIAS, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_SIGN_ALIAS, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_RSA_DECRYPT_ALIAS, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_ALIAS, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_SIGN_ALIAS, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_ECC_DECRYPT_ALIAS, Usage.DOMAIN, false),

            new DomibusPropertyMetadata(DOMIBUS_SECURITY_TRUSTSTORE_LOCATION, Type.URI, true, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_TRUSTSTORE_TYPE, Type.STRING, true, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD, Type.PASSWORD, true, Usage.DOMAIN, false, true),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_AUTH_UNSECURE_LOGIN_ALLOWED, Type.BOOLEAN),
            new DomibusPropertyMetadata(DOMIBUS_CONSOLE_LOGIN_MAXIMUM_ATTEMPT, Type.NUMERIC, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_CONSOLE_LOGIN_SUSPENSION_TIME, Type.NUMERIC, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_CERTIFICATE_REVOCATION_OFFSET, Type.NUMERIC, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_PLUGIN_LOGIN_MAXIMUM_ATTEMPT, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PLUGIN_LOGIN_SUSPENSION_TIME, Type.NUMERIC, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PATTERN, Type.REGEXP, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_VALIDATION_MESSAGE, Type.FREE_TEXT, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_EXPIRATION, Type.NUMERIC, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_DEFAULT_PASSWORD_EXPIRATION, Type.NUMERIC, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_WARNING_BEFORE_EXPIRATION, Type.NUMERIC, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_DONT_REUSE_LAST, Type.NUMERIC, Usage.DOMAIN_AND_SUPER, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD, Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PASSWORD_POLICY_DEFAULT_USER_CREATE, Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PASSWORD_POLICY_DEFAULT_USER_AUTOGENERATE_PASSWORD, Type.BOOLEAN),

            new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICY_PATTERN, Type.REGEXP, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICY_VALIDATION_MESSAGE, Type.FREE_TEXT, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_EXPIRATION, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_DEFAULT_PASSWORD_EXPIRATION, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_DONT_REUSE_LAST, Type.NUMERIC, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_ATTACHMENT_TEMP_STORAGE_LOCATION, Type.URI, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ATTACHMENT_STORAGE_LOCATION, Type.URI, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_ENCRYPTION_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_MSH_MESSAGEID_SUFFIX, Type.URI, Usage.DOMAIN, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_MSH_RETRY_MESSAGE_EXPIRATION_DELAY, Type.NUMERIC),

            new DomibusPropertyMetadata(DOMIBUS_MSH_RETRY_TIMEOUT_DELAY, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SMLZONE, Type.URI, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_MODE, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION, Type.REGEXP, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION, Type.REGEXP, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION, Type.STRING, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_PARTYID_RESPONDER_ROLE, Type.URI, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_PARTYID_RESPONDER_ROLE, Type.URI, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_PARTYID_TYPE, Type.URI, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_PARTYID_TYPE, Type.URI, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4, Type.HYPHENED_NAME, Usage.DOMAIN, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_DYNAMICDISCOVERY_LOOKUP_CACHE_TTL, Type.NUMERIC),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_LIST_PENDING_MESSAGES_MAX_COUNT, Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_JMS_QUEUE_MAX_BROWSE_SIZE, Type.NUMERIC), //there is one place at init time where it is not refreshed
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_JMS_INTERNAL_QUEUE_EXPRESSION, Type.REGEXP),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_JMS_INTERNAL_ADDRESS_EXPRESSION, Type.REGEXP),

            new DomibusPropertyMetadata(DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION, Type.REGEXP, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SENDER_TRUST_VALIDATION_CERTIFICATE_POLICY_OIDS, Type.COMMA_SEPARATED_LIST, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SENDER_CERTIFICATE_SUBJECT_CHECK, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS, Type.BOOLEAN, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_RECEIVER_SELF_SENDING_VALIDATION_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN, Type.REGEXP, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PARTYINFO_ROLES_VALIDATION_ENABLED, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PMODE_LEGCONFIGURATION_MPC_VALIDATION_ENABLED, Type.BOOLEAN, Usage.DOMAIN, true),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATE_TIME_PATTERN_ON_RECEIVING, Type.REGEXP),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATE_TIME_PATTERN_ON_SENDING, Type.REGEXP),

            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CONNECTION_TIMEOUT, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_RECEIVE_TIMEOUT, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_ALLOW_CHUNKING, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CHUNKING_THRESHOLD, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CONCURENCY, Type.CONCURRENCY, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_LARGE_FILES_CONCURRENCY, Type.CONCURRENCY, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CACHEABLE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CONNECTION_KEEP_ALIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_PRIORITY, Module.MSH, false, Usage.DOMAIN, false, false, false, true),
            new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_DELETION_STRATEGY, Type.STRING, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_SENT_MAX_DELETE, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_PAYLOAD_DELETED_MAX_DELETE, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_BATCH_DELETE, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_RETENTION_JMS_CONCURRENCY, Type.CONCURRENCY, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PARTITIONS_CREATION_DAYS_TO_CHECK, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PARTITIONS_DROP_CHECK_MESSAGES_EARCHIVED, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCH_EBMS_ERROR_UNRECOVERABLE_RETRY, Type.BOOLEAN, Usage.DOMAIN, true),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_ENABLED, Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_HTTP_HOST, Type.URI),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_HTTP_PORT, Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_USER),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_PASSWORD, Type.PASSWORD, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_NON_PROXY_HOSTS),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_JDBC_DATASOURCE_JNDI_NAME, Type.JNDI),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_JDBC_DATASOURCE_QUARTZ_JNDI_NAME, Type.JNDI),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PLUGIN_NOTIFICATION_ACTIVE, Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_NONREPUDIATION_AUDIT_ACTIVE, Type.BOOLEAN),
            new DomibusPropertyMetadata(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD, Type.BOOLEAN, Usage.DOMAIN, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_SEND_MESSAGE_ATTEMPT_AUDIT_ACTIVE, Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_FOURCORNERMODEL_ENABLED, Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_LOGGING_PAYLOAD_PRINT, Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_LOGGING_METADATA_PRINT, Type.BOOLEAN),
            new DomibusPropertyMetadata(DOMIBUS_LOGGING_EBMS3_ERROR_PRINT, Type.BOOLEAN, Usage.DOMAIN, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_LOGGING_CXF_LIMIT, Type.NUMERIC),         //there are still usages in xml!!!! move them?

            new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXCLUDE_REGEX, Type.REGEXP, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXPIRATION, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_DIRECTORIES, Type.URI, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_LIMIT_28ATTACHMENTS_PER_MESSAGE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_DECOMPRESSION_VALIDATION_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_CONCURRENCY, Type.CONCURRENCY, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_PAYLOADS_SCHEDULE_THRESHOLD, Type.NUMERIC, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMAIN_TITLE, Usage.DOMAIN, false),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_USER_INPUT_BLACK_LIST, Type.REGEXP),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_USER_INPUT_WHITE_LIST, Type.REGEXP),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROPERTY_LENGTH_MAX, Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROPERTY_VALIDATION_ENABLED, Type.BOOLEAN),
            new DomibusPropertyMetadata(DOMIBUS_PROPERTY_BACKUP_PERIOD_MIN, Type.POSITIVE_DECIMAL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PROPERTY_BACKUP_HISTORY_MAX, Type.POSITIVE_INTEGER, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_ACCOUNT_UNLOCK_CRON, Type.CRON, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_CERTIFICATE_CHECK_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PLUGIN_ACCOUNT_UNLOCK_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICIES_CHECK_CRON, Type.CRON, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICIES_CHECK_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_MSH_RETRY_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PARTITIONS_WORKER_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_MSH_PULL_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_RETRY_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CLEANER_CRON, Type.CRON, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_RETRY_CRON, Type.CRON, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_SPLIT_AND_JOIN_RECEIVE_EXPIRATION_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_MONITORING_CONNECTION_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_MONITORING_CONNECTION_SELF_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ERRORLOG_CLEANER_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ERRORLOG_CLEANER_OLDER_DAYS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ERRORLOG_CLEANER_BATCH_SIZE, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_DELETE_RECEIVED_TEST_MESSAGE_HISTORY_CRON, Type.CRON, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_START_DATE_STOPPED_ALLOWED_HOURS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_EXPORT_EMPTY, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_STORAGE_LOCATION, Type.URI, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_SANITY_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_BATCH_SIZE, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_BATCH_SIZE_PAYLOAD, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_BATCH_MAX, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_BATCH_RETRY_TIMEOUT, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_BATCH_MPCS, Type.STRING, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_NOTIFICATION_URL, Type.URI, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_NOTIFICATION_TIMEOUT, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_NOTIFICATION_USEPROXY, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_NOTIFICATION_USERNAME, Type.STRING, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_NOTIFICATION_PASSWORD, Type.PASSWORD, Usage.DOMAIN, false),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_REST_API_RETURN_MESSAGES, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_RETENTION_DAYS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_RETENTION_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_RETENTION_DELETE_MAX, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_QUEUE_CONCURRENCY, Type.CONCURRENCY, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_NOTIFICATION_QUEUE_CONCURRENCY, Type.CONCURRENCY, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVE_NOTIFICATION_DLQ_CONCURRENCY, Type.CONCURRENCY, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_ALERT_EARCHIVING_MSG_NON_FINAL_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_EARCHIVING_MSG_NON_FINAL_LEVEL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_EARCHIVING_MSG_NON_FINAL_MAIL_SUBJECT, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_ALERT_EARCHIVING_START_DATE_STOPPED_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_EARCHIVING_START_DATE_STOPPED_LEVEL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_EARCHIVING_START_DATE_STOPPED_MAIL_SUBJECT, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EARCHIVING_NOTIFICATION_DETAILS_ENABLED, Type.BOOLEAN, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_ALERT_EARCHIVING_EXPORT_FAILED_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_EARCHIVING_EXPORT_FAILED_LEVEL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_EARCHIVING_EXPORT_FAILED_MAIL_SUBJECT, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED, Type.COMMA_SEPARATED_LIST, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_MONITORING_CONNECTION_DELETE_HISTORY_FOR_PARTIES, Type.COMMA_SEPARATED_LIST, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_SMART_RETRY_ENABLED, Type.COMMA_SEPARATED_LIST, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_ALERT_ACTIVE, Type.BOOLEAN, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE, Type.BOOLEAN, Usage.DOMAIN_AND_SUPER, true),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_MAIL_SMTP_TIMEOUT, Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_URL, Type.URI),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_PORT, Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_USER),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_PASSWORD, Type.PASSWORD),

            new DomibusPropertyMetadata(DOMIBUS_ALERT_SENDER_EMAIL, Type.EMAIL, Usage.DOMAIN_AND_SUPER, false),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_RECEIVER_EMAIL, Type.EMAIL, Usage.DOMAIN_AND_SUPER, false),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME, Type.NUMERIC, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_RETRY_TIME, Type.NUMERIC, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS, Type.NUMERIC, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES, Type.COMMA_SEPARATED_LIST, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL, Type.COMMA_SEPARATED_LIST, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE, Type.BOOLEAN, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE, Type.BOOLEAN, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_ACTIVE, Type.BOOLEAN, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_LEVEL, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_SUBJECT, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_LEVEL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_ACTIVE, Type.BOOLEAN, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_DELAY_DAYS, Type.NUMERIC, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_FREQUENCY_DAYS, Type.NUMERIC, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_LEVEL, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_MAIL_SUBJECT, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_ACTIVE, Type.BOOLEAN, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_DELAY_DAYS, Type.NUMERIC, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_FREQUENCY_DAYS, Type.NUMERIC, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_LEVEL, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_MAIL_SUBJECT, Usage.DOMAIN_AND_SUPER, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_DELAY_DAYS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_FREQUENCY_DAYS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_LEVEL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_MAIL_SUBJECT, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_DELAY_DAYS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_FREQUENCY_DAYS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_LEVEL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_MAIL_SUBJECT, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_LEVEL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_MAIL_SUBJECT, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_LEVEL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_MOMENT, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_SUBJECT, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_LEVEL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_SUBJECT, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_PARTITION_CHECK_FREQUENCY_DAYS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_EARCHIVING_NOTIFICATION_FAILED_ACTIVE, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_EARCHIVING_NOTIFICATION_FAILED_LEVEL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_EARCHIVING_NOTIFICATION_FAILED_MAIL_SUBJECT, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PARTIES, Type.COMMA_SEPARATED_LIST, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_FREQUENCY_DAYS, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_LEVEL, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_MAIL_SUBJECT, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_FREQUENCY_RECOVERY_TIME, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_FREQUENCY_ERROR_COUNT, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_DYNAMIC_INITIATOR, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_MULTIPLE_LEGS, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_FORCE_BY_MPC, Type.BOOLEAN, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_MPC_INITIATOR_SEPARATOR, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_RECEIPT_QUEUE_CONCURRENCY, Type.CONCURRENCY, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_PULL_QUEUE_CONCURENCY, Type.CONCURRENCY, Usage.DOMAIN, true),

            new DomibusPropertyMetadata(DOMIBUS_EXTENSION_IAM_AUTHENTICATION_IDENTIFIER, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_EXTENSION_IAM_AUTHORIZATION_IDENTIFIER, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_CERTIFICATE_CRL_HTTP_TIMEOUT, Type.NUMERIC, Usage.DOMAIN, true),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_EXCEPTIONS_REST_ENABLE, Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_INSTANCE_NAME),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_FILE_UPLOAD_MAX_SIZE, Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_MESSAGE_DOWNLOAD_MAX_SIZE, Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_CERTIFICATE_CRL_EXCLUDED_PROTOCOLS, Type.COMMA_SEPARATED_LIST),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_RESEND_BUTTON_ENABLED_RECEIVED_MINUTES, Type.NUMERIC),
            new DomibusPropertyMetadata(DOMIBUS_MESSAGE_RESEND_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_QUARTZ_TRIGGER_BLOCKED_DURATION, Type.NUMERIC, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_FINAL_RECIPIENT_CLEANUP_CRON, Type.CRON, Usage.DOMAIN, true),
            new DomibusPropertyMetadata(DOMIBUS_FINAL_RECIPIENT_CLEANUP_OLDER_THAN, Type.NUMERIC, Usage.DOMAIN, true),

            //Start distributed cache properties
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_DISTRIBUTED_CACHE_DEFAULT_TTL, Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_DISTRIBUTED_CACHE_MAX_IDLE, Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_DISTRIBUTED_CACHE_DEFAULT_SIZE, Type.NUMERIC),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_DISTRIBUTED_NEAR_CACHE_DEFAULT_TTL, Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_DISTRIBUTED_NEAR_CACHE_DEFAULT_MAX_IDLE, Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_DISTRIBUTED_NEAR_CACHE_DEFAULT_SIZE, Type.NUMERIC),

            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_DISTRIBUTED_CACHE_PORT, Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_DISTRIBUTED_CACHE_PORT_AUTOINCREMENT, Type.BOOLEAN),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_DISTRIBUTED_CACHE_PORT_COUNT, Type.NUMERIC),
            DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_DISTRIBUTED_CACHE_MEMBERS, Type.COMMA_SEPARATED_LIST)
            //End distributed cache properties
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
