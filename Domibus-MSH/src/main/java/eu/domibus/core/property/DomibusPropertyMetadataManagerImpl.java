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
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DEPLOYMENT_CLUSTERED),
                new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD, false, DomibusPropertyMetadata.Type.DOMAIN, false, true),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATABASE_GENERAL_SCHEMA),
                new DomibusPropertyMetadata(DOMIBUS_DATABASE_SCHEMA, false, DomibusPropertyMetadata.Type.DOMAIN, false),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_XA_DATA_SOURCE_CLASS_NAME),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_MAX_LIFETIME),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_MIN_POOL_SIZE),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_MAX_POOL_SIZE),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_BORROW_CONNECTION_TIMEOUT),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_REAP_TIMEOUT),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_MAX_IDLE_TIME),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_MAINTENANCE_INTERVAL),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_PROPERTY_USER),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_PROPERTY_PASSWORD, true),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_PROPERTY_url),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_PROPERTY_URL),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_DRIVER_CLASS_NAME),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_URL),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_USER),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_PASSWORD, true),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_MAX_LIFETIME),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_MIN_POOL_SIZE),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_MAX_POOL_SIZE),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_PACKAGES_TO_SCAN),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_CONNECTION_DRIVER_CLASS),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_DIALECT),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_ID_NEW_GENERATOR_MAPPINGS),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_FORMAT_SQL),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_TRANSACTION_FACTORY_CLASS),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_TRANSACTION_JTA_PLATFORM),

                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_ENCRYPTION_ACTIVE, false, DomibusPropertyMetadata.Type.GLOBAL_AND_DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_ENCRYPTION_PROPERTIES, false, DomibusPropertyMetadata.Type.GLOBAL_AND_DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION, false, DomibusPropertyMetadata.Type.GLOBAL_AND_DOMAIN, true),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_QUEUE_PULL),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_QUEUE_UI_REPLICATION), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_XACONNECTION_FACTORY_MAX_POOL_SIZE), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_QUEUE_ALERT), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_TASK_EXECUTOR_THREAD_COUNT),  //move the use=age from xml ?

                DomibusPropertyMetadata.getReadOnlyGlobalProperty(COM_ATOMIKOS_ICATCH_OUTPUT_DIR), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(COM_ATOMIKOS_ICATCH_LOG_BASE_DIR), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(COM_ATOMIKOS_ICATCH_DEFAULT_JTA_TIMEOUT), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(COM_ATOMIKOS_ICATCH_MAX_TIMEOUT), //move the use=age from xml ?

                DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_BROKER_HOST), //cannot find the usage
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_BROKER_NAME), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_EMBEDDED_CONFIGURATION_FILE),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_JMXURL), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_CONNECTOR_PORT), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_RMI_SERVER_PORT), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_TRANSPORT_CONNECTOR_URI), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_USERNAME), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_PASSWORD), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_PERSISTENT), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_CONNECTION_CLOSE_TIMEOUT), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_CONNECTION_CONNECT_RESPONSE_TIMEOUT), //move the use=age from xml ?

                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_ALERT_QUEUE_CONCURRENCY), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(MESSAGE_FACTORY_CLASS), //move the use=age from xml ?
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(COMPRESSION_BLACKLIST),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_INTERNAL_QUEUE_CONCURENCY), //move the use=age from xml ?

                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_JMX_REPORTER_ENABLE),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_ENABLE),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_TIME_UNIT),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_NUMBER),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_MEMORY),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_GC),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_CACHED_THREADS),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_SECURITY_EXT_AUTH_PROVIDER_ENABLED),

                DomibusPropertyMetadata.getReadOnlyGlobalProperty(WEBLOGIC_MANAGEMENT_SERVER),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMX_USER),
                DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMX_PASSWORD),
//
                //writable properties
                new DomibusPropertyMetadata(DOMIBUS_UI_TITLE_NAME, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_REPLICATION_ENABLED, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_NAME, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_EMAIL, DomibusPropertyMetadata.Type.DOMAIN, true),

                new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEYSTORE_LOCATION, DomibusPropertyMetadata.Type.DOMAIN, false),
                new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEYSTORE_TYPE, DomibusPropertyMetadata.Type.DOMAIN, false),
                new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEYSTORE_PASSWORD, true, DomibusPropertyMetadata.Type.DOMAIN, false, true),
                new DomibusPropertyMetadata(DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS, DomibusPropertyMetadata.Type.DOMAIN, false),

                new DomibusPropertyMetadata(DOMIBUS_SECURITY_TRUSTSTORE_LOCATION, DomibusPropertyMetadata.Type.DOMAIN, false),
                new DomibusPropertyMetadata(DOMIBUS_SECURITY_TRUSTSTORE_TYPE, DomibusPropertyMetadata.Type.DOMAIN, false),
                new DomibusPropertyMetadata(DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD, true, DomibusPropertyMetadata.Type.DOMAIN, false, true),

                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_AUTH_UNSECURE_LOGIN_ALLOWED),
                new DomibusPropertyMetadata(DOMIBUS_CONSOLE_LOGIN_MAXIMUM_ATTEMPT, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_CONSOLE_LOGIN_SUSPENSION_TIME, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_CERTIFICATE_REVOCATION_OFFSET, DomibusPropertyMetadata.Type.DOMAIN, false),
                new DomibusPropertyMetadata(DOMIBUS_CERTIFICATE_CRL_EXCLUDED_PROTOCOLS, DomibusPropertyMetadata.Type.DOMAIN, false),

                new DomibusPropertyMetadata(DOMIBUS_PLUGIN_LOGIN_MAXIMUM_ATTEMPT, DomibusPropertyMetadata.Type.DOMAIN, false),
                new DomibusPropertyMetadata(DOMIBUS_PLUGIN_LOGIN_SUSPENSION_TIME, DomibusPropertyMetadata.Type.DOMAIN, true),

                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PATTERN, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_VALIDATION_MESSAGE, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_EXPIRATION, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_DEFAULT_PASSWORD_EXPIRATION, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_WARNING_BEFORE_EXPIRATION, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_DONT_REUSE_LAST, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD),

                new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICY_PATTERN, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICY_VALIDATION_MESSAGE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_EXPIRATION, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_DEFAULT_PASSWORD_EXPIRATION, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_DONT_REUSE_LAST, DomibusPropertyMetadata.Type.DOMAIN, true),

                new DomibusPropertyMetadata(DOMIBUS_ATTACHMENT_TEMP_STORAGE_LOCATION, DomibusPropertyMetadata.Type.DOMAIN, false),
                new DomibusPropertyMetadata(DOMIBUS_ATTACHMENT_STORAGE_LOCATION, DomibusPropertyMetadata.Type.DOMAIN, false),
                new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_ENCRYPTION_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN, true),

                new DomibusPropertyMetadata(DOMIBUS_MSH_MESSAGEID_SUFFIX, DomibusPropertyMetadata.Type.DOMAIN, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_MSH_RETRY_MESSAGE_EXPIRATION_DELAY),

                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_SMLZONE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_MODE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_PARTYID_RESPONDER_ROLE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_PARTYID_TYPE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4, DomibusPropertyMetadata.Type.DOMAIN, true),

                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_LIST_PENDING_MESSAGES_MAX_COUNT),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_JMS_QUEUE_MAX_BROWSE_SIZE), //there is one place at init time that it is not refreshed
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_JMS_INTERNAL_QUEUE_EXPRESSION),

                new DomibusPropertyMetadata(DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION, DomibusPropertyMetadata.Type.DOMAIN, false),
                new DomibusPropertyMetadata(DOMIBUS_SENDER_CERTIFICATE_SUBJECT_CHECK, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN, DomibusPropertyMetadata.Type.DOMAIN, false),

                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CONNECTION_TIMEOUT, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_RECEIVE_TIMEOUT, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_ALLOW_CHUNKING, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CHUNKING_THRESHOLD, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CONCURENCY, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_LARGE_FILES_CONCURRENCY, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CACHEABLE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_CONNECTION_KEEP_ALIVE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_RETENTION_JMS_CONCURRENCY, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCH_EBMS_ERROR_UNRECOVERABLE_RETRY, DomibusPropertyMetadata.Type.DOMAIN, true),

                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_ENABLED),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_HTTP_HOST),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_HTTP_PORT),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_USER),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_PASSWORD),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PROXY_NON_PROXY_HOSTS),

                new DomibusPropertyMetadata(DOMIBUS_UI_REPLICATION_SYNC_CRON_MAX_ROWS, DomibusPropertyMetadata.Type.DOMAIN, true), //there is still one call from xml!!!
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PLUGIN_NOTIFICATION_ACTIVE),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_NONREPUDIATION_AUDIT_ACTIVE),
                new DomibusPropertyMetadata(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD, DomibusPropertyMetadata.Type.DOMAIN, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_SEND_MESSAGE_ATTEMPT_AUDIT_ACTIVE),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_FOURCORNERMODEL_ENABLED),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_LOGGING_PAYLOAD_PRINT),     //there are still usages in xml!!!! move them?
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_LOGGING_CXF_LIMIT),         //there are still usages in xml!!!! move them?

                new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXCLUDE_REGEX, DomibusPropertyMetadata.Type.DOMAIN, false),
                new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXPIRATION, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_DIRECTORIES, DomibusPropertyMetadata.Type.DOMAIN, false),

                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_CONCURRENCY, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_PAYLOADS_SCHEDULE_THRESHOLD, DomibusPropertyMetadata.Type.DOMAIN, true),

                new DomibusPropertyMetadata(DOMAIN_TITLE, DomibusPropertyMetadata.Type.DOMAIN, false),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_USER_INPUT_BLACK_LIST),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_USER_INPUT_WHITE_LIST),

                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ACCOUNT_UNLOCK_CRON),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_CERTIFICATE_CHECK_CRON),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PLUGIN_ACCOUNT_UNLOCK_CRON),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICIES_CHECK_CRON, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICIES_CHECK_CRON, DomibusPropertyMetadata.Type.DOMAIN, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_CRON),
                new DomibusPropertyMetadata(DOMIBUS_MSH_RETRY_CRON, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_MSH_PULL_CRON, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_RETRY_CRON, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CLEANER_CRON, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_RETRY_CRON, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_UI_REPLICATION_SYNC_CRON),
                new DomibusPropertyMetadata(DOMIBUS_SPLIT_AND_JOIN_RECEIVE_EXPIRATION_CRON, DomibusPropertyMetadata.Type.DOMAIN, true),

                new DomibusPropertyMetadata(DOMIBUS_ALERT_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_MAIL_SMTP_TIMEOUT),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_URL),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_PORT),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_USER),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_PASSWORD),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_SENDER_EMAIL, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, false),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_RECEIVER_EMAIL, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, false),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_RETRY_TIME, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_LEVEL, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_DELAY_DAYS, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_FREQUENCY_DAYS, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_LEVEL, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_MAIL_SUBJECT, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_DELAY_DAYS, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_FREQUENCY_DAYS, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_LEVEL, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PASSWORD_EXPIRED_MAIL_SUBJECT, DomibusPropertyMetadata.Type.DOMAIN_AND_SUPER, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_DELAY_DAYS, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_FREQUENCY_DAYS, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_LEVEL, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_MAIL_SUBJECT, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_DELAY_DAYS, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_FREQUENCY_DAYS, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_LEVEL, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_MAIL_SUBJECT, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_LEVEL, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_MAIL_SUBJECT, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_ACTIVE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_LEVEL, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_MOMENT, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_SUBJECT, DomibusPropertyMetadata.Type.DOMAIN, true),

                new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_FREQUENCY_RECOVERY_TIME, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_REQUEST_FREQUENCY_ERROR_COUNT, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_DYNAMIC_INITIATOR, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_MULTIPLE_LEGS, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_FORCE_BY_MPC, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_MPC_INITIATOR_SEPARATOR, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_RECEIPT_QUEUE_CONCURRENCY, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PULL_QUEUE_CONCURENCY, DomibusPropertyMetadata.Type.DOMAIN, true),

                new DomibusPropertyMetadata(DOMIBUS_EXTENSION_IAM_AUTHENTICATION_IDENTIFIER, DomibusPropertyMetadata.Type.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_EXTENSION_IAM_AUTHORIZATION_IDENTIFIER, DomibusPropertyMetadata.Type.DOMAIN, true),

                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_EXCEPTIONS_REST_ENABLE),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_INSTANCE_NAME),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

    public boolean hasKnownProperty(String name) {
        return this.getKnownProperties().containsKey(name);
    }

}
